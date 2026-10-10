package com.homeledger.app.ui

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homeledger.app.data.ExpenseEntity
import com.homeledger.app.data.LedgerViewModel
import com.homeledger.app.domain.DateWindow
import com.homeledger.app.domain.PeriodKind
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun HomeLedgerApp(viewModel: LedgerViewModel) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var destination by remember { mutableStateOf(Destination.HOME) }
    var period by remember { mutableStateOf(PeriodKind.MONTH) }
    var customWindow by remember { mutableStateOf(DateWindow(LocalDate.now().withDayOfMonth(1), LocalDate.now())) }
    var editing by remember { mutableStateOf<ExpenseEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var confirmRestore by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val backupPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            runCatching { viewModel.createBackup() }.onSuccess { json ->
                runCatching { context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray(Charsets.UTF_8)) } ?: error("Could not open the selected file.") }
                    .onSuccess { snackbar.showSnackbar("Backup saved on this device.") }
                    .onFailure { snackbar.showSnackbar("Backup failed: ${it.message ?: "try again"}") }
            }.onFailure { snackbar.showSnackbar("Backup failed: ${it.message ?: "try again"}") }
        }
    }
    val restorePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            loading = true
            val result = runCatching {
                val json = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: error("Could not read the selected backup.")
                viewModel.restoreBackup(json)
            }
            loading = false
            result.onSuccess { snackbar.showSnackbar("Backup restored.") }.onFailure { snackbar.showSnackbar("Restore failed: ${it.message ?: "invalid backup"}") }
        }
    }

    HomeLedgerTheme(settings.theme) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                NavigationBar {
                    Destination.entries.forEach { item ->
                        NavigationBarItem(selected = destination == item, onClick = { destination = item }, icon = { Icon(item.icon(), contentDescription = null) }, label = { Text(item.label) })
                    }
                }
            },
            floatingActionButton = {
                if (destination != Destination.SETTINGS) ExtendedFloatingActionButton(onClick = { editing = null; showEditor = true }, icon = { Icon(Icons.Outlined.Add, contentDescription = null) }, text = { Text("Add expense") })
            },
        ) { insets ->
            Box(Modifier.fillMaxSize().padding(insets).imePadding()) {
                when (destination) {
                    Destination.HOME -> HomeScreen(expenses, categories, settings.currency, period, customWindow, settings.weekStart, { period = it }, { period = PeriodKind.CUSTOM; customWindow = it }, { editing = null; showEditor = true }, { editing = it; showEditor = true })
                    Destination.HISTORY -> HistoryScreen(expenses, categories, settings.currency) { editing = it; showEditor = true }
                    Destination.REPORTS -> ReportsScreen(expenses, categories, settings.currency, period, customWindow, settings.weekStart, { period = it }, { period = PeriodKind.CUSTOM; customWindow = it })
                    Destination.SETTINGS -> SettingsScreen(settings, categories, { viewModel.saveSettings(it); Unit }, { backupPicker.launch("homeledger-backup.json") }, { confirmRestore = true }, { id, name, icon, tint -> viewModel.saveCategory(id, name, icon, tint); Unit }, { category, archived -> viewModel.archiveCategory(category, archived); Unit })
                }
                if (loading) CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
    }

    if (showEditor) ExpenseEditor(editing, categories, settings.currency, onDismiss = { showEditor = false }, onSave = { amount, category, date, note ->
        viewModel.saveExpense(editing?.id, amount, category, date, note)
        showEditor = false
        editing = null
        destination = Destination.HOME
        scope.launch { snackbar.showSnackbar("Expense saved.") }
    }, onDelete = { expense -> viewModel.deleteExpense(expense.id); scope.launch { snackbar.showSnackbar("Expense deleted.") } })

    if (confirmRestore) AlertDialog(onDismissRequest = { confirmRestore = false }, title = { Text("Replace local ledger?") }, text = { Text("Restoring a backup replaces expenses, custom categories, and preferences on this device.") }, confirmButton = { TextButton(onClick = { confirmRestore = false; restorePicker.launch(arrayOf("application/json", "text/json")) }) { Text("Choose backup") } }, dismissButton = { TextButton(onClick = { confirmRestore = false }) { Text("Cancel") } })
}

private fun Destination.icon() = when (this) { Destination.HOME -> Icons.Filled.Home; Destination.HISTORY -> Icons.Filled.ListAlt; Destination.REPORTS -> Icons.Filled.PieChart; Destination.SETTINGS -> Icons.Filled.Settings }
