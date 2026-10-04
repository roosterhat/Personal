package com.example.armcontrol.ephemeris

/**
 * Offline planet and asteroid positions.
 *
 * Planets use the JPL "Approximate Positions of the Planets" Keplerian elements
 * (valid 1800-2050, roughly arcminute-level accuracy), so they need no network and no cache.
 * Asteroids propagate the osculating elements fetched from JPL SBDB as a plain two-body orbit.
 *
 * Earth's position is taken from the Earth-Moon barycentre row of the table, which is within a
 * few thousand km of the real Earth. That is invisible for planets and asteroids. The Moon is
 * the exception and is computed separately by [MoonEphemeris] (listed here as "moon").
 */
object SolarSystem {

    private class Row(
        val a0: Double, val aRate: Double,
        val e0: Double, val eRate: Double,
        val i0: Double, val iRate: Double,
        val l0: Double, val lRate: Double,
        val peri0: Double, val periRate: Double,
        val node0: Double, val nodeRate: Double
    )

    // a [AU], e, I [deg], L [deg], longitude of perihelion [deg], longitude of node [deg]; each with a per-century rate.
    private val rows: Map<String, Row> = mapOf(
        "mercury" to Row(0.38709927, 0.00000037, 0.20563593, 0.00001906, 7.00497902, -0.00594749, 252.25032350, 149472.67411175, 77.45779628, 0.16047689, 48.33076593, -0.12534081),
        "venus" to Row(0.72333566, 0.00000390, 0.00677672, -0.00004107, 3.39467605, -0.00078890, 181.97909950, 58517.81538729, 131.60246718, 0.00268329, 76.67984255, -0.27769418),
        "earth" to Row(1.00000261, 0.00000562, 0.01671123, -0.00004392, -0.00001531, -0.01294668, 100.46457166, 35999.37244981, 102.93768193, 0.32327364, 0.0, 0.0),
        "mars" to Row(1.52371034, 0.00001847, 0.09339410, 0.00007882, 1.84969142, -0.00813131, -4.55343205, 19140.30268499, -23.94362959, 0.44441088, 49.55953891, -0.29257343),
        "jupiter" to Row(5.20288700, -0.00011607, 0.04838624, -0.00013253, 1.30439695, -0.00183714, 34.39644051, 3034.74612775, 14.72847983, 0.21252668, 100.47390909, 0.20469106),
        "saturn" to Row(9.53667594, -0.00125060, 0.05386179, -0.00050991, 2.48599187, 0.00193609, 49.95424423, 1222.49362201, 92.59887831, -0.41897216, 113.66242448, -0.28867794),
        "uranus" to Row(19.18916464, -0.00196176, 0.04725744, -0.00004397, 0.77263783, -0.00242939, 313.23810451, 428.48202785, 170.95427630, 0.40805281, 74.01692503, 0.04240589),
        "neptune" to Row(30.06992276, 0.00026291, 0.00859048, 0.00005105, 1.77004347, 0.00035372, -55.12002969, 218.45945325, 44.96476227, -0.32241464, 131.78422574, -0.00508664)
    )

    val planetEntries: List<EphemerisEntry.Planet> =
        listOf("moon", "mercury", "venus", "mars", "jupiter", "saturn", "uranus", "neptune").map { id ->
            EphemerisEntry.Planet(id, id.replaceFirstChar { it.uppercase() })
        }

    private fun heliocentricEcliptic(key: String, jd: Double): Vec3 {
        val r = rows.getValue(key)
        val t = Astro.centuriesSinceJ2000(jd)
        val a = r.a0 + r.aRate * t
        val e = r.e0 + r.eRate * t
        val inc = r.i0 + r.iRate * t
        val meanLon = r.l0 + r.lRate * t
        val periLon = r.peri0 + r.periRate * t
        val node = r.node0 + r.nodeRate * t
        return Astro.heliocentricEcliptic(
            a = a, e = e, incDeg = inc,
            raanDeg = node,
            argPeriDeg = periLon - node,
            meanAnomalyDeg = meanLon - periLon
        )
    }

    /** Heliocentric ecliptic position of the Earth, in AU. */
    fun earthHeliocentric(jd: Double): Vec3 = heliocentricEcliptic("earth", jd)

    /** Geocentric equatorial J2000 position of a planet, in km. [id] is e.g. "mars". */
    fun planetGeocentricKm(id: String, jd: Double): Vec3 {
        if (id == "moon") return MoonEphemeris.geocentricJ2000Km(jd)
        val rel = heliocentricEcliptic(id, jd) - earthHeliocentric(jd)
        return Astro.eclipticToEquatorial(rel) * Astro.AU_KM
    }

    /** Geocentric equatorial J2000 position of an asteroid, in km. */
    fun asteroidGeocentricKm(el: OrbitalElements, jd: Double): Vec3 {
        val meanAnomaly = el.meanAnomaly + el.meanMotion * (jd - el.epochJd)
        val helio = Astro.heliocentricEcliptic(
            a = el.a, e = el.e, incDeg = el.inclination,
            raanDeg = el.raan, argPeriDeg = el.argPeriapsis,
            meanAnomalyDeg = meanAnomaly
        )
        val rel = helio - earthHeliocentric(jd)
        return Astro.eclipticToEquatorial(rel) * Astro.AU_KM
    }
}
