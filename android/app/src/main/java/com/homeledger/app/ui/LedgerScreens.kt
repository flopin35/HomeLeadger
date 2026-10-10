package com.homeledger.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeledger.app.data.CategoryEntity
import com.homeledger.app.data.ExpenseEntity
import com.homeledger.app.domain.DateWindow
import com.homeledger.app.domain.PeriodKind
import com.homeledger.app.domain.Reporting
import com.homeledger.app.domain.SpendingSummary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class Destination(val label: String) { HOME("Home"), HISTORY("History"), REPORTS("Reports"), SETTINGS("Settings") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodSelector(selected: PeriodKind, onSelect: (PeriodKind) -> Unit, custom: DateWindow, onCustom: (DateWindow) -> Unit) {
    var showRange by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        listOf(PeriodKind.WEEK to "Week", PeriodKind.MONTH to "Month", PeriodKind.YEAR to "Year", PeriodKind.CUSTOM to "Custom").forEach { (period, label) ->
            FilterChip(selected == period, onClick = { if (period == PeriodKind.CUSTOM) showRange = true else onSelect(period) }, label = { Text(label, fontSize = 12.sp) })
        }
    }
    if (showRange) {
        val state = rememberDateRangePickerState()
        DatePickerDialog(onDismissRequest = { showRange = false }, confirmButton = {
            TextButton(onClick = {
                val start = state.selectedStartDateMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate() }
                val end = state.selectedEndDateMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate() }
                if (start != null && end != null && !end.isBefore(start)) onCustom(DateWindow(start, end))
                showRange = false
            }) { Text("Apply") }
        }, dismissButton = { TextButton(onClick = { showRange = false }) { Text("Cancel") } }) {
            DateRangePicker(state = state, title = { Text("Choose report dates", Modifier.padding(start = 24.dp, top = 16.dp)) }, showModeToggle = false)
        }
    }
}

