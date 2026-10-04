package com.example.armcontrol.ephemeris

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.GeomagneticField
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class LocationSource(private val context: Context) {
    private val client = LocationServices.getFusedLocationProviderClient(context)

    /** Returns null if permission is missing or no fix is available. */
    @SuppressLint("MissingPermission")
    suspend fun currentObserver(): Observer? {
        val granted = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
        Log.d("currentObserver", "granted: $granted")
        if (!granted) return null

        val fix = client
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
            .await()
            ?: client.lastLocation.await()   // fall back to a cached fix
        return fix?.toObserver()
    }
}

/** Where the phone is. [altitudeM] is metres above the WGS84 ellipsoid, which is what Location.altitude reports. */
data class Observer(
    val latitudeDeg: Double,
    val longitudeDeg: Double,
    val altitudeM: Double = 0.0,
    val declination: Float,
)

fun Location.toObserver(): Observer {
    val declination = GeomagneticField(latitude.toFloat(), longitude.toFloat(), altitude.toFloat(), System.currentTimeMillis()).declination
    return Observer(latitude + declination, longitude, if (hasAltitude()) altitude else 0.0, declination)
}

/**
 * Where to point.
 * [azimuthDeg]: 0..360, measured clockwise from TRUE north (90 = east, 180 = south).
 * [elevationDeg]: 0 at the horizon, 90 at the zenith, negative when below the horizon.
 */
data class AzEl(
    val azimuthDeg: Double,
    val elevationDeg: Double,
    val rangeKm: Double
) {
    val isAboveHorizon: Boolean get() = elevationDeg > 0.0
}

/**
 * Turns an entry's geocentric position into azimuth/elevation for an observer on the Earth.
 *
 * Pipeline: geocentric position -> equatorial frame of date (precession, for everything except
 * satellites, whose TLE frame is already of-date) -> rotate by Greenwich sidereal time into the
 * Earth-fixed frame -> subtract the observer's own position (this is what handles parallax) ->
 * rotate into the local east/north/up frame.
 *
 * What it deliberately ignores, and how big each effect is:
 *  - Atmospheric refraction: ~0.5 degrees at the horizon, about 1 arcminute at 45 degrees up.
 *  - Nutation, polar motion, UT1-UTC, aberration, planetary light-time: each at most a few
 *    to a few tens of arcseconds.
 *  - Azimuth is from true north. A magnetic compass reads differently by the local declination
 *    (android.hardware.GeomagneticField(...).declination gives it).
 *  - Satellite accuracy is limited by [SatellitePropagator], not by this function.
 */
object Pointing {
    private const val WGS84_A_KM = 6378.137
    private const val WGS84_F = 1.0 / 298.257223563
    private const val WGS84_E2 = WGS84_F * (2.0 - WGS84_F)
    private const val ARCSEC_RAD = Math.PI / (180.0 * 3600.0)

    fun azEl(
        entry: EphemerisEntry,
        observer: Observer,
        timeMs: Long = System.currentTimeMillis()
    ): AzEl {
        val jd = Astro.julianDate(timeMs)
        val geocentric = Ephemeris.positionKm(entry, timeMs)
        val ofDate = if (entry is EphemerisEntry.Satellite) geocentric else precessJ2000ToDate(geocentric, jd)
        return fromEquatorialOfDate(ofDate, observer, jd)
    }

    private class PrecessionAngles(val zeta: Double, val z: Double, val theta: Double)

    private fun precessionAngles(jd: Double): PrecessionAngles {
        val t = (jd - Astro.J2000_JD) / 36_525.0
        return PrecessionAngles(
            zeta = (2306.2181 * t + 0.30188 * t * t + 0.017998 * t * t * t) * ARCSEC_RAD,
            z = (2306.2181 * t + 1.09468 * t * t + 0.018203 * t * t * t) * ARCSEC_RAD,
            theta = (2004.3109 * t - 0.42665 * t * t - 0.041833 * t * t * t) * ARCSEC_RAD
        )
    }

