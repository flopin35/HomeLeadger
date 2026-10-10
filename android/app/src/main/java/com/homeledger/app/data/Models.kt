package com.homeledger.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val amountMinor: Long,
    val categoryId: String,
    val note: String,
    val occurredAt: Long,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String,
    val tint: String,
    val isBuiltIn: Boolean,
    val isArchived: Boolean = false,
)

@Entity(tableName = "app_settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val currency: String = "GHS",
    val weekStart: Int = 1,
    val theme: String = "SYSTEM",
)