@Composable
fun HomeScreen(expenses: List<ExpenseEntity>, categories: List<CategoryEntity>, currency: String, period: PeriodKind, custom: DateWindow, weekStart: Int, onPeriod: (PeriodKind) -> Unit, onCustom: (DateWindow) -> Unit, onAdd: () -> Unit, onEdit: (ExpenseEntity) -> Unit) {
    val today = LocalDate.now()
    val window = if (period == PeriodKind.CUSTOM) custom else Reporting.window(period, today, weekStart)
    val previous = Reporting.previous(window, period)
    val summary = Reporting.summarize(expenses, categories, window, previous)
    val activeCategories = categories.filterNot { it.isArchived }
    val rows = expenses.filter { window.includes(it.localDate()) }.take(5)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        ScreenTitle("Your household at a glance", "A clear view of where it goes.")
        Spacer(Modifier.height(16.dp))
        PeriodSelector(period, onPeriod, custom, onCustom)
        Spacer(Modifier.height(12.dp))
        TotalCard(summary, currency, window)
        if (summary.totalMinor > 0) {
            SectionHeader("By category")
            CategoryBreakdown(summary, currency)
            SectionHeader("Recent activity", trailing = { Text("${rows.size.coerceAtMost(5)} shown", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp) })
            rows.forEach { expense -> ExpenseRow(expense, activeCategories.firstOrNull { it.id == expense.categoryId }, currency, onClick = { onEdit(expense) }) }
        } else {
            Spacer(Modifier.height(16.dp))
            EmptyState("A fresh start", "Your expenses will appear here when you add them. No sample transactions are saved in your ledger.", onAdd)
            SectionHeader("Recent activity")
            Text("No expenses in this period yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
private fun TotalCard(summary: SpendingSummary, currency: String, window: DateWindow) {
    val compare = summary.previousTotalMinor?.takeIf { it > 0 && summary.totalMinor > 0 }
    val delta = compare?.let { ((summary.totalMinor - it) * 100.0 / it).toInt() }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text("TOTAL SPENDING", color = Color.White.copy(alpha = .76f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.1.sp)
            Text(formatMoney(summary.totalMinor, currency), color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            Text("${window.start.format(DateTimeFormatter.ofPattern("d MMM"))} - ${window.endInclusive.format(DateTimeFormatter.ofPattern("d MMM yyyy"))}", color = Color.White.copy(alpha = .78f), fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
            if (delta != null) Text("${if (delta > 0) "+$delta%" else "$delta%"} vs previous period", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(top = 12.dp))
        }
    }
}

@Composable
fun CategoryBreakdown(summary: SpendingSummary, currency: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)) {
            summary.categoryTotals.take(5).forEach { total ->
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) { Icon(iconFor(total.category.icon), contentDescription = total.category.name, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp)) }
                    Column(Modifier.weight(1f).padding(start = 10.dp)) {
                        Text(total.category.name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        androidx.compose.foundation.Canvas(Modifier.fillMaxWidth().padding(top = 6.dp).height(5.dp)) {
                            drawRoundRect(color = Color(0xFFE8EEE9), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f))
                            drawRoundRect(color = MaterialTheme.colorScheme.primary, size = androidx.compose.ui.geometry.Size(size.width * total.percent.coerceIn(0, 100) / 100f, size.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f))
                        }
                    }
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 10.dp)) { Text(formatMoney(total.amountMinor, currency), fontSize = 12.sp, fontWeight = FontWeight.SemiBold); Text("${total.percent}%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
    }
}

@Composable
fun ExpenseRow(expense: ExpenseEntity, category: CategoryEntity?, currency: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) { Icon(iconFor(category?.icon ?: "category"), contentDescription = category?.name ?: "Other", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
        Column(Modifier.weight(1f).padding(start = 11.dp)) {
            Text(expense.note.ifBlank { category?.name ?: "Household expense" }, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            Text("${category?.name ?: "Other"}  ·  ${expense.localDate().format(DateTimeFormatter.ofPattern("d MMM yyyy"))}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
        }
        Text("- ${formatMoney(expense.amountMinor, currency)}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
}

@Composable
fun HistoryScreen(expenses: List<ExpenseEntity>, categories: List<CategoryEntity>, currency: String, onEdit: (ExpenseEntity) -> Unit) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf<String?>(null) }
    var dateFilter by remember { mutableStateOf<DateWindow?>(null) }
    var showDateFilter by remember { mutableStateOf(false) }
    val active = categories.filterNot { it.isArchived }
    val filtered = expenses.filter { row ->
        val category = categories.firstOrNull { it.id == row.categoryId }
        (filter == null || row.categoryId == filter) && (dateFilter == null || dateFilter!!.includes(row.localDate())) && (query.isBlank() || row.note.contains(query, true) || category?.name?.contains(query, true) == true)
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        ScreenTitle("Expense history", "Every entry, easy to find.")
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Outlined.Search, null) }, placeholder = { Text("Search expenses") }, singleLine = true, shape = RoundedCornerShape(14.dp))
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.padding(top = 8.dp)) {
            item { FilterChip(filter == null, { filter = null }, label = { Text("All") }) }
            items(active) { category -> FilterChip(filter == category.id, { filter = if (filter == category.id) null else category.id }, label = { Text(category.name) }) }
            item { FilterChip(dateFilter != null, { showDateFilter = true }, label = { Text(dateFilter?.let { "${it.start.format(DateTimeFormatter.ofPattern("d MMM"))} - ${it.endInclusive.format(DateTimeFormatter.ofPattern("d MMM"))}" } ?: "Date range") }) }
        }
        if (filtered.isEmpty()) {
            EmptyState(if (expenses.isEmpty()) "No expenses yet" else "No matching expenses", if (expenses.isEmpty()) "Your saved expenses will appear here." else "Try another search or clear the category filter.", null)
        } else {
            val grouped = filtered.groupBy { it.localDate() }.toSortedMap(compareByDescending { it })
            LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f).padding(top = 10.dp)) {
                grouped.forEach { (date, rows) ->
                    item(key = "date-$date") { Text(if (date == LocalDate.now()) "Today" else date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 13.dp, bottom = 4.dp)) }
                    items(rows, key = { it.id }) { row -> ExpenseRow(row, categories.firstOrNull { it.id == row.categoryId }, currency) { onEdit(row) } }
                }
            }
        }
    }
    if (showDateFilter) DateRangeFilterDialog(onDismiss = { showDateFilter = false }, onApply = { dateFilter = it; showDateFilter = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeFilterDialog(onDismiss: () -> Unit, onApply: (DateWindow) -> Unit) {
    val state = rememberDateRangePickerState()
    DatePickerDialog(onDismissRequest = onDismiss, confirmButton = {
        TextButton(onClick = {
            val start = state.selectedStartDateMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate() }
            val end = state.selectedEndDateMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate() }
            if (start != null && end != null && !end.isBefore(start)) onApply(DateWindow(start, end))
        }) { Text("Apply") }
    }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }) {
        DateRangePicker(state = state, title = { Text("Filter dates", Modifier.padding(start = 24.dp, top = 16.dp)) }, showModeToggle = false)
    }
}