    /** IAU 1976 precession of a J2000 equatorial vector to the mean equator/equinox of date. */
    internal fun precessJ2000ToDate(v: Vec3, jd: Double): Vec3 {
        val (zeta, z, theta) = precessionAngles(jd).let { Triple(it.zeta, it.z, it.theta) }
        val v1 = rotateZ(v, zeta)
        val v2 = Vec3(
            cos(theta) * v1.x - sin(theta) * v1.z,
            v1.y,
            sin(theta) * v1.x + cos(theta) * v1.z
        )
        return rotateZ(v2, z)
    }

    /** Inverse of [precessJ2000ToDate]: mean equator/equinox of date back to J2000. */
    internal fun precessDateToJ2000(v: Vec3, jd: Double): Vec3 {
        val (zeta, z, theta) = precessionAngles(jd).let { Triple(it.zeta, it.z, it.theta) }
        val v1 = rotateZ(v, -z)
        val v2 = Vec3(
            cos(theta) * v1.x + sin(theta) * v1.z,
            v1.y,
            -sin(theta) * v1.x + cos(theta) * v1.z
        )
        return rotateZ(v2, -zeta)
    }

    /** Rotation about Z that increases right ascension by [angleRad]. */
    private fun rotateZ(v: Vec3, angleRad: Double): Vec3 = Vec3(
        cos(angleRad) * v.x - sin(angleRad) * v.y,
        sin(angleRad) * v.x + cos(angleRad) * v.y,
        v.z
    )

    private fun fromEquatorialOfDate(v: Vec3, observer: Observer, jd: Double): AzEl {
        // Equatorial of date -> Earth-fixed: the Earth has turned GMST east since the equinox.
        val gmst = Math.toRadians(gmstDeg(jd))
        val cg = cos(gmst)
        val sg = sin(gmst)
        val ecef = Vec3(cg * v.x + sg * v.y, -sg * v.x + cg * v.y, v.z)

        val d = ecef - observerEcefKm(observer)

        val lat = Math.toRadians(observer.latitudeDeg)
        val lon = Math.toRadians(observer.longitudeDeg)
        val sinLat = sin(lat); val cosLat = cos(lat)
        val sinLon = sin(lon); val cosLon = cos(lon)

        val east = -sinLon * d.x + cosLon * d.y
        val north = -sinLat * cosLon * d.x - sinLat * sinLon * d.y + cosLat * d.z
        val up = cosLat * cosLon * d.x + cosLat * sinLon * d.y + sinLat * d.z

        val range = d.norm
        val azimuth = (Math.toDegrees(atan2(east, north)) + 360.0) % 360.0 + observer.declination
        val elevation = Math.toDegrees(asin((up / range).coerceIn(-1.0, 1.0)))
        return AzEl(azimuth, elevation, range)
    }

    /** Greenwich mean sidereal time in degrees (Meeus 12.4), treating UTC as UT1. */
    private fun gmstDeg(jd: Double): Double {
        val t = (jd - Astro.J2000_JD) / 36_525.0
        val deg = 280.46061837 +
            360.98564736629 * (jd - Astro.J2000_JD) +
            0.000387933 * t * t -
            t * t * t / 38_710_000.0
        return ((deg % 360.0) + 360.0) % 360.0
    }

    /** Geodetic latitude/longitude/height to Earth-fixed Cartesian on the WGS84 ellipsoid, in km. */
    private fun observerEcefKm(o: Observer): Vec3 {
        val lat = Math.toRadians(o.latitudeDeg)
        val lon = Math.toRadians(o.longitudeDeg)
        val h = o.altitudeM / 1000.0
        val n = WGS84_A_KM / Math.sqrt(1.0 - WGS84_E2 * sin(lat) * sin(lat))
        return Vec3(
            (n + h) * cos(lat) * cos(lon),
            (n + h) * cos(lat) * sin(lon),
            (n * (1.0 - WGS84_E2) + h) * sin(lat)
        )
    }
}
