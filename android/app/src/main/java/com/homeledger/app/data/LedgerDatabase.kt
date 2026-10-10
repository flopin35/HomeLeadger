package com.homeledger.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY occurredAt DESC, createdAt DESC")
    fun observeAll(): Flow<List<ExpenseEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(expense: ExpenseEntity)
    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: String)
    @Query("SELECT * FROM expenses ORDER BY occurredAt DESC, createdAt DESC")
    suspend fun allOnce(): List<ExpenseEntity>
    @Query("DELETE FROM expenses")
    suspend fun clearAll()
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rows: List<ExpenseEntity>)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY isBuiltIn DESC, name COLLATE NOCASE")
    fun observeAll(): Flow<List<CategoryEntity>>
    @Query("SELECT * FROM categories ORDER BY isBuiltIn DESC, name COLLATE NOCASE")
    suspend fun allOnce(): List<CategoryEntity>
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaults(rows: List<CategoryEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: CategoryEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rows: List<CategoryEntity>)
    @Query("DELETE FROM categories WHERE isBuiltIn = 0")
    suspend fun clearCustom()
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun observe(): Flow<SettingsEntity?>
    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun load(): SettingsEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(settings: SettingsEntity)
}

@Database(entities = [ExpenseEntity::class, CategoryEntity::class, SettingsEntity::class], version = 1, exportSchema = false)
abstract class LedgerDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile private var instance: LedgerDatabase? = null
        fun get(context: Context): LedgerDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, LedgerDatabase::class.java, "homeledger.db")
                .build().also { instance = it }
        }
    }
}

val builtInCategories = listOf(
    CategoryEntity("utilities", "Utilities", "bolt", "#DDF2E5", true),
    CategoryEntity("groceries", "Groceries", "cart", "#E8F1DC", true),
    CategoryEntity("housing", "Housing", "home", "#E6ECF6", true),
    CategoryEntity("repairs", "Repairs", "build", "#F5E8DA", true),
    CategoryEntity("transport", "Transport", "car", "#E4EFF2", true),
    CategoryEntity("health", "Healthcare", "health", "#F6E4E5", true),
    CategoryEntity("education", "Education", "school", "#F1EAF5", true),
    CategoryEntity("subscriptions", "Subscriptions", "subscriptions", "#E7EBF5", true),
    CategoryEntity("other", "Other", "category", "#ECEFEB", true),
)
