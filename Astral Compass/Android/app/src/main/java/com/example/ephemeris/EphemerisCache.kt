package com.example.armcontrol.ephemeris

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

private const val DAY_MS = 86_400_000L

/**
 * Persists fetched catalogs as JSON under filesDir (not cacheDir, which Android may purge
 * whenever it likes, and these files are what lets the app work without a network).
 * Each category carries its own time-to-live.
 */
class EphemerisCache(context: Context) {

    enum class Category(val fileName: String, val ttlMs: Long) {
        STARS("stars.json", 365 * DAY_MS),        // positions are effectively fixed
        ASTEROIDS("asteroids.json", 30 * DAY_MS), // osculating elements drift slowly
        SATELLITES("satellites.json", 2 * DAY_MS) // TLEs go stale within days
    }

    class Snapshot(val fetchedAtMs: Long, val entries: List<EphemerisEntry>) {
        fun isFresh(ttlMs: Long, nowMs: Long): Boolean = nowMs - fetchedAtMs < ttlMs
    }

    private val dir = File(context.filesDir, "ephemeris").apply { mkdirs() }

    /** Returns null if there is no cache file or it can't be read (a corrupt file is treated as missing). */
    fun load(category: Category): Snapshot? {
        val file = File(dir, category.fileName)
        if (!file.exists()) return null
        return try {
            val root = JSONObject(file.readText())
            val arr = root.getJSONArray("entries")
            Snapshot(
                fetchedAtMs = root.getLong("fetchedAt"),
                entries = List(arr.length()) { EntryJson.fromJson(arr.getJSONObject(it)) }
            )
        } catch (e: Exception) {
            null
        }
    }

    fun save(category: Category, entries: List<EphemerisEntry>, nowMs: Long) {
        val arr = JSONArray()
        entries.forEach { arr.put(EntryJson.toJson(it)) }
        val root = JSONObject().put("fetchedAt", nowMs).put("entries", arr)

        // Write to a temp file first so a crash mid-write can't leave a half-written cache behind.
        val target = File(dir, category.fileName)
        val tmp = File(dir, category.fileName + ".tmp")
        tmp.writeText(root.toString())
        if (!tmp.renameTo(target)) {
            tmp.copyTo(target, overwrite = true)
            tmp.delete()
        }
    }

    fun clear() {
        dir.listFiles()?.forEach { it.delete() }
    }
}

private object EntryJson {
    fun toJson(entry: EphemerisEntry): JSONObject {
        val o = JSONObject()
            .put("id", entry.id)
            .put("name", entry.name)
            .put("type", entry.type.name)
        return when (entry) {
            is EphemerisEntry.Star -> o
                .put("mag", entry.magnitude)
                .put("x", entry.positionKm.x)
                .put("y", entry.positionKm.y)
                .put("z", entry.positionKm.z)
            is EphemerisEntry.Planet -> o
            is EphemerisEntry.Asteroid -> o.put(
                "el", JSONObject()
                    .put("epoch", entry.elements.epochJd)
                    .put("a", entry.elements.a)
                    .put("e", entry.elements.e)
                    .put("i", entry.elements.inclination)
                    .put("om", entry.elements.raan)
                    .put("w", entry.elements.argPeriapsis)
                    .put("ma", entry.elements.meanAnomaly)
                    .put("n", entry.elements.meanMotion)
            )
            is EphemerisEntry.Satellite -> o
                .put("l1", entry.tle.line1)
                .put("l2", entry.tle.line2)
        }
    }

    fun fromJson(o: JSONObject): EphemerisEntry {
        val id = o.getString("id")
        val name = o.getString("name")
        return when (ObjectType.valueOf(o.getString("type"))) {
            ObjectType.STAR -> EphemerisEntry.Star(
                id, name, o.getDouble("mag"),
                Vec3(o.getDouble("x"), o.getDouble("y"), o.getDouble("z"))
            )
            ObjectType.PLANET -> EphemerisEntry.Planet(id, name)
            ObjectType.ASTEROID -> {
                val el = o.getJSONObject("el")
                EphemerisEntry.Asteroid(
                    id, name,
                    OrbitalElements(
                        epochJd = el.getDouble("epoch"),
                        a = el.getDouble("a"),
                        e = el.getDouble("e"),
                        inclination = el.getDouble("i"),
                        raan = el.getDouble("om"),
                        argPeriapsis = el.getDouble("w"),
                        meanAnomaly = el.getDouble("ma"),
                        meanMotion = el.getDouble("n")
                    )
                )
            }
            ObjectType.SATELLITE -> EphemerisEntry.Satellite(
                id, name, Tle(name, o.getString("l1"), o.getString("l2"))
            )
        }
    }
}
