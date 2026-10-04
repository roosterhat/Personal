package com.example.armcontrol.ephemeris

import java.time.LocalDate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Simplified satellite propagator: two-body motion plus the secular J2 drift of the node,
 * argument of perigee and mean anomaly. It is NOT SGP4 (no drag, no short-period terms, and
 * the TLE's mean motion is used as if it were an osculating one), so expect kilometre-scale
 * position errors that grow with distance from the TLE epoch.
 *
 * It exists so the pipeline runs with zero dependencies. Anything that needs real pointing
 * accuracy should replace [positionKm] with an SGP4 implementation; the signature is the
 * only thing the rest of the package depends on.
 */
object SatellitePropagator {
    private const val MU_KM3_S2 = 398_600.4418
    private const val EARTH_RADIUS_KM = 6_378.137
    private const val J2 = 1.08262668e-3
    private const val TWO_PI = 2.0 * Math.PI

    private class Parsed(
        val epochJd: Double,
        val incRad: Double,
        val raanRad: Double,
        val ecc: Double,
        val argPerigeeRad: Double,
        val meanAnomalyRad: Double,
        val meanMotionRadPerSec: Double
    )

    // Column positions below are the fixed TLE layout (0-based, end-exclusive).
    private fun parse(tle: Tle): Parsed {
        val l1 = tle.line1
        val l2 = tle.line2
        require(l1.length >= 63 && l2.length >= 63) { "TLE too short for ${tle.name}" }

        val yy = l1.substring(18, 20).trim().toInt()
        val year = if (yy < 57) 2000 + yy else 1900 + yy
        val dayOfYear = l1.substring(20, 32).trim().toDouble() // 1.0 == Jan 1 00:00 UTC
        val jan1Jd = LocalDate.of(year, 1, 1).toEpochDay() + 2_440_587.5
        val epochJd = jan1Jd + (dayOfYear - 1.0)

        val revPerDay = l2.substring(52, 63).trim().toDouble()
        return Parsed(
            epochJd = epochJd,
            incRad = Math.toRadians(l2.substring(8, 16).trim().toDouble()),
            raanRad = Math.toRadians(l2.substring(17, 25).trim().toDouble()),
            ecc = ("0." + l2.substring(26, 33).trim()).toDouble(), // decimal point is implied
            argPerigeeRad = Math.toRadians(l2.substring(34, 42).trim().toDouble()),
            meanAnomalyRad = Math.toRadians(l2.substring(43, 51).trim().toDouble()),
            meanMotionRadPerSec = revPerDay * TWO_PI / 86_400.0
        )
    }

    /** Geocentric position in km, in the TLE's inertial frame, at Julian date [jd]. */
    fun positionKm(tle: Tle, jd: Double): Vec3 {
        val p = parse(tle)
        val n = p.meanMotionRadPerSec
        val a = Math.cbrt(MU_KM3_S2 / (n * n))
        val e = p.ecc
        val semiLatus = a * (1.0 - e * e)
        val cosI = cos(p.incRad)

        val k = 1.5 * J2 * (EARTH_RADIUS_KM / semiLatus) * (EARTH_RADIUS_KM / semiLatus)
        val raanRate = -k * n * cosI
        val argPerigeeRate = 0.5 * k * n * (5.0 * cosI * cosI - 1.0)
        val meanAnomalyRate = n * (1.0 + 0.5 * k * sqrt(1.0 - e * e) * (3.0 * cosI * cosI - 1.0))

        val dt = (jd - p.epochJd) * 86_400.0
        val raan = p.raanRad + raanRate * dt
        val argPerigee = p.argPerigeeRad + argPerigeeRate * dt
        val meanAnomaly = p.meanAnomalyRad + meanAnomalyRate * dt

        val wrapped = Astro.normalizeDeg(Math.toDegrees(meanAnomaly))
        val ea = Astro.solveKepler(Math.toRadians(wrapped), e)
        val xp = a * (cos(ea) - e)
        val yp = a * sqrt(1.0 - e * e) * sin(ea)
        return Astro.perifocalToInertial(xp, yp, p.incRad, raan, argPerigee)
    }
}
