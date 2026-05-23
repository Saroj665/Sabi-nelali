package com.example.utils

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class NepaliDate(
    val year: Int,
    val month: Int,     // 1-indexed (1 = Baisakh)
    val day: Int,
    val dayOfWeekNp: String,
    val dayOfWeekEn: String
) {
    fun formatNp(): String {
        return "${NepaliNumberConverter.toNepali(year)} ${NepaliCalendar.MONTH_NAMES_NP[month - 1]} ${NepaliNumberConverter.toNepali(day)}"
    }

    fun formatEn(): String {
        return "$year ${NepaliCalendar.MONTH_NAMES_EN[month - 1]} $day"
    }
}

data class NepalFestival(
    val nameNp: String,
    val nameEn: String,
    val monthBs: Int,
    val dayBs: Int,
    val descriptionNp: String,
    val descriptionEn: String,
    val dateAd: LocalDate // Target date in 2026 AD (which is 2083 BS)
)

object NepaliNumberConverter {
    private val NP_DIGITS = charArrayOf('०', '१', '२', '३', '४', '५', '६', '७', '८', '९')

    fun toNepali(number: Int): String {
        return number.toString()
            .map { if (it.isDigit()) NP_DIGITS[it - '0'] else it }
            .joinToString("")
    }

    fun toNepali(text: String): String {
        return text.map { if (it.isDigit()) NP_DIGITS[it - '0'] else it }.joinToString("")
    }
}

object NepaliCalendar {
    val MONTH_NAMES_NP = listOf(
        "वैशाख", "जेठ", "असार", "साउन", "भदौ", "असोज",
        "कात्तिक", "मंसिर", "पुस", "माघ", "फागुन", "चैत"
    )

    val MONTH_NAMES_EN = listOf(
        "Baisakh", "Jestha", "Ashadh", "Shrawan", "Bhadra", "Ashwin",
        "Kartik", "Mangsir", "Poush", "Magh", "Falgun", "Chaitra"
    )

    val DAYS_NP = listOf(
        "आइतबार", "सोमवार", "मंगलबार", "बुधबार", "बिहीबार", "शुक्रबार", "शनिबार"
    )

