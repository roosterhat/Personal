package com.example.armcontrol.ephemeris

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * The Moon, from the main terms of Meeus "Astronomical Algorithms" ch. 47 (the ELP-2000/82
 * series truncated to 60 longitude/distance and 60 latitude terms).
 *
 * Accuracy: about 10 arcseconds in longitude and 4 in latitude, roughly 20 km in distance. The
 * Moon's disc is ~1800 arcseconds across, so this is far below anything an arm can resolve.
 * Nutation is ignored (up to ~17 arcseconds), as is light time; both are negligible here.
 *
 * Output is geocentric J2000 equatorial km, like the planets. The Moon is close enough that
 * parallax is up to ~1 degree, which [Pointing] handles by subtracting the observer's position.
 */
object MoonEphemeris {

    // Each row: D, M, M', F, sum-l (1e-6 deg), sum-r (1e-3 km)
    private val LONGITUDE_DISTANCE = arrayOf(
        intArrayOf(0, 0, 1, 0, 6288774, -20905355),
        intArrayOf(2, 0, -1, 0, 1274027, -3699111),
        intArrayOf(2, 0, 0, 0, 658314, -2955968),
        intArrayOf(0, 0, 2, 0, 213618, -569925),
        intArrayOf(0, 1, 0, 0, -185116, 48888),
        intArrayOf(0, 0, 0, 2, -114332, -3149),
        intArrayOf(2, 0, -2, 0, 58793, 246158),
        intArrayOf(2, -1, -1, 0, 57066, -152138),
        intArrayOf(2, 0, 1, 0, 53322, -170733),
        intArrayOf(2, -1, 0, 0, 45758, -204586),
        intArrayOf(0, 1, -1, 0, -40923, -129620),
        intArrayOf(1, 0, 0, 0, -34720, 108743),
        intArrayOf(0, 1, 1, 0, -30383, 104755),
        intArrayOf(2, 0, 0, -2, 15327, 10321),
        intArrayOf(0, 0, 1, 2, -12528, 0),
        intArrayOf(0, 0, 1, -2, 10980, 79661),
        intArrayOf(4, 0, -1, 0, 10675, -34782),
        intArrayOf(0, 0, 3, 0, 10034, -23210),
        intArrayOf(4, 0, -2, 0, 8548, -21636),
        intArrayOf(2, 1, -1, 0, -7888, 24208),
        intArrayOf(2, 1, 0, 0, -6766, 30824),
        intArrayOf(1, 0, -1, 0, -5163, -8379),
        intArrayOf(1, 1, 0, 0, 4987, -16675),
        intArrayOf(2, -1, 1, 0, 4036, -12831),
        intArrayOf(2, 0, 2, 0, 3994, -10445),
        intArrayOf(4, 0, 0, 0, 3861, -11650),
        intArrayOf(2, 0, -3, 0, 3665, 14403),
        intArrayOf(0, 1, -2, 0, -2689, -7003),
        intArrayOf(2, 0, -1, 2, -2602, 0),
        intArrayOf(2, -1, -2, 0, 2390, 10056),
        intArrayOf(1, 0, 1, 0, -2348, 6322),
        intArrayOf(2, -2, 0, 0, 2236, -9884),
        intArrayOf(0, 1, 2, 0, -2120, 5751),
        intArrayOf(0, 2, 0, 0, -2069, 0),
        intArrayOf(2, -2, -1, 0, 2048, -4950),
        intArrayOf(2, 0, 1, -2, -1773, 4130),
        intArrayOf(2, 0, 0, 2, -1595, 0),
        intArrayOf(4, -1, -1, 0, 1215, -3958),
        intArrayOf(0, 0, 2, 2, -1110, 0),
        intArrayOf(3, 0, -1, 0, -892, 3258),
        intArrayOf(2, 1, 1, 0, -810, 2616),
        intArrayOf(4, -1, -2, 0, 759, -1897),
        intArrayOf(0, 2, -1, 0, -713, -2117),
        intArrayOf(2, 2, -1, 0, -700, 2354),
        intArrayOf(2, 1, -2, 0, 691, 0),
        intArrayOf(2, -1, 0, -2, 596, 0),
        intArrayOf(4, 0, 1, 0, 549, -1423),
        intArrayOf(0, 0, 4, 0, 537, -1117),
        intArrayOf(4, -1, 0, 0, 520, -1571),
        intArrayOf(1, 0, -2, 0, -487, -1739),
        intArrayOf(2, 1, 0, -2, -399, 0),
        intArrayOf(0, 0, 2, -2, -381, -4421),
        intArrayOf(1, 1, 1, 0, 351, 0),
        intArrayOf(3, 0, -2, 0, -340, 0),
        intArrayOf(4, 0, -3, 0, 330, 0),
        intArrayOf(2, -1, 2, 0, 327, 0),
        intArrayOf(0, 2, 1, 0, -323, 1165),
        intArrayOf(1, 1, -1, 0, 299, 0),
        intArrayOf(2, 0, 3, 0, 294, 0),
        intArrayOf(2, 0, -1, -2, 0, 8752)
    )

