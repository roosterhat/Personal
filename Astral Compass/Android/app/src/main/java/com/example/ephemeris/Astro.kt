package com.example.armcontrol.ephemeris

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Shared orbital-mechanics helpers. All angles are degrees unless a parameter says "Rad". */
object Astro {
    const val AU_KM = 149_597_870.7
    const val PARSEC_KM = 3.0856775814913673e13
    const val J2000_JD = 2_451_545.0
    private const val OBLIQUITY_J2000_DEG = 23.43928

    fun julianDate(epochMillis: Long): Double = epochMillis / 86_400_000.0 + 2_440_587.5

    fun centuriesSinceJ2000(jd: Double): Double = (jd - J2000_JD) / 36_525.0

    /** Wraps an angle into (-180, 180]. */
    fun normalizeDeg(deg: Double): Double {
        var r = deg % 360.0
        if (r > 180.0) r -= 360.0
        if (r <= -180.0) r += 360.0
        return r
    }

    /** Solves Kepler's equation M = E - e*sin(E) for the eccentric anomaly E (Newton's method). */
    fun solveKepler(meanAnomalyRad: Double, e: Double): Double {
        var ea = if (e < 0.8) meanAnomalyRad else Math.PI
        for (i in 0 until 50) {
            val delta = (ea - e * sin(ea) - meanAnomalyRad) / (1.0 - e * cos(ea))
            ea -= delta
            if (abs(delta) < 1e-12) break
        }
        return ea
    }

    /** Rotates a point in the orbital plane (perifocal x/y) into the reference frame the angles are measured in. */
    fun perifocalToInertial(
        xp: Double, yp: Double,
        incRad: Double, raanRad: Double, argPeriRad: Double
    ): Vec3 {
        val cw = cos(argPeriRad); val sw = sin(argPeriRad)
        val co = cos(raanRad); val so = sin(raanRad)
        val ci = cos(incRad); val si = sin(incRad)
        return Vec3(
            (cw * co - sw * so * ci) * xp + (-sw * co - cw * so * ci) * yp,
            (cw * so + sw * co * ci) * xp + (-sw * so + cw * co * ci) * yp,
            (sw * si) * xp + (cw * si) * yp
        )
    }

    /** Heliocentric position in the ecliptic J2000 frame, in AU. */
    fun heliocentricEcliptic(
        a: Double, e: Double, incDeg: Double,
        raanDeg: Double, argPeriDeg: Double, meanAnomalyDeg: Double
    ): Vec3 {
        val m = Math.toRadians(normalizeDeg(meanAnomalyDeg))
        val ea = solveKepler(m, e)
        val xp = a * (cos(ea) - e)
        val yp = a * sqrt(1.0 - e * e) * sin(ea)
        return perifocalToInertial(
            xp, yp,
            Math.toRadians(incDeg), Math.toRadians(raanDeg), Math.toRadians(argPeriDeg)
        )
    }

    /** Rotates ecliptic J2000 coordinates into equatorial J2000 (a rotation about X by the obliquity). */
    fun eclipticToEquatorial(v: Vec3): Vec3 {
        val eps = Math.toRadians(OBLIQUITY_J2000_DEG)
        return Vec3(
            v.x,
            v.y * cos(eps) - v.z * sin(eps),
            v.y * sin(eps) + v.z * cos(eps)
        )
    }
}
