package com.example.utils

import kotlin.math.*

object PrayerTimeCalculator {

    data class PrayerTimes(
        val imsakString: String,
        val fajrString: String,
        val sunriseString: String,
        val dhuhrString: String,
        val asrString: String,
        val sunsetString: String,
        val maghribString: String,
        val ishaString: String,
        val midnightString: String
    )

    fun calculateTimes(
        year: Int,
        month: Int,
        day: Int,
        latitude: Double,
        longitude: Double,
        timezone: Double
    ): PrayerTimes {
        val m = month.toDouble()
        val y = year.toDouble()
        val d = day.toDouble()

        // Julian Date relative to epoch J2000.0
        val jd = 367.0 * y - (7.0 * (y + (m + 9.0) / 12.0).toInt()) / 4.0 + (275.0 * m) / 9.0 + d - 730531.5

        // Solar Mean Anomaly (degrees)
        var g = 357.529 + 0.98560028 * jd
        g = normalizeDegrees(g)

        // Solar Mean Longitude (degrees)
        var q = 280.459 + 0.98564736 * jd
        q = normalizeDegrees(q)

        // Ecliptic Longitude (degrees)
        var L = q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2.0 * g))
        L = normalizeDegrees(L)

        // Obliquity of Ecliptic (degrees)
        val e = 23.439 - 0.00000036 * jd

        // Sun's Declination (radians)
        val declination = asin(sin(Math.toRadians(e)) * sin(Math.toRadians(L)))

        // Right Ascension (degrees)
        var ra = Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(L)), cos(Math.toRadians(L))))
        ra = normalizeDegrees(ra) / 15.0 // in hours

        // Equation of Time (hours)
        val qHour = q / 15.0
        var eot = qHour - ra
        if (eot > 20.0) eot -= 24.0
        if (eot < -20.0) eot += 24.0

        // Solar Transit (Noon in local hours)
        val transit = 12.0 + timezone - longitude / 15.0 - eot

        // Hour Angles for Fajr, Sunrise, Sunset, Maghrib, Isha
        // Shia Ithna Ashari Angles:
        // Fajr: -16.0 degrees
        // Sunrise/Sunset: -0.833 degrees (refraction + size)
        // Maghrib: -4.0 degrees (vanishing of eastern redness)
        // Isha: -14.0 degrees
        val fajrHA = computeHourAngle(latitude, declination, -16.0)
        val sunriseHA = computeHourAngle(latitude, declination, -0.833)
        val sunsetHA = computeHourAngle(latitude, declination, -0.833)
        val maghribHA = computeHourAngle(latitude, declination, -4.0)
        val ishaHA = computeHourAngle(latitude, declination, -14.0)

        // Calculate Times
        val imsakTime = formatTime(transit - fajrHA - (10.0 / 60.0)) // 10 minutes precaution before Fajr
        val fajrTime = formatTime(transit - fajrHA)
        val sunriseTime = formatTime(transit - sunriseHA)
        val dhuhrTime = formatTime(transit)

        // Asr Calculation based on shadow ratio.
        // For Shia, shadow ratio for Asr premium is typically 1 (same as Shafi'i).
        val latRad = Math.toRadians(latitude)
        val decRad = declination
        val absAngleDiff = abs(latRad - decRad)
        val asrAltRad = atan(1.0 / (1.0 + tan(absAngleDiff)))
        val asrAltDeg = Math.toDegrees(asrAltRad)
        val asrHA = computeHourAngle(latitude, declination, asrAltDeg)
        val asrTime = formatTime(transit + asrHA)

        val sunsetTime = formatTime(transit + sunsetHA)
        val maghribTime = formatTime(transit + maghribHA)
        val ishaTime = formatTime(transit + ishaHA)

        // Shia Midnight (Nisfe Shab): Halfway between Sunset and next Fajr
        val sunsetHrs = transit + sunsetHA
        val fajrHrs = transit - fajrHA
        val midnightHrs = sunsetHrs + (fajrHrs + 24.0 - sunsetHrs) / 2.0
        val midnightTime = formatTime(midnightHrs)

        return PrayerTimes(
            imsakString = imsakTime,
            fajrString = fajrTime,
            sunriseString = sunriseTime,
            dhuhrString = dhuhrTime,
            asrString = asrTime,
            sunsetString = sunsetTime,
            maghribString = maghribTime,
            ishaString = ishaTime,
            midnightString = midnightTime
        )
    }

    private fun computeHourAngle(latDeg: Double, declinationRad: Double, altDeg: Double): Double {
        val latRad = Math.toRadians(latDeg)
        val altRad = Math.toRadians(altDeg)
        val cosHA = (sin(altRad) - sin(latRad) * sin(declinationRad)) / (cos(latRad) * cos(declinationRad))
        if (cosHA < -1.0) return 12.0 // Midnight sun
        if (cosHA > 1.0) return 0.0 // Polar night
        val haRad = acos(cosHA)
        return Math.toDegrees(haRad) / 15.0
    }

    private fun normalizeDegrees(deg: Double): Double {
        var d = deg % 360.0
        if (d < 0.0) d += 360.0
        return d
    }

    private fun formatTime(hours: Double): String {
        var h = hours % 24.0
        if (h < 0.0) h += 24.0
        val totalMinutes = (h * 60.0).roundToInt()
        val mins = totalMinutes % 60
        val hrs = (totalMinutes / 60) % 24
        return String.format("%02d:%02d", hrs, mins)
    }

    private fun Double.roundToInt(): Int = (this + 0.5).toInt()
}