    // Each row: D, M, M', F, sum-b (1e-6 deg)
    private val LATITUDE = arrayOf(
        intArrayOf(0, 0, 0, 1, 5128122),
        intArrayOf(0, 0, 1, 1, 280602),
        intArrayOf(0, 0, 1, -1, 277693),
        intArrayOf(2, 0, 0, -1, 173237),
        intArrayOf(2, 0, -1, 1, 55413),
        intArrayOf(2, 0, -1, -1, 46271),
        intArrayOf(2, 0, 0, 1, 32573),
        intArrayOf(0, 0, 2, 1, 17198),
        intArrayOf(2, 0, 1, -1, 9266),
        intArrayOf(0, 0, 2, -1, 8822),
        intArrayOf(2, -1, 0, -1, 8216),
        intArrayOf(2, 0, -2, -1, 4324),
        intArrayOf(2, 0, 1, 1, 4200),
        intArrayOf(2, 1, 0, -1, -3359),
        intArrayOf(2, -1, -1, 1, 2463),
        intArrayOf(2, -1, 0, 1, 2211),
        intArrayOf(2, -1, -1, -1, 2065),
        intArrayOf(0, 1, -1, -1, -1870),
        intArrayOf(4, 0, -1, -1, 1828),
        intArrayOf(0, 1, 0, 1, -1794),
        intArrayOf(0, 0, 0, 3, -1749),
        intArrayOf(0, 1, -1, 1, -1565),
        intArrayOf(1, 0, 0, 1, -1491),
        intArrayOf(0, 1, 1, 1, -1475),
        intArrayOf(0, 1, 1, -1, -1410),
        intArrayOf(0, 1, 0, -1, -1344),
        intArrayOf(1, 0, 0, -1, -1335),
        intArrayOf(0, 0, 3, 1, 1107),
        intArrayOf(4, 0, 0, -1, 1021),
        intArrayOf(4, 0, -1, 1, 833),
        intArrayOf(0, 0, 1, -3, 777),
        intArrayOf(4, 0, -2, 1, 671),
        intArrayOf(2, 0, 0, -3, 607),
        intArrayOf(2, 0, 2, -1, 596),
        intArrayOf(2, -1, 1, -1, 491),
        intArrayOf(2, 0, -2, 1, -451),
        intArrayOf(0, 0, 3, -1, 439),
        intArrayOf(2, 0, 2, 1, 422),
        intArrayOf(2, 0, -3, -1, 421),
        intArrayOf(2, 1, -1, 1, -366),
        intArrayOf(2, 1, 0, 1, -351),
        intArrayOf(4, 0, 0, 1, 331),
        intArrayOf(2, -1, 1, 1, 315),
        intArrayOf(2, -2, 0, -1, 302),
        intArrayOf(0, 0, 1, 3, -283),
        intArrayOf(2, 1, 1, -1, -229),
        intArrayOf(1, 1, 0, -1, 223),
        intArrayOf(1, 1, 0, 1, 223),
        intArrayOf(0, 1, -2, -1, -220),
        intArrayOf(2, 1, -1, -1, -220),
        intArrayOf(1, 0, 1, 1, -185),
        intArrayOf(2, -1, -2, -1, 181),
        intArrayOf(0, 1, 2, 1, -177),
        intArrayOf(4, 0, -2, -1, 176),
        intArrayOf(4, -1, -1, -1, 166),
        intArrayOf(1, 0, 1, -1, -164),
        intArrayOf(4, 0, 1, -1, 132),
        intArrayOf(1, 0, -1, -1, -119),
        intArrayOf(4, -1, 0, -1, 115),
        intArrayOf(2, -2, 0, 1, 107)
    )

