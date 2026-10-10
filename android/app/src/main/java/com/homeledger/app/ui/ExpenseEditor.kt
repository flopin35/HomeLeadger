package com.homeledger.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeledger.app.data.CategoryEntity
import com.homeledger.app.data.ExpenseEntity
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEditor(expense: ExpenseEntity?, categories: List<CategoryEntity>, currency: String, onDismiss: () -> Unit, onSave: (Long, String, LocalDate, String) -> Unit, onDelete: (ExpenseEntity) -> Unit) {
    var amount by remember(expense?.id) { mutableStateOf(expense?.let { String.format(Locale.US, "%.2f", it.amountMinor / 100.0) } ?: "") }
    var note by remember(expense?.id) { mutableStateOf(expense?.note.orEmpty()) }
    var categoryId by remember(expense?.id, categories) { mutableStateOf(expense?.categoryId ?: categories.firstOrNull { !it.isArchived }?.id.orEmpty()) }
    var date by remember(expense?.id) { mutableStateOf(expense?.localDate() ?: LocalDate.now()) }
    var showDate by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val allowedCategories = categories.filter { !it.isArchived || it.id == expense?.categoryId }

    ModalBottomSheet(onDismissRequest = onDismiss, dragHandle = null) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDismiss) { Icon(Icons.Outlined.Close, contentDescription = "Close expense form") }
                Text(if (expense == null) "Add expense" else "Edit expense", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (expense != null) IconButton(onClick = { showDelete = true }) { Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete expense", tint = MaterialTheme.colorScheme.error) }
            }
            Text("AMOUNT ($currency)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp, modifier = Modifier.padding(top = 14.dp, bottom = 6.dp))
            OutlinedTextField(amount, { amount = it; error = null }, Modifier.fillMaxWidth(), prefix = { Text("$currency ") }, placeholder = { Text("0.00") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, shape = RoundedCornerShape(14.dp), isError = error != null)
            if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
            Text("CATEGORY", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp, modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))
            LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.fillMaxWidth().height(174.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(allowedCategories, key = { it.id }) { category ->
                    FilterChip(selected = categoryId == category.id, onClick = { categoryId = category.id }, label = { Text(category.name, maxLines = 1, fontSize = 11.sp) }, leadingIcon = { Icon(iconFor(category.icon), null) })
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(date.toString(), {}, Modifier.weight(1f), readOnly = true, label = { Text("Date") }, trailingIcon = { IconButton(onClick = { showDate = true }) { Icon(Icons.Outlined.CalendarMonth, contentDescription = "Choose date") } }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(note, { if (it.length <= 100) note = it }, Modifier.weight(1.4f), label = { Text("Note (optional)") }, singleLine = true, shape = RoundedCornerShape(12.dp))
            }
            Button(onClick = {
                val minor = amount.trim().toBigDecimalOrNull()?.let { value -> runCatching { value.setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact() }.getOrNull() }
                if (minor == null || minor <= 0L) error = "Enter an amount greater than zero with up to two decimal places."
                else if (categoryId.isBlank()) error = "Choose a category."
                else { onSave(minor, categoryId, date, note); onDismiss() }
            }, Modifier.fillMaxWidth().height(52.dp).padding(top = 4.dp)) { Text(if (expense == null) "Save expense" else "Save changes") }
            Spacer(Modifier.height(12.dp))
        }
    }
    if (showDate) {
        val initial = date.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        val state = rememberDatePickerState(initialSelectedDateMillis = initial)
        DatePickerDialog(onDismissRequest = { showDate = false }, confirmButton = { TextButton(onClick = { state.selectedDateMillis?.let { date = Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate() }; showDate = false }) { Text("Done") } }, dismissButton = { TextButton(onClick = { showDate = false }) { Text("Cancel") } }) { DatePicker(state) }
    }
    if (showDelete && expense != null) AlertDialog(onDismissRequest = { showDelete = false }, title = { Text("Delete this expense?") }, text = { Text("This removes the saved entry from your device. This cannot be undone.") }, confirmButton = { TextButton(onClick = { showDelete = false; onDelete(expense); onDismiss() }) { Text("Delete", color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Keep expense") } })
}
