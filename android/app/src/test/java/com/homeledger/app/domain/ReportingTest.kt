package com.homeledger.app.domain

import com.homeledger.app.data.CategoryEntity
import com.homeledger.app.data.ExpenseEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ReportingTest {
    private val zone = ZoneId.of("UTC")
    private fun expense(id: String, day: String, amount: Long, category: String = "home") = ExpenseEntity(
        id = id,
        amountMinor = amount,
        categoryId = category,
        note = id,
        occurredAt = LocalDate.parse(day).atStartOfDay(zone).toInstant().toEpochMilli(),
    )

    @Test fun monthWindowIncludesWholeMonth() {
        val range = Reporting.window(PeriodKind.MONTH, LocalDate.parse("2026-10-10"))
        assertEquals(LocalDate.parse("2026-10-01"), range.start)
        assertEquals(LocalDate.parse("2026-10-31"), range.endInclusive)
    }

    @Test fun weekWindowHonorsConfiguredWeekStart() {
        val sundayStart = Reporting.window(PeriodKind.WEEK, LocalDate.parse("2026-10-14"), weekStartsOn = 7)
        assertEquals(LocalDate.parse("2026-10-11"), sundayStart.start)
        assertEquals(LocalDate.parse("2026-10-17"), sundayStart.endInclusive)
    }

    @Test fun summarySumsMinorUnitsAndComputesCategoryShares() {
        val groceries = CategoryEntity("food", "Groceries", "cart", "#E8F1DC", true)
        val home = CategoryEntity("home", "Home", "home", "#E6ECF6", true)
        val rows = listOf(expense("one", "2026-10-02", 35_000), expense("two", "2026-10-03", 62_050, "food"), expense("outside", "2026-09-30", 90_000))
        val summary = Reporting.summarize(rows, listOf(home, groceries), DateWindow(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-10-31")), zone = zone)
        assertEquals(97_050L, summary.totalMinor)
        assertEquals(35_000L, summary.categoryTotals.first { it.category.id == "home" }.amountMinor)
        assertEquals(64, summary.categoryTotals.first { it.category.id == "food" }.percent)
        assertEquals(2, summary.dailyTotals.size)
    }

    @Test fun emptyPreviousPeriodDoesNotInventComparison() {
        val range = DateWindow(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-10-31"))
        val current = expense("current", "2026-10-02", 1_000)
        val summary = Reporting.summarize(listOf(current), emptyList(), range, Reporting.previous(range, PeriodKind.MONTH), zone)
        assertEquals(1_000L, summary.totalMinor)
        assertEquals(0L, summary.previousTotalMinor)
    }

    @Test fun customPreviousWindowHasEqualLength() {
        val current = DateWindow(LocalDate.parse("2026-10-05"), LocalDate.parse("2026-10-11"))
        val previous = Reporting.previous(current, PeriodKind.CUSTOM)
        assertEquals(DateWindow(LocalDate.parse("2026-09-28"), LocalDate.parse("2026-10-04")), previous)
        assertNull(Reporting.summarize(emptyList(), emptyList(), current).previousTotalMinor)
    }
}
