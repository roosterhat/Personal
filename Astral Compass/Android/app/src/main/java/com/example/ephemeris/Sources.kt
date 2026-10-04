package com.example.armcontrol.ephemeris

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

internal object Http {
    suspend fun get(url: String, timeoutMs: Int = 15_000): String = withContext(Dispatchers.IO) {
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = timeoutMs
            conn.readTimeout = timeoutMs
            conn.setRequestProperty("User-Agent", "ArmControl/1.0")
            val code = conn.responseCode
            check(code == HttpURLConnection.HTTP_OK) { "HTTP $code from $url" }
            conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }
}


/**
 * Stars from the HYG database CSV (github.com/astronexus/HYGDatabase). Columns are looked up by
 * header name, so minor version-to-version layout changes don't break parsing.
 * ra is in hours, dec in degrees, dist in parsecs (100000 = "unknown", kept as a direction only).
 */
object StarSource {

    /**
     * Keeps every star with a proper name, plus unnamed ones brighter than [maxMagnitude]
     * (lower magnitude = brighter). Positions are computed once here and stored.
     */
    fun parse(
        csv: String,
        maxMagnitude: Double = 4.0,
        onProgress: (Float) -> Unit = {} // 0..1 through the file, for a loading bar
    ): List<EphemerisEntry.Star> =
        parseLines(csv.lineSequence().iterator(), csv.length.toLong(), maxMagnitude, onProgress)

    /**
     * Same as [parse] but reads line by line, so a large file never has to be held in memory as one
     * string. [totalChars] is the file size, used only to turn "how much has been read" into 0..1;
     * pass 0 if unknown and no progress is reported until the end.
     */
    fun parseLines(
        lines: Iterator<String>,
        totalChars: Long,
        maxMagnitude: Double = 4.0,
        onProgress: (Float) -> Unit = {}
    ): List<EphemerisEntry.Star> {
        if (!lines.hasNext()) return emptyList()

        val headerLine = lines.next()
        var consumed = headerLine.length + 1L
        val header = splitCsv(headerLine).map { it.trim().lowercase() }
        val iId = header.indexOf("id")
        val iHip = header.indexOf("hip")
        val iProper = header.indexOf("proper")
        val iBayer = header.indexOf("bayer")
        val iCon = header.indexOf("con")
        val iRa = header.indexOf("ra")
        val iDec = header.indexOf("dec")
        val iDist = header.indexOf("dist")
        val iMag = header.indexOf("mag")
        require(listOf(iId, iRa, iDec, iDist, iMag).all { it >= 0 }) {
            "Not a HYG catalog: expected id, ra, dec, dist and mag columns"
        }

        val out = ArrayList<EphemerisEntry.Star>()
        var lineNo = 0
        for (line in lines) {
            lineNo++
            consumed += line.length + 1
            if (totalChars > 0 && lineNo % 2_000 == 0) {
                onProgress((consumed.toFloat() / totalChars).coerceIn(0f, 1f))
            }
            if (line.isBlank()) continue
            val f = splitCsv(line)
            fun field(i: Int): String = if (i in f.indices) f[i].trim() else ""

            val proper = field(iProper)
            if (proper == "Sol") continue
            val mag = field(iMag).toDoubleOrNull() ?: continue
            if (proper.isEmpty() && mag > maxMagnitude) continue

            val raHours = field(iRa).toDoubleOrNull() ?: continue
            val decDeg = field(iDec).toDoubleOrNull() ?: continue
            val distPc = field(iDist).toDoubleOrNull()?.takeIf { it > 0.0 } ?: continue

            val bayer = field(iBayer)
            val con = field(iCon)
            val hip = field(iHip)
            val name = when {
                proper.isNotEmpty() -> proper
                bayer.isNotEmpty() && con.isNotEmpty() -> "$bayer $con"
                hip.isNotEmpty() -> "HIP $hip"
                else -> "Star ${field(iId)}"
            }

            val ra = Math.toRadians(raHours * 15.0)
            val dec = Math.toRadians(decDeg)
            val d = distPc * Astro.PARSEC_KM
            out += EphemerisEntry.Star(
                id = "hyg-${field(iId)}",
                name = name,
                magnitude = mag,
                positionKm = Vec3(d * cos(dec) * cos(ra), d * cos(dec) * sin(ra), d * sin(dec))
            )
        }
        onProgress(1f)
        return out
    }

