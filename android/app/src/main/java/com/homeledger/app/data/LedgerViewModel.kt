package com.homeledger.app.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class LedgerViewModel(application: Application) : AndroidViewModel(application) {
    private val database = LedgerDatabase.get(application)
    private val expensesDao = database.expenseDao()
    private val categoriesDao = database.categoryDao()
    private val settingsDao = database.settingsDao()

    val expenses = expensesDao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val categories = categoriesDao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val settings = settingsDao.observe().map { it ?: SettingsEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsEntity())

    init {
        viewModelScope.launch {
            categoriesDao.insertDefaults(builtInCategories)
            if (settingsDao.load() == null) settingsDao.save(SettingsEntity())
        }
    }

    fun saveExpense(id: String?, amountMinor: Long, categoryId: String, date: LocalDate, note: String) = viewModelScope.launch {
        val zone = ZoneId.systemDefault()
        val timestamp = date.atStartOfDay(zone).toInstant().toEpochMilli()
        expensesDao.upsert(ExpenseEntity(id ?: UUID.randomUUID().toString(), amountMinor, categoryId, note.trim(), timestamp))
    }

    fun deleteExpense(id: String) = viewModelScope.launch { expensesDao.deleteById(id) }
    fun saveSettings(settings: SettingsEntity) = viewModelScope.launch { settingsDao.save(settings) }

    fun saveCategory(id: String?, name: String, icon: String, tint: String) = viewModelScope.launch {
        val key = id ?: "custom-${UUID.randomUUID()}"
        categoriesDao.upsert(CategoryEntity(key, name.trim(), icon, tint, isBuiltIn = false, isArchived = false))
    }

    fun archiveCategory(category: CategoryEntity, archived: Boolean) = viewModelScope.launch {
        categoriesDao.upsert(category.copy(isArchived = archived))
    }

    suspend fun createBackup(): String {
        val root = JSONObject().put("format", 1)
        root.put("expenses", JSONArray().apply {
            expensesDao.allOnce().forEach { row -> put(JSONObject().put("id", row.id).put("amountMinor", row.amountMinor).put("categoryId", row.categoryId).put("note", row.note).put("occurredAt", row.occurredAt).put("createdAt", row.createdAt)) }
        })
        root.put("categories", JSONArray().apply {
            categoriesDao.allOnce().forEach { row -> put(JSONObject().put("id", row.id).put("name", row.name).put("icon", row.icon).put("tint", row.tint).put("isBuiltIn", row.isBuiltIn).put("isArchived", row.isArchived)) }
        })
        val prefs = settings.value
        root.put("settings", JSONObject().put("currency", prefs.currency).put("weekStart", prefs.weekStart).put("theme", prefs.theme))
        return root.toString(2)
    }

    suspend fun restoreBackup(contents: String) {
        val root = JSONObject(contents)
        require(root.optInt("format") == 1) { "This backup format is not supported." }
        val expenses = root.getJSONArray("expenses").let { rows -> (0 until rows.length()).map { i ->
            val item = rows.getJSONObject(i)
            ExpenseEntity(item.getString("id"), item.getLong("amountMinor"), item.getString("categoryId"), item.optString("note"), item.getLong("occurredAt"), item.optLong("createdAt", item.getLong("occurredAt")))
        } }
        val categoriesFromBackup = root.optJSONArray("categories")?.let { rows -> (0 until rows.length()).map { i ->
            val item = rows.getJSONObject(i)
            CategoryEntity(item.getString("id"), item.getString("name"), item.optString("icon", "category"), item.optString("tint", "#ECEFEB"), item.optBoolean("isBuiltIn"), item.optBoolean("isArchived"))
        } }.orEmpty()
        val pref = root.optJSONObject("settings") ?: JSONObject()
        database.withTransaction {
            expensesDao.clearAll()
            if (expenses.isNotEmpty()) expensesDao.insertAll(expenses)
            categoriesDao.clearCustom()
            categoriesDao.insertDefaults(builtInCategories)
            if (categoriesFromBackup.isNotEmpty()) categoriesDao.upsertAll(categoriesFromBackup)
            settingsDao.save(SettingsEntity(currency = pref.optString("currency", "GHS"), weekStart = pref.optInt("weekStart", 1), theme = pref.optString("theme", "SYSTEM")))
        }
    }
}
