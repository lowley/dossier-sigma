package lorry.folder.items.dossiersigma.ui.IndexBar.utilities

import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.headless.domain.Item
import lorry.folder.items.dossiersigma.ui.sigma.SortingCriterion
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.Period
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.stream.IntStream

fun List<Item>.toIndexBarItemInfoList(
    sorting: SortingCriterion
): List<IndexBarItemInfo> {

    return when (sorting) {
        SortingCriterion.ByNameAsc ->
            createInfoListByNameAsc(this)

        SortingCriterion.ByDateDesc ->
            createInfoListByDateDesc(this)
    }
}


fun createInfoListByNameAsc(items: List<Item>): List<IndexBarItemInfo> {

    val sortedItemNames = items
        .map { it.name }
        .sortedBy { it }

    val letters = 'A'..'Z'

    val filteredLetters = letters.filter { letter ->
        sortedItemNames.any { it.uppercase().startsWith(letter) }
    }

    val result = filteredLetters.map { letter ->
        IndexBarItemInfo(
            content = Content.Text("$letter"),
            contextualHelp = "Items starting with $letter"
        )
    }

    return result
}

fun createInfoListByDateDesc(items: List<Item>): List<IndexBarItemInfo> {

    if (items.size < 2)
        return emptyList()

    val sortedItemsMillis = items
        .map { it.modificationDate }
        .sortedByDescending { it }

    val newestMillis = sortedItemsMillis.first()
    val oldestMillis = sortedItemsMillis.last()

    val zone = ZoneId.systemDefault()

    val newestInstant = Instant.ofEpochMilli(newestMillis)
    val newestDate = newestInstant.atZone(zone).toLocalDate()

    val oldestInstant = Instant.ofEpochMilli(oldestMillis)
    val oldestDate = oldestInstant.atZone(zone).toLocalDate()

    val gap1 = Period.between(oldestDate, newestDate)
    val gapYears = gap1.years
    val gapDays = ChronoUnit.DAYS.between(oldestDate, newestDate)
    val gapWeeks = ChronoUnit.WEEKS.between(oldestDate, newestDate)
    val gapMonths = gap1.months
    val gapHours = ChronoUnit.HOURS.between(oldestInstant, newestInstant)
    val gapMinutes = ChronoUnit.MINUTES.between(oldestInstant, newestInstant)

    if (gapYears > 0) {
        //* grosse unité: années, petite unité: mois
        val allYears = IntStream.rangeClosed(oldestDate.year, newestDate.year)
        val filteredYears = allYears.filter { year ->
            year in sortedItemsMillis.map { itemMillis ->
                Instant.ofEpochMilli(itemMillis).atZone(zone).toLocalDate().year
            }
        }

        val allMonths = monthsBetween(oldestDate, newestDate)
        val filteredMonths = allMonths.filter { yearMonth ->
            yearMonth in sortedItemsMillis.map { itemMillis ->
                val year = Instant.ofEpochMilli(itemMillis).atZone(zone).toLocalDate().year
                val month = Instant.ofEpochMilli(itemMillis).atZone(zone).toLocalDate().monthValue
                val yearMonth = YearMonth.of(year, month)
                yearMonth
            }
                .sortedBy { month -> month }
        }

        //construire composables
        val result: List<IndexBarItemInfo> = emptyList()
        filteredMonths.forEach { yearMonth ->
            if (yearMonth.month == Month.JANUARY) {
                result.plus(
                    createIndexBarItemInfo(
                        contextualHelp = "${yearMonth.year}",
                        infoType = InfoType.MAJOR,
                        endDate = oldestDate
                    )
                )
            } else {
                result.plus(
                    createIndexBarItemInfo(
                        contextualHelp = "${
                            yearMonth.month.getDisplayName(
                                TextStyle.SHORT,
                                Locale.getDefault()
                            )
                        } ${yearMonth.year}",
                        infoType = InfoType.MINOR,
                        endDate = oldestDate
                    )
                )
            }
        }

        return result.reversed()

    } else if (gapMonths > 0) {
        val allMonths = monthsBetween(oldestDate, newestDate)
        val filteredMonths = allMonths.filter { yearMonth ->
            yearMonth in sortedItemsMillis.map { itemMillis ->
                val year = Instant.ofEpochMilli(itemMillis).atZone(zone).toLocalDate().year
                val month = Instant.ofEpochMilli(itemMillis).atZone(zone).toLocalDate().monthValue
                val yearMonth = YearMonth.of(year, month)
                yearMonth
            }
                .sortedBy { month -> month }
        }

        val startWeekDay = oldestDate.withDayOfMonth(1)
        var startDayOfStartWeek: LocalDate = startWeekDay
        for (j in 0..6) {
            val tested = startWeekDay.minusDays(j.toLong())
            if (tested.dayOfWeek == DayOfWeek.MONDAY)
                startDayOfStartWeek = tested
        }

        var i = 0
        val result: MutableList<IndexBarItemInfo> = mutableListOf()
        do {
            val beginningOfWeek = startDayOfStartWeek.plusDays(i.toLong() * 7)
            if (beginningOfWeek.isBefore(newestDate)) {
                //test si nouveau mois
                val day = beginningOfWeek.dayOfMonth

                val containsNewMonth = (0..6)
                    .takeWhile { addition ->
                        addition == 0 ||
                        beginningOfWeek.plusDays(addition.toLong()).dayOfWeek != DayOfWeek.MONDAY }
                    .any { addition ->
                        beginningOfWeek.plusDays(addition.toLong()).dayOfMonth == 1
                    } || (-1..-6)
                    .takeWhile { soustraction -> beginningOfWeek.minusDays(soustraction.toLong()).dayOfWeek != DayOfWeek.MONDAY }
                    .any { soustraction ->
                        beginningOfWeek.minusDays(soustraction.toLong()).dayOfMonth == 1
                    }

                if (containsNewMonth) {
                    result.add(
                        createIndexBarItemInfo(
                            contextualHelp = "${
                                beginningOfWeek.month.getDisplayName(
                                    TextStyle.FULL_STANDALONE,
                                    Locale.getDefault()
                                )
                            }\n${beginningOfWeek.dayOfMonth} -> ${beginningOfWeek.plusDays(6).dayOfMonth}",
                            infoType = InfoType.MAJOR,
                            endDate = beginningOfWeek.plusDays(6)
                        )
                    )

                } else result.add(
                    createIndexBarItemInfo(
                        contextualHelp = "semaine ${beginningOfWeek.dayOfMonth / 7 + 1}\n" +
                                "${beginningOfWeek.dayOfMonth} -> ${beginningOfWeek.plusDays(6).dayOfMonth}",
                        infoType = InfoType.MINOR,
                        endDate = beginningOfWeek.plusDays(6)
                    )
                )
            }

            i++
        } while (i <= gapWeeks + 5)

        return result.toList().reversed()

    } else if (gapWeeks > 0) {


    } else if (gapDays > 0) {


    } else if (gapHours > 0) {


    } else {


    }

    return emptyList()

}

fun createIndexBarItemInfo(
    contextualHelp: String,
    infoType: InfoType,
    endDate: LocalDate,
): IndexBarItemInfo {
    return IndexBarItemInfo(
        infoType = infoType,
        contextualHelp = contextualHelp,
        content = Content.Icon(
            icon = R.drawable.rond,
        ),
        endDate = endDate

//            { goTo ->
//            Icon(
//                modifier = Modifier
//                    .pointerInput(Unit) {
//                        detectTapGestures(
//                            onTap = {
//                                goTo()
//                            })
//                    },
//                painter = painterResource(id = R.drawable.rond),
//                tint = iconColor,
//                contentDescription = null
//            )
//        },

    )
}

fun monthsBetween(start: LocalDate, end: LocalDate): List<YearMonth> {
    val startYM = YearMonth.from(start)
    val endYM = YearMonth.from(end)

    val months = mutableListOf<YearMonth>()
    var current = startYM
    while (!current.isAfter(endYM)) {
        months.add(current)
        current = current.plusMonths(1)
    }
    return months
}