package com.example.armcontrol.ephemeris

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * Computes where an entry is, right now or at any instant. Cheap (microseconds), so positions
 * of moving objects are computed on demand instead of being cached.
 */
object Ephemeris {
    fun positionKm(entry: EphemerisEntry, timeMs: Long = System.currentTimeMillis()): Vec3 {
        val jd = Astro.julianDate(timeMs)
        return when (entry) {
            is EphemerisEntry.Star -> entry.positionKm
            is EphemerisEntry.Planet -> SolarSystem.planetGeocentricKm(entry.id, jd)
            is EphemerisEntry.Asteroid -> SolarSystem.asteroidGeocentricKm(entry.elements, jd)
            is EphemerisEntry.Satellite -> SatellitePropagator.positionKm(entry.tle, jd)
        }
    }
}

/**
 * Startup progress for a loading screen. [fraction] runs 0..1 and does not go backwards
 * within one [EphemerisRepository.refresh]. [finished] flips to true once, at the very end,
 * and [errors] is then final (empty means everything loaded).
 */
data class InitProgress(
    val fraction: Float = 0f,
    val message: String = "Starting…",
    val errors: List<String> = emptyList(),
    val finished: Boolean = false
)

/**
 * Gathers every searchable object, caches the source data on device, and exposes the merged list.
 *
 * What gets cached, and why: the expensive, slow-changing inputs. Star positions are stored
 * directly (they don't move); asteroid elements and satellite TLEs are stored with a TTL.
 * Positions of moving bodies change every second, so they are computed on demand via
 * [Ephemeris.positionKm] rather than stored. Planets need no network or cache at all.
 *
 * Frames and accuracy, which matter once this feeds a pointing arm:
 *  - Stars, planets, asteroids are geocentric J2000 equatorial. To get az/el for your site you
 *    still need to rotate into the frame of date (precession is roughly 0.36 degrees by 2026)
 *    and apply local sidereal time and latitude.
 *  - Satellite vectors are in the TLE's TEME frame (equator/equinox of date). Do not mix them
 *    with the J2000 ones without converting.
 *  - Everything is geocentric. For the Moon or low satellites, observer parallax is large and
 *    you should subtract the observer's own position vector before computing az/el.
 *  - [SatellitePropagator] is a simplified J2 model, not SGP4. Fine for a list and a rough
 *    pointing direction; replace it for anything precise.
 */
