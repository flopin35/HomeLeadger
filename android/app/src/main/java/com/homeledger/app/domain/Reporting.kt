package com.homeledger.app.domain

import com.homeledger.app.data.CategoryEntity
import com.homeledger.app.data.ExpenseEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

enum class PeriodKind { WEEK, MONTH, YEAR, CUSTOM }
data class DateWindow(val start: LocalDate, val endInclusive: LocalDate) {
    init { require(!endInclusive.isBefore(start)) }
    fun includes(date: LocalDate) = !date.isBefore(start) && !date.isAfter(endInclusive)
    val dayCount get() = ChronoUnit.DAYS.between(start, endInclusive) + 1
}
data class CategoryTotal(val category: CategoryEntity, val amountMinor: Long, val percent: Int)
data class SpendingSummary(val totalMinor: Long, val previousTotalMinor: Long?, val categoryTotals: List<CategoryTotal>, val dailyTotals: Map<LocalDate, Long>)

object Reporting {
    fun window(kind: PeriodKind, today: LocalDate, weekStartsOn: Int = 1, custom: DateWindow? = null): DateWindow = when (kind) {
        PeriodKind.WEEK -> {
            val offset = Math.floorMod(today.dayOfWeek.value - weekStartsOn, 7)
            val start = today.minusDays(offset.toLong())
            DateWindow(start, start.plusDays(6))
        }
        PeriodKind.MONTH -> DateWindow(today.withDayOfMonth(1), today.withDayOfMonth(today.lengthOfMonth()))
        PeriodKind.YEAR -> DateWindow(today.withDayOfYear(1), today.withDayOfYear(today.lengthOfYear()))
        PeriodKind.CUSTOM -> requireNotNull(custom) { "Custom period requires a date window" }
    }

    fun previous(window: DateWindow, kind: PeriodKind): DateWindow = when (kind) {
        PeriodKind.WEEK -> DateWindow(window.start.minusWeeks(1), window.endInclusive.minusWeeks(1))
        PeriodKind.MONTH -> {
            val start = window.start.minusMonths(1).withDayOfMonth(1)
            DateWindow(start, start.withDayOfMonth(start.lengthOfMonth()))
        }
        PeriodKind.YEAR -> {
            val start = window.start.minusYears(1).withDayOfYear(1)
            DateWindow(start, start.withDayOfYear(start.lengthOfYear()))
        }
        PeriodKind.CUSTOM -> {
            val end = window.start.minusDays(1)
            DateWindow(end.minusDays(window.dayCount - 1), end)
        }
    }

    fun summarize(
        expenses: List<ExpenseEntity>,
        categories: List<CategoryEntity>,
        window: DateWindow,
        previousWindow: DateWindow? = null,
        zone: ZoneId = ZoneId.systemDefault(),
    ): SpendingSummary {
        fun dateOf(row: ExpenseEntity) = Instant.ofEpochMilli(row.occurredAt).atZone(zone).toLocalDate()
        val current = expenses.filter { window.includes(dateOf(it)) }
        val total = current.sumOf { it.amountMinor }
        val byCategory = current.groupBy { it.categoryId }.mapValues { (_, rows) -> rows.sumOf { it.amountMinor } }
        val categoryMap = categories.associateBy { it.id }
        val categoryTotals = byCategory.mapNotNull { (id, amount) ->
            categoryMap[id]?.let { CategoryTotal(it, amount, if (total <= 0) 0 else ((amount * 100.0) / total).roundToInt()) }
        }.sortedByDescending { it.amountMinor }
        val previous = previousWindow?.let { range -> expenses.filter { range.includes(dateOf(it)) }.sumOf { it.amountMinor } }
        val daily = current.groupBy(::dateOf).mapValues { (_, rows) -> rows.sumOf { it.amountMinor } }.toSortedMap()
        return SpendingSummary(total, previous, categoryTotals, daily)
    }
}