    /** Geocentric J2000 equatorial position of the Moon in km. */
    fun geocentricJ2000Km(jd: Double): Vec3 {
        val t = (jd - Astro.J2000_JD) / 36_525.0
        val t2 = t * t; val t3 = t2 * t; val t4 = t3 * t

        val lp = 218.3164477 + 481267.88123421 * t - 0.0015786 * t2 + t3 / 538841.0 - t4 / 65194000.0
        val d = 297.8501921 + 445267.1114034 * t - 0.0018819 * t2 + t3 / 545868.0 - t4 / 113065000.0
        val m = 357.5291092 + 35999.0502909 * t - 0.0001536 * t2 + t3 / 24490000.0
        val mp = 134.9633964 + 477198.8675055 * t + 0.0087414 * t2 + t3 / 69699.0 - t4 / 14712000.0
        val f = 93.2720950 + 483202.0175233 * t - 0.0036539 * t2 - t3 / 3526000.0 + t4 / 863310000.0
        val a1 = 119.75 + 131.849 * t
        val a2 = 53.09 + 479264.290 * t
        val a3 = 313.45 + 481266.484 * t
        // Eccentricity of Earth's orbit shrinks the terms that involve the Sun's mean anomaly.
        val e = 1.0 - 0.002516 * t - 0.0000074 * t2

        fun rad(deg: Double) = Math.toRadians(deg)
        fun eFactor(mMultiple: Int) = when (abs(mMultiple)) { 1 -> e; 2 -> e * e; else -> 1.0 }

        var sumL = 0.0
        var sumR = 0.0
        for (c in LONGITUDE_DISTANCE) {
            val arg = rad(c[0] * d + c[1] * m + c[2] * mp + c[3] * f)
            val ef = eFactor(c[1])
            sumL += c[4] * ef * sin(arg)
            sumR += c[5] * ef * cos(arg)
        }
        var sumB = 0.0
        for (c in LATITUDE) {
            val arg = rad(c[0] * d + c[1] * m + c[2] * mp + c[3] * f)
            sumB += c[4] * eFactor(c[1]) * sin(arg)
        }

        // Additive terms (Venus, Jupiter and the flattening of the Earth).
        sumL += 3958 * sin(rad(a1)) + 1962 * sin(rad(lp - f)) + 318 * sin(rad(a2))
        sumB += -2235 * sin(rad(lp)) + 382 * sin(rad(a3)) +
            175 * sin(rad(a1 - f)) + 175 * sin(rad(a1 + f)) +
            127 * sin(rad(lp - mp)) - 115 * sin(rad(lp + mp))

        val lambda = rad(Astro.normalizeDeg(lp + sumL / 1e6)) // ecliptic longitude of date
        val beta = rad(sumB / 1e6)                             // ecliptic latitude of date
        val distanceKm = 385000.56 + sumR / 1000.0

        // Ecliptic-of-date Cartesian -> mean equator of date (rotate about X by the obliquity)
        val x = distanceKm * cos(beta) * cos(lambda)
        val yEcl = distanceKm * cos(beta) * sin(lambda)
        val zEcl = distanceKm * sin(beta)
        val eps = rad(meanObliquityDeg(t))
        val ofDate = Vec3(
            x,
            yEcl * cos(eps) - zEcl * sin(eps),
            yEcl * sin(eps) + zEcl * cos(eps)
        )
        return Pointing.precessDateToJ2000(ofDate, jd)
    }

    /** Mean obliquity of the ecliptic of date, degrees (Meeus 22.2). */
    private fun meanObliquityDeg(t: Double): Double {
        val arcsec = 21.448 - 46.8150 * t - 0.00059 * t * t + 0.001813 * t * t * t
        return 23.0 + 26.0 / 60.0 + arcsec / 3600.0
    }
}
