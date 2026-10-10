package com.homeledger.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeledger.app.data.CategoryEntity
import com.homeledger.app.data.SettingsEntity

@Composable
fun SettingsScreen(settings: SettingsEntity, categories: List<CategoryEntity>, onSettings: (SettingsEntity) -> Unit, onBackup: () -> Unit, onRestore: () -> Unit, onSaveCategory: (String?, String, String, String) -> Unit, onArchiveCategory: (CategoryEntity, Boolean) -> Unit) {
    var manageCategories by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        ScreenTitle("Settings", "Your preferences and local data.")
        Text("PREFERENCES", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp, modifier = Modifier.padding(top = 22.dp, bottom = 8.dp))
        ChoiceRow("Currency", settings.currency, listOf("GHS", "USD", "GBP")) { onSettings(settings.copy(currency = it)) }
        ChoiceRow("Week starts on", if (settings.weekStart == 7) "Sunday" else "Monday", listOf("Monday", "Sunday")) { onSettings(settings.copy(weekStart = if (it == "Sunday") 7 else 1)) }
        ChoiceRow("Appearance", settings.theme.lowercase().replaceFirstChar(Char::uppercase), listOf("System", "Light", "Dark")) { onSettings(settings.copy(theme = it.uppercase())) }

        Text("CATEGORIES", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp, modifier = Modifier.padding(top = 22.dp, bottom = 8.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Category, null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f).padding(start = 12.dp)) { Text("Manage categories", fontWeight = FontWeight.SemiBold, fontSize = 13.sp); Text("${categories.count { !it.isArchived }} available", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 3.dp)) }
                TextButton(onClick = { manageCategories = true }) { Text("Manage") }
            }
        }

        Text("BACKUP AND RESTORE", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp, modifier = Modifier.padding(top = 22.dp, bottom = 8.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Your data stays on this device", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("Save a local JSON backup or restore one you choose. The backup file is not uploaded.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 5.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                    Button(onClick = onBackup) { Icon(Icons.Outlined.Backup, null); Text("Back up", Modifier.padding(start = 6.dp)) }
                    TextButton(onClick = onRestore) { Icon(Icons.Outlined.Restore, null); Text("Restore") }
                }
                Text("Uninstalling HomeLedger or clearing its app data can remove records. Keep a backup somewhere you control.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
            }
        }

        Text("ABOUT", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp, modifier = Modifier.padding(top = 22.dp, bottom = 8.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) { Text("HomeLedger", fontSize = 14.sp, fontWeight = FontWeight.Bold); Text("Household expenses, thoughtfully organized.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp)); Text("Version 0.1.0", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp)) }
        }
    }
    if (manageCategories) CategoryManagerDialog(categories, onDismiss = { manageCategories = false }, onSave = onSaveCategory, onArchive = onArchiveCategory)
}

@Composable
private fun ChoiceRow(title: String, selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        androidx.compose.foundation.layout.Box {
            TextButton(onClick = { expanded = true }) { Text(selected) }
            DropdownMenu(expanded, onDismissRequest = { expanded = false }) { options.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false }) } }
        }
    }
}

@Composable
private fun CategoryManagerDialog(categories: List<CategoryEntity>, onDismiss: () -> Unit, onSave: (String?, String, String, String) -> Unit, onArchive: (CategoryEntity, Boolean) -> Unit) {
    var editing by remember { mutableStateOf<CategoryEntity?>(null) }
    var creating by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Expense categories") }, text = {
        Column {
            Button(onClick = { editing = null; creating = true }, modifier = Modifier.fillMaxWidth()) { Text("Add custom category") }
            LazyColumn(Modifier.heightIn(max = 390.dp)) {
                items(categories.sortedBy { it.name }, key = { it.id }) { category ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(iconFor(category.icon), null, tint = MaterialTheme.colorScheme.primary)
                        Column(Modifier.weight(1f).padding(start = 10.dp)) { Text(category.name, fontSize = 12.sp, fontWeight = FontWeight.Medium); Text(if (category.isBuiltIn) "Built in" else if (category.isArchived) "Archived" else "Custom", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        if (!category.isBuiltIn) TextButton(onClick = { editing = category; creating = true }) { Text("Edit") }
                        TextButton(onClick = { onArchive(category, !category.isArchived) }) { Text(if (category.isArchived) "Restore" else "Archive") }
                    }
                }
            }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } })
    if (creating) CategoryEditDialog(editing, onDismiss = { creating = false }, onSave = { id, name, icon, tint -> onSave(id, name, icon, tint); creating = false })
}

@Composable
private fun CategoryEditDialog(category: CategoryEntity?, onDismiss: () -> Unit, onSave: (String?, String, String, String) -> Unit) {
    var name by remember(category?.id) { mutableStateOf(category?.name.orEmpty()) }
    var icon by remember(category?.id) { mutableStateOf(category?.icon ?: "category") }
    val icons = listOf("category", "bolt", "cart", "home", "build", "car", "health", "school", "subscriptions")
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (category == null) "New category" else "Edit category") }, text = {
        Column {
            OutlinedTextField(name, { name = it.take(32) }, label = { Text("Category name") }, singleLine = true)
            Text("Choose an icon", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp))
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) { items(icons) { value -> TextButton(onClick = { icon = value }) { Text(if (icon == value) "[${value.take(4)}]" else value.take(4)) } } }
        }
    }, confirmButton = { TextButton(enabled = name.isNotBlank(), onClick = { onSave(category?.id, name.trim(), icon, "#E8F1DC") }) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}
