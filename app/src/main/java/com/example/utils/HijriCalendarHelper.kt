package com.example.utils

import java.util.Calendar

object HijriCalendarHelper {
    data class HijriDate(val day: Int, val monthName: String, val year: Int)

    val MONTH_NAMES = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qa'dah", "Dhu al-Hijjah"
    )

    fun gregorianToJulianDay(year: Int, month: Int, day: Int): Double {
        var Y = year
        var M = month
        if (M <= 2) {
            Y -= 1
            M += 12
        }
        val A = Y / 100
        val B = 2 - A + (A / 4)
        val JD = (365.25 * (Y + 4716)).toLong() + (30.6001 * (M + 1)).toInt() + day + B - 1524.5
        return JD
    }

    private fun isLeapYearIn30YearCycle(y: Int): Boolean {
        // Standard leap years in 30-year cycle
        return y == 2 || y == 5 || y == 7 || y == 10 || y == 13 || y == 16 || 
               y == 18 || y == 21 || y == 24 || y == 26 || y == 29
    }

    private fun getDaysInIslamicMonth(m: Int, isLeapYear: Boolean): Int {
        if (m == 12) {
            return if (isLeapYear) 30 else 29
        }
        return if (m % 2 == 1) 30 else 29
    }

    fun convertGregorianToHijri(year: Int, month: Int, day: Int): HijriDate {
        val jd = gregorianToJulianDay(year, month, day)
        // Adjust cycle calculations (epoch Friday July 16, 622 CE is JD 1948439.5)
        val jdMidnight = jd + 0.5
        val daysSinceEpoch = (jdMidnight - 1948439.5).toLong()

        var cycle = daysSinceEpoch / 10631
        var rem = daysSinceEpoch % 10631

        if (rem < 0) {
            rem += 10631
            cycle -= 1
        }

        var yearInCycle = 1
        for (y in 1..30) {
            val isLeap = isLeapYearIn30YearCycle(y)
            val daysInYear = if (isLeap) 355 else 354
            if (rem < daysInYear) {
                yearInCycle = y
                break
            }
            rem -= daysInYear
        }

        val hYear = (cycle * 30) + yearInCycle

        var hMonth = 1
        var tempDays = rem
        for (m in 1..12) {
            val daysInMonth = getDaysInIslamicMonth(m, isLeapYearIn30YearCycle(yearInCycle))
            if (tempDays < daysInMonth) {
                hMonth = m
                break
            }
            tempDays -= daysInMonth
        }
        val hDay = (tempDays + 1).toInt()

        return HijriDate(
            day = hDay,
            monthName = MONTH_NAMES.getOrElse(hMonth - 1) { "Muharram" },
            year = hYear.toInt()
        )
    }

    fun convertGregorianToHijri(calendar: Calendar, dayOffset: Int = 0): HijriDate {
        val cal = calendar.clone() as Calendar
        cal.add(Calendar.DAY_OF_YEAR, dayOffset)
        return convertGregorianToHijri(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    /**
     * Converts a Hijri date back to Gregorian (Calendar) via Meeus Julian Day conversion.
     * month is 1-indexed (1 = Muharram, ..., 12 = Dhu al-Hijjah)
     */
    fun convertHijriToGregorian(hYear: Int, hMonth: Int, hDay: Int): Calendar {
        val monthIdx = (hMonth - 1).coerceIn(0, 11)
        
        var precedingDays = 0
        for (m in 1..monthIdx) {
            val cycleYearIdx = ((hYear - 1) % 30) + 1
            precedingDays += getDaysInIslamicMonth(m, isLeapYearIn30YearCycle(cycleYearIdx))
        }

        val cycle = (hYear - 1) / 30
        val yearInCycle = hYear - 1 - cycle * 30
        
        // Days in cycle
        var daysInCycle = cycle * 10631L
        for (y in 1..yearInCycle) {
            val isLeap = isLeapYearIn30YearCycle(y)
            daysInCycle += if (isLeap) 355 else 354
        }
        val jd = hDay + precedingDays + daysInCycle + 1948439.5 - 0.5
        
        // Convert JD to Gregorian date
        val z = (jd + 0.5).toLong()
        val f = (jd + 0.5) - z
        var a = z
        if (z >= 2299161) {
            val alpha = ((z - 1867216.25) / 36524.25).toLong()
            a = z + 1 + alpha - (alpha / 4)
        }
        val b = a + 1524
        val c = ((b - 122.1) / 365.25).toLong()
        val d = (365.25 * c).toLong()
        val e = ((b - d) / 30.6001).toLong()

        val day = (b - d - (30.6001 * e).toLong() + f).toInt()
        val month = (if (e < 14) e - 1 else e - 13).toInt()
        val year = (if (month > 2) c - 4716 else c - 4715).toInt()

        return Calendar.getInstance().apply {
            clear()
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, (month - 1).coerceIn(0, 11))
            set(Calendar.DAY_OF_MONTH, day.coerceIn(1, 31))
        }
    }
}
