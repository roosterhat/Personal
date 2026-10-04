package com.example.armcontrol.ephemeris

import kotlin.math.sqrt

/**
 * Cartesian vector. Every position this package returns is **geocentric** (origin at the
 * center of the Earth), in **kilometres**, in an equatorial frame:
 *  - stars, planets, asteroids: J2000 equatorial (+Z = celestial north pole, +X = vernal equinox)
 *  - satellites: the TLE's own frame (TEME, equator/equinox of date), used here as-is
 * See the EphemerisRepository KDoc for what those frames mean for pointing.
 */
data class Vec3(val x: Double, val y: Double, val z: Double) {
    operator fun plus(o: Vec3) = Vec3(x + o.x, y + o.y, z + o.z)
    operator fun minus(o: Vec3) = Vec3(x - o.x, y - o.y, z - o.z)
    operator fun times(s: Double) = Vec3(x * s, y * s, z * s)
    val norm: Double get() = sqrt(x * x + y * y + z * z)
}

enum class ObjectType(val label: String) {
    SATELLITE("Satellite"),
    PLANET("Planet"),
    ASTEROID("Asteroid"),
    STAR("Star")
}

/** Heliocentric ecliptic-J2000 Keplerian elements at [epochJd]. Angles in degrees, [a] in AU. */
data class OrbitalElements(
    val epochJd: Double,
    val a: Double,
    val e: Double,
    val inclination: Double,
    val raan: Double,
    val argPeriapsis: Double,
    val meanAnomaly: Double,
    val meanMotion: Double // degrees per day
)

data class Tle(val name: String, val line1: String, val line2: String)

/**
 * One searchable object plus whatever is needed to compute where it is.
 * [id] / [name] / [type] line up with the search drawer's CelestialObject(id, name, type).
 */
sealed class EphemerisEntry {
    abstract val id: String
    abstract val name: String
    abstract val type: ObjectType

    /** Stars barely move, so the geocentric position is computed once at fetch time and stored. */
    data class Star(
        override val id: String,
        override val name: String,
        val magnitude: Double,
        val positionKm: Vec3
    ) : EphemerisEntry() {
        override val type: ObjectType get() = ObjectType.STAR
    }

    /** Planet elements are built into [SolarSystem], so a planet entry is just an id. */
    data class Planet(
        override val id: String,
        override val name: String
    ) : EphemerisEntry() {
        override val type: ObjectType get() = ObjectType.PLANET
    }

    data class Asteroid(
        override val id: String,
        override val name: String,
        val elements: OrbitalElements
    ) : EphemerisEntry() {
        override val type: ObjectType get() = ObjectType.ASTEROID
    }

    data class Satellite(
        override val id: String,
        override val name: String,
        val tle: Tle
    ) : EphemerisEntry() {
        override val type: ObjectType get() = ObjectType.SATELLITE
    }
}
