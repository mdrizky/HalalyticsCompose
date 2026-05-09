package com.example.halalyticscompose.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_history")
data class ProductHistoryEntity(
    @PrimaryKey val barcode: String,
    val name: String,
    val brand: String? = null,
    val status: String, // "Halal", "Haram", "Syubhat"
    val image: String? = null,
    val sources: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val favoriteServerId: Int? = null,
    val isSynced: Boolean = false
)