    val DAYS_EN = listOf(
        "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    )

    // Nepali Month lengths from 2080 to 2085 BS
    private val MONTH_DAYS_MAP = mapOf(
        2080 to listOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 30),
        2081 to listOf(31, 31, 32, 32, 31, 30, 30, 30, 30, 29, 29, 30),
        2082 to listOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 30),
        2083 to listOf(31, 31, 32, 32, 31, 30, 30, 30, 30, 29, 29, 30),
        2084 to listOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 30),
        2085 to listOf(31, 31, 32, 32, 31, 30, 30, 30, 30, 29, 29, 30)
    )

    // Anchor Date: 2081 Baisakh 1 corresponds to April 13, 2024 AD
    private val ANCHOR_AD = LocalDate.of(2024, 4, 13)
    private const val ANCHOR_BS_YEAR = 2081
    private const val ANCHOR_BS_MONTH = 1
    private const val ANCHOR_BS_DAY = 1

    /**
     * Converts Gregorian date (AD) to Bikram Sambat (BS)
     */
    fun convertAdToBs(date: LocalDate): NepaliDate {
        var daysDiff = ChronoUnit.DAYS.between(ANCHOR_AD, date)
        
        var bsYear = ANCHOR_BS_YEAR
        var bsMonth = ANCHOR_BS_MONTH
        var bsDay = ANCHOR_BS_DAY

        if (daysDiff >= 0) {
            // Forward conversion
            while (daysDiff > 0) {
                val monthsDays = MONTH_DAYS_MAP[bsYear] ?: listOf(30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30)
                val daysInCurrentMonth = monthsDays[bsMonth - 1]
                val remainingInMonth = daysInCurrentMonth - bsDay + 1

                if (daysDiff >= remainingInMonth) {
                    daysDiff -= remainingInMonth
                    bsDay = 1
                    bsMonth++
                    if (bsMonth > 12) {
                        bsMonth = 1
                        bsYear++
                    }
                } else {
                    bsDay += daysDiff.toInt()
                    daysDiff = 0
                }
            }
        } else {
            // Backward conversion
            daysDiff = -daysDiff
            while (daysDiff > 0) {
                if (bsDay > 1) {
                    val toSubtract = minOf(daysDiff, (bsDay - 1).toLong())
                    bsDay -= toSubtract.toInt()
                    daysDiff -= toSubtract
                } else {
                    bsMonth--
                    if (bsMonth < 1) {
                        bsMonth = 12
                        bsYear--
                    }
                    val monthsDays = MONTH_DAYS_MAP[bsYear] ?: listOf(30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30)
                    bsDay = monthsDays[bsMonth - 1]
                    daysDiff--
                }
            }
        }

        // Calculate days of the week index (Sunday = 1, Saturday = 7)
        val dayOfWeekIndex = date.dayOfWeek.value // 1 = Monday, 7 = Sunday
        val indexShifted = if (dayOfWeekIndex == 7) 0 else dayOfWeekIndex // Sunday -> 0, Monday -> 1, ..., Sat -> 6
        
        return NepaliDate(
            year = bsYear,
            month = bsMonth,
            day = bsDay,
            dayOfWeekNp = DAYS_NP[indexShifted],
            dayOfWeekEn = DAYS_EN[indexShifted]
        )
    }

    /**
     * Approximate reverse conversion for calculator verification
     */
    fun convertBsToAd(year: Int, month: Int, day: Int): LocalDate {
        if (year < 2080 || year > 2085 || month < 1 || month > 12 || day < 1) {
            return LocalDate.of(2026, 1, 1) // Default fallback
        }
        val monthDays = MONTH_DAYS_MAP[year] ?: return LocalDate.of(2026, 1, 1)
        if (day > monthDays[month - 1]) return LocalDate.of(2026, 1, 1)

        // Calculate total days from ANCHOR_BS to requested BS date
        var daysOffset = 0L

        if (year >= ANCHOR_BS_YEAR) {
            // Going forward from April 13, 2024
            var curYear = ANCHOR_BS_YEAR
            var curMonth = ANCHOR_BS_MONTH
            
            while (curYear < year || curMonth < month) {
                val dm = MONTH_DAYS_MAP[curYear] ?: listOf(30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30)
                daysOffset += dm[curMonth - 1]
                curMonth++
                if (curMonth > 12) {
                    curMonth = 1
                    curYear++
                }
            }
            daysOffset += (day - ANCHOR_BS_DAY)
            return ANCHOR_AD.plusDays(daysOffset)
        } else {
            // Going backward from April 13, 2024
            var curYear = year
            var curMonth = month
            
            while (curYear < ANCHOR_BS_YEAR || curMonth < ANCHOR_BS_MONTH) {
                val dm = MONTH_DAYS_MAP[curYear] ?: listOf(30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30)
                daysOffset += dm[curMonth - 1]
                curMonth++
                if (curMonth > 12) {
                    curMonth = 1
                    curYear++
                }
            }
            daysOffset += (ANCHOR_BS_DAY - day)
            return ANCHOR_AD.minusDays(daysOffset)
        }
    }

    // Major static Nepalese festivals for year 2026 / 2083 BS
    val festivals2083 = listOf(
        NepalFestival("नयाँ वर्ष (बिस्केट जात्रा)", "Nepali New Year (Bisket Jatra)", 1, 1, "नयाँ वर्ष २०८३ को पहिलो दिन। भक्तपुर र नेपालभर उत्सव मनाइन्छ।", "Celebration of Nepali New Year 2083 and Bisket Jatra in Bhaktapur.", LocalDate.of(2026, 4, 14)),
        NepalFestival("बुद्ध जयन्ती", "Buddha Jayanti", 2, 17, "गौतम बुद्धको जन्म, ज्ञान प्राप्ति र महापरिनिर्वाण भएको पवित्र दिन।", "Celebration of Lord Buddha's Birth, Enlightenment, and Mahaparinirvana.", LocalDate.of(2026, 5, 30)),
        NepalFestival("तीज (हरितालिका)", "Haritalika Teej", 5, 29, "नेपाली महिलाहरूले व्रत बसी शिवजीको पूजा आराधना गर्ने लोकप्रिय पर्व।", "Fast and prayer festival deeply celebrated by Hindu women for conjugal bliss.", LocalDate.of(2026, 9, 11)),
        NepalFestival("दशैं (घटस्थापना)", "Dashain (Ghatasthapana)", 6, 16, "बडादशैंको प्रारम्भ, घडा स्थापना गरी जमरा राखिने पवित्र दिन।", "Commencement of Dashain, the biggest Nepalese Hindu festival, sowing barley jamara seeds.", LocalDate.of(2026, 9, 28)),
        NepalFestival("फूलपाती (दशैं सप्तमी)", "Fulpati (Dashain Day 7)", 6, 22, "दशैंको फूलपाती भित्र्याउने दिन, धादिङबाट डोली काठमाडौं दरवार ल्याइन्छ।", "The seventh day of Dashain where sacred floral offerings are brought to Kathmandu.", LocalDate.of(2026, 10, 4)),
        NepalFestival("विजया दशमी (टीका)", "Vijaya Dashami (Tika)", 6, 25, "बडादशैंको मुख्य दिन, मान्यजनहरूबाट टीका, जमरा र आर्शिवाद ग्रहण गरिने।", "The main day of Dashain; elders apply Tika, Jamara, and offer blessings.", LocalDate.of(2026, 10, 7)),
        NepalFestival("तिहार (लक्ष्मी पूजा)", "Tihar (Laxmi Puja)", 7, 24, "यमपञ्चक अन्तर्गत धनधान्यकी देवी महालक्ष्मीको भव्य पूजा र दीप प्रज्वलन।", "Festival of lights; grand worshipping of Goddess Laxmi, the deity of wealth and prosperity.", LocalDate.of(2026, 11, 5)),
        NepalFestival("भाईटीका", "Bhai Tika", 7, 26, "दिदीबहिनीहरूले दाजुभाईको लामो आयु र सुस्वास्थ्यको कामना गर्दै सप्तरङ्गी टीका लगाइदिने।", "Final day of Tihar celebrating the sacred bond between sisters and brothers.", LocalDate.of(2026, 11, 7)),
        NepalFestival("छठ पर्व", "Chhath Parva", 7, 27, "नदी र पोखरीका किनारमा अस्ताउँदो र उदाउँदो सूर्यदेवको पूजा आराधना।", "Solar deity worship festival especially celebrated on riverbanks and ponds.", LocalDate.of(2026, 11, 8)),
        NepalFestival("उधौली पर्व (योमरी पुन्ही)", "Yomari Punhi (Udhauli)", 8, 29, "धानको फसल भित्र्याएको खुसीयालीमा नेवार र किराँत समुदायमा लोकप्रिय उत्सव।", "Post-harvest thanksgiving festival celebrated by NeWar (Yomari) and Kirant (Udhauli) communities.", LocalDate.of(2026, 12, 10)),
        NepalFestival("महा शिवरात्रि", "Maha Shivaratri", 11, 14, "भगवान शिवको आराधना गरिने पवित्र रात, पशुपतिनाथ मन्दिरमा लाखौं दर्शनार्थी।", "Night of Lord Shiva; celebrated nationwide with massive pilgrimage at Pashupatinath temple.", LocalDate.of(2027, 2, 23))
    )
}