class EphemerisRepository(
    private val context: Context,
    private val credentialsStore: CredentialsStore,
    private val cache: EphemerisCache = EphemerisCache(context),
    private val asteroidTargets: List<String> = AsteroidSource.DEFAULT_TARGETS,
    /** Optional download location for the HYG CSV, used only if assets/hyg.csv is absent. */
    private val starCatalogUrl: String? = null,
    private val clock: () -> Long = System::currentTimeMillis
) {
    private val _entries = MutableStateFlow<List<EphemerisEntry>>(emptyList())
    val entries: StateFlow<List<EphemerisEntry>> = _entries.asStateFlow()

    private val _progress = MutableStateFlow(InitProgress())
    val progress: StateFlow<InitProgress> = _progress.asStateFlow()

    /** Shows whatever is already on disk immediately, without touching the network. */
    suspend fun loadFromCache() {
        val (stars, asteroids, satellites) = withContext(Dispatchers.IO) {
            Triple(
                cache.load(EphemerisCache.Category.STARS)?.entries.orEmpty(),
                cache.load(EphemerisCache.Category.ASTEROIDS)?.entries.orEmpty(),
                cache.load(EphemerisCache.Category.SATELLITES)?.entries.orEmpty()
            )
        }
        _entries.value = SolarSystem.planetEntries + stars + asteroids + satellites
    }

    /**
     * Refreshes any category whose cache is missing or past its TTL (or all of them if [force]),
     * publishing [progress] as it goes. A failed refresh falls back to the stale cache rather
     * than emptying the list. Returns human-readable errors, empty if everything succeeded.
     */
    suspend fun refresh(force: Boolean = false): List<String> {
        _progress.value = InitProgress()
        val errors = mutableListOf<String>()

        // Progress is weighted by how long each stage takes: parsing the star catalog is the
        // heavy part, and the other stages are one network request per asteroid / satellite group.
        val asteroidUnits = asteroidTargets.size.coerceAtLeast(1)
        val satelliteUnits = 1
        val tracker = Tracker(total = STAR_WEIGHT + asteroidUnits + satelliteUnits)

        val stars = loadCategory(
            EphemerisCache.Category.STARS, force, errors, tracker, STAR_WEIGHT, "stars"
        ) { report -> fetchStars(report) }
        val asteroids = loadCategory(
            EphemerisCache.Category.ASTEROIDS, force, errors, tracker, asteroidUnits.toFloat(), "asteroids"
        ) { report -> fetchAsteroids(report) }
        val satellites = loadCategory(
            EphemerisCache.Category.SATELLITES, force, errors, tracker, satelliteUnits.toFloat(), "satellites"
        ) { report -> fetchSatellites(report) }

        _entries.value = SolarSystem.planetEntries + stars + asteroids + satellites
        _progress.value = InitProgress(
            fraction = 1f,
            message = if (errors.isEmpty()) "Ready" else "Finished with errors",
            errors = errors.toList(),
            finished = true
        )
        return errors
    }

    fun positionKm(entry: EphemerisEntry, timeMs: Long = clock()): Vec3 = Ephemeris.positionKm(entry, timeMs)

    fun azEl(entry: EphemerisEntry, observer: Observer, timeMs: Long = clock()): AzEl =
        Pointing.azEl(entry, observer, timeMs)

    /** Maps "how far through the current stage" onto the overall 0..1 bar. */
    private inner class Tracker(private val total: Float) {
        private var stageStart = 0f
        private var stageWeight = 0f

        fun begin(weight: Float) {
            stageStart += stageWeight
            stageWeight = weight
        }

        fun update(stageFraction: Float, message: String) {
            val done = stageStart + stageWeight * stageFraction.coerceIn(0f, 1f)
            _progress.update { it.copy(fraction = (done / total).coerceIn(0f, 1f), message = message) }
        }
    }

    private suspend fun loadCategory(
        category: EphemerisCache.Category,
        force: Boolean,
        errors: MutableList<String>,
        tracker: Tracker,
        weight: Float,
        label: String,
        fetch: suspend (report: (Float, String) -> Unit) -> List<EphemerisEntry>
    ): List<EphemerisEntry> {
        tracker.begin(weight)
        tracker.update(0f, "Checking saved $label…")

        val now = clock()
        val cached = withContext(Dispatchers.IO) { cache.load(category) }
        if (cached != null && !force && cached.isFresh(category.ttlMs, now)) {
            tracker.update(1f, "Loaded $label from device")
            return cached.entries
        }

        return try {
            val fresh = fetch { fraction, message -> tracker.update(fraction, message) }
            if (fresh.isEmpty()) error("source returned no entries")
            withContext(Dispatchers.IO) { cache.save(category, fresh, now) }
            tracker.update(1f, "Saved $label")
            fresh
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errors += "${category.name.lowercase()}: ${e.message ?: e.javaClass.simpleName}"
            tracker.update(1f, "Couldn't update $label")
            cached?.entries.orEmpty()
        }
    }

    private suspend fun fetchStars(report: (Float, String) -> Unit): List<EphemerisEntry> {
        report(0.02f, "Reading star catalog…")
        // Stars don't change, so a bundled copy is the best source: no network, no stale URL.
        val bundled = withContext(Dispatchers.IO) {
            try {
                context.assets.open(STAR_ASSET).bufferedReader().use { it.readText() }
            } catch (e: IOException) {
                null
            }
        }
        val csv = bundled
            ?: starCatalogUrl?.let {
                report(0.02f, "Downloading star catalog…")
                Http.get(it, timeoutMs = 60_000)
            }
            ?: error("No star catalog: put the HYG CSV at app/src/main/assets/$STAR_ASSET or pass starCatalogUrl")

        report(0.1f, "Indexing stars…")
        return withContext(Dispatchers.Default) {
            StarSource.parse(csv) { f -> report(0.1f + 0.9f * f, "Indexing stars…") }
        }
    }

    private suspend fun fetchAsteroids(report: (Float, String) -> Unit): List<EphemerisEntry> {
        val out = ArrayList<EphemerisEntry>()
        var lastError: Exception? = null
        for ((index, target) in asteroidTargets.withIndex()) {
            report(index.toFloat() / asteroidTargets.size, "Fetching asteroid: $target")
            try {
                AsteroidSource.fetch(target)?.let { out += it }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastError = e // one bad name shouldn't discard the rest
            }
        }
        if (out.isEmpty() && lastError != null) throw lastError
        return out
    }

    private suspend fun fetchSatellites(report: (Float, String) -> Unit): List<EphemerisEntry> {
        val byId = LinkedHashMap<String, EphemerisEntry>()
        var lastError: Exception? = null
        report(0f, "Fetching satellites")
        try {
            SatelliteSource.fetch(credentialsStore.load()).forEach { byId.putIfAbsent(it.id, it) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            lastError = e
        }
        if (byId.isEmpty() && lastError != null) throw lastError
        return byId.values.toList()
    }

    /** The display name of a document picked with the system file picker, or null if unknown. */
    fun displayName(uri: Uri): String? = try {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { c -> if (c.moveToFirst() && !c.isNull(0)) c.getString(0) else null }
    } catch (e: Exception) {
        null
    }

    /**
     * Replaces the star catalog with a HYG CSV the user picked. The file is copied into app
     * storage first (so it survives a cache clear and no permission to the original is needed),
     * parsed with [onProgress] reporting 0..1, and only swapped in if it parses to at least one
     * star, so a wrong file can't wipe out the working catalog. Returns the number of stars.
     */
    suspend fun importStarCatalog(uri: Uri, onProgress: (Float) -> Unit): Int =
        withContext(Dispatchers.IO) {
            val target = File(context.filesDir, USER_STAR_FILE)
            val tmp = File(context.filesDir, "$USER_STAR_FILE.tmp")
            var success = false
            try {
                val input = context.contentResolver.openInputStream(uri)
                    ?: throw IOException("Couldn't open the selected file")
                input.use { src -> tmp.outputStream().use { dst -> src.copyTo(dst) } }

                val stars = tmp.bufferedReader().use { reader ->
                    StarSource.parseLines(reader.lineSequence().iterator(), tmp.length(), onProgress = onProgress)
                }
                check(stars.isNotEmpty()) { "No stars found: expected a HYG database CSV" }

                if (!tmp.renameTo(target)) {
                    tmp.copyTo(target, overwrite = true)
                    tmp.delete()
                }
                cache.save(EphemerisCache.Category.STARS, stars, clock())
                _entries.update { current ->
                    SolarSystem.planetEntries + stars +
                            current.filter { it is EphemerisEntry.Asteroid || it is EphemerisEntry.Satellite }
                }
                success = true
                stars.size
            } finally {
                if (!success) tmp.delete()
            }
        }

    suspend fun clearCache() {
        withContext(Dispatchers.IO) { cache.clear() }
        _entries.value = SolarSystem.planetEntries
    }

    private companion object {
        const val USER_STAR_FILE = "hyg_user.csv"
        const val STAR_ASSET = "hyg.csv"
        const val STAR_WEIGHT = 3f
    }
}