    internal fun splitCsv(line: String): List<String> {
        val out = ArrayList<String>()
        val sb = StringBuilder()
        var inQuotes = false
        for (c in line) {
            when {
                c == '"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> { out += sb.toString(); sb.setLength(0) }
                else -> sb.append(c)
            }
        }
        out += sb.toString()
        return out
    }
}

/** Asteroid osculating elements from the JPL Small-Body Database API (ssd-api.jpl.nasa.gov/sbdb.api). */
object AsteroidSource {
    val DEFAULT_TARGETS = listOf(
        "Ceres", "Vesta", "Pallas", "Hygiea", "Juno", "Psyche", "Eros", "Bennu", "Ryugu", "Apophis"
    )

    private const val GAUSS_DEG_PER_DAY = 0.9856076686 // used if the API omits mean motion

    /** Returns null if the name is ambiguous or has no orbit. Throws on network/HTTP failure. */
    suspend fun fetch(designation: String): EphemerisEntry.Asteroid? {
        val url = "https://ssd-api.jpl.nasa.gov/sbdb.api?sstr=" + URLEncoder.encode(designation, "UTF-8")
        return parse(Http.get(url), designation)
    }

    fun parse(json: String, designation: String): EphemerisEntry.Asteroid? {
        val root = JSONObject(json)
        val orbit = root.optJSONObject("orbit") ?: return null
        val obj = root.optJSONObject("object")

        val values = HashMap<String, Double>()
        val elements = orbit.optJSONArray("elements") ?: return null
        for (i in 0 until elements.length()) {
            val el = elements.getJSONObject(i)
            el.optString("value").toDoubleOrNull()?.let { values[el.optString("name")] = it }
        }

        val epochJd = orbit.optString("epoch").toDoubleOrNull() ?: return null
        val a = values["a"] ?: return null
        val e = values["e"] ?: return null
        val inc = values["i"] ?: return null
        val raan = values["om"] ?: return null
        val argPeri = values["w"] ?: return null
        val meanAnomaly = values["ma"] ?: return null
        val meanMotion = values["n"] ?: (GAUSS_DEG_PER_DAY / a.pow(1.5))

        val name = obj?.optString("shortname")?.takeIf { it.isNotBlank() }
            ?: obj?.optString("fullname")?.takeIf { it.isNotBlank() }
            ?: designation
        val id = "ast-" + (obj?.optString("spkid")?.takeIf { it.isNotBlank() } ?: designation.lowercase())

        return EphemerisEntry.Asteroid(
            id = id,
            name = name,
            elements = OrbitalElements(epochJd, a, e, inc, raan, argPeri, meanAnomaly, meanMotion)
        )
    }
}

/** Satellite TLEs from CelesTrak's GP endpoint (celestrak.org/NORAD/elements/gp.php), 3-line format. */
object SatelliteSource_CelesTrak {
    val DEFAULT_GROUPS = listOf("stations", "visual")

    suspend fun fetchGroup(group: String): List<EphemerisEntry.Satellite> {
        val url = "https://celestrak.org/NORAD/elements/gp.php?GROUP=" +
            URLEncoder.encode(group, "UTF-8") + "&FORMAT=tle"
        return parse(Http.get(url, timeoutMs = 5000))
    }

    fun parse(text: String): List<EphemerisEntry.Satellite> {
        val lines = text.lines().map { it.trimEnd() }.filter { it.isNotBlank() }
        val out = ArrayList<EphemerisEntry.Satellite>()
        var i = 0
        while (i + 2 < lines.size) {
            val l1 = lines[i + 1]
            val l2 = lines[i + 2]
            if (l1.startsWith("1 ") && l2.startsWith("2 ") && l1.length >= 63 && l2.length >= 63) {
                val name = lines[i].trim()
                out += EphemerisEntry.Satellite(
                    id = "sat-" + l1.substring(2, 7).trim(),
                    name = name,
                    tle = Tle(name, l1, l2)
                )
                i += 3
            } else {
                i += 1
            }
        }
        return out
    }
}