@Composable
fun ReportsScreen(expenses: List<ExpenseEntity>, categories: List<CategoryEntity>, currency: String, period: PeriodKind, custom: DateWindow, weekStart: Int, onPeriod: (PeriodKind) -> Unit, onCustom: (DateWindow) -> Unit) {
    val window = if (period == PeriodKind.CUSTOM) custom else Reporting.window(period, LocalDate.now(), weekStart)
    val summary = Reporting.summarize(expenses, categories, window, Reporting.previous(window, period))
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        ScreenTitle("Reports", "Understand the shape of your spending.")
        Spacer(Modifier.height(14.dp)); PeriodSelector(period, onPeriod, custom, onCustom)
        Spacer(Modifier.height(12.dp)); TotalCard(summary, currency, window)
        SectionHeader("Spending trend")
        if (summary.dailyTotals.size >= 2) TrendChart(summary.dailyTotals)
        else Text("Add expenses across a few dates to see a spending trend.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(vertical = 10.dp))
        SectionHeader("Category breakdown")
        if (summary.categoryTotals.isEmpty()) Text("No category totals for this period yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        else CategoryBreakdown(summary, currency)
        summary.previousTotalMinor?.takeIf { it > 0 }?.let { previous -> Text("Previous period: ${formatMoney(previous, currency)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 14.dp, bottom = 18.dp)) }
    }
}

@Composable
private fun TrendChart(values: Map<LocalDate, Long>) {
    val max = values.values.maxOrNull()?.coerceAtLeast(1L) ?: 1L
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Canvas(Modifier.fillMaxWidth().height(120.dp)) {
                val step = size.width / values.size
                values.values.forEachIndexed { index, value ->
                    val height = size.height * (value.toFloat() / max)
                    drawRoundRect(MaterialTheme.colorScheme.primary, topLeft = androidx.compose.ui.geometry.Offset(index * step + step * .18f, size.height - height), size = androidx.compose.ui.geometry.Size(step * .64f, height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f))
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(values.keys.first().format(DateTimeFormatter.ofPattern("d MMM")), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(values.keys.last().format(DateTimeFormatter.ofPattern("d MMM")), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
fun EmptyState(title: String, message: String, onAction: (() -> Unit)?) {
    Card(Modifier.fillMaxWidth().padding(top = 8.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.padding(top = 10.dp))
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 5.dp))
            if (onAction != null) androidx.compose.material3.Button(onClick = onAction, modifier = Modifier.padding(top = 14.dp)) { Icon(Icons.Outlined.Add, null); Text("Add expense", Modifier.padding(start = 6.dp)) }
        }
    }
}

fun ExpenseEntity.localDate(): LocalDate = Instant.ofEpochMilli(occurredAt).atZone(ZoneId.systemDefault()).toLocalDate()