object SatelliteSource {
    private const val LOGIN_URL = "https://www.space-track.org/ajaxauth/login"
    private const val QUERY_BASE = "https://www.space-track.org/basicspacedata/query"

    suspend fun fetch(credentials: Credentials?): List<EphemerisEntry.Satellite> {
        val body = getContent(credentials)
        val satellites = parse(body)
        check(satellites.isNotEmpty()) { "Space-Track returned no usable TLEs" }
        return satellites
    }

    private suspend fun getContent(credentials: Credentials?): String = withContext(Dispatchers.IO) {
        val cookie = login(credentials,)
        try {
            get3LEs(cookie)
        } catch (e: Exception) {
            ""
        }
    }

    /** Space-Track's 3le format prefixes each name line with "0 " ("0 ISS (ZARYA)"); strip it. */
    fun parse(text: String): List<EphemerisEntry.Satellite> {
        val lines = text.lineSequence()
            .map { it.trimEnd() }
            .filter { it.isNotBlank() }
            .joinToString("\n") { line ->
                if (line.startsWith("1 ") || line.startsWith("2 ")) line else line.removePrefix("0 ")
            }.lines().map { it.trimEnd() }.filter { it.isNotBlank() }

        val out = ArrayList<EphemerisEntry.Satellite>()
        var i = 0
        while (i + 2 < lines.size) {
            val l1 = lines[i + 1]
            val l2 = lines[i + 2]
            if (l1.startsWith("1 ") && l2.startsWith("2 ") && l1.length >= 63 && l2.length >= 63) {
                val name = lines[i].trim()
                out += EphemerisEntry.Satellite(
                    id = "sat-" + l1.substring(2, 7).trim(),
                    name = name,
                    tle = Tle(name, l1, l2)
                )
                i += 3
            } else {
                i += 1
            }
        }
        return out
    }

    private fun login(credentials: Credentials?): String {
        check(credentials != null) {
            "Failed to load space-track credentials"
        }

        fun enc(s: String) = URLEncoder.encode(s, "UTF-8")
        val form = "identity=${enc(credentials.identity)}&password=${enc(credentials.password)}"

        val conn = URL(LOGIN_URL).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            conn.setRequestProperty("User-Agent", "ArmControl/1.0")
            conn.outputStream.use { it.write(form.toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            check(code == HttpURLConnection.HTTP_OK) {
                "Space-Track login HTTP $code: ${text.take(200).ifBlank { "no body" }}"
            }
            // A failed login still returns HTTP 200, with a small JSON body and no cookie.
            check(!(text.contains("\"Login\"") && text.contains("Failed", ignoreCase = true))) {
                "Space-Track login failed: check the username/password"
            }

            // Keep only "name=value" from each Set-Cookie (drop Path, Expires, HttpOnly, ...)
            val cookies = conn.headerFields.entries
                .filter { it.key.equals("Set-Cookie", ignoreCase = true) }
                .flatMap { it.value }
                .map { it.substringBefore(';').trim() }
                .filter { it.contains('=') }
            check(cookies.isNotEmpty()) { "Space-Track login returned no session cookie" }
            return cookies.joinToString("; ")
        } finally {
            conn.disconnect()
        }
    }

    private fun get3LEs(cookie: String): String {
        val conn = URL("$QUERY_BASE/class/gp/decay_date/null-val/epoch/%3Enow-30/orderby/NORAD_CAT_ID/format/3le").openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.setRequestProperty("Cookie", cookie)
            conn.setRequestProperty("User-Agent", "ArmControl/1.0")
            val code = conn.responseCode
            val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            check(code == HttpURLConnection.HTTP_OK) {
                "Space-Track HTTP $code: ${text.take(200).ifBlank { "no body" }}"
            }
            return text
        } finally {
            conn.disconnect()
        }
    }
}
