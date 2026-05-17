package com.example.halalyticscompose.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductHistoryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductHistoryEntity)
    
    @Query("SELECT * FROM product_history ORDER BY timestamp DESC")
    fun getAllProducts(): Flow<List<ProductHistoryEntity>>
    
    @Query("SELECT * FROM product_history WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteProducts(): Flow<List<ProductHistoryEntity>>
    
    @Query("SELECT * FROM product_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentProducts(limit: Int = 5): Flow<List<ProductHistoryEntity>>
    
    @Query("DELETE FROM product_history WHERE barcode = :barcode")
    suspend fun deleteProduct(barcode: String)
    
    @Query("UPDATE product_history SET isFavorite = :isFavorite WHERE barcode = :barcode")
    suspend fun updateFavoriteStatus(barcode: String, isFavorite: Boolean)

    @Query("UPDATE product_history SET favoriteServerId = :serverId, isFavorite = 1 WHERE barcode = :barcode")
    suspend fun updateFavoriteServerId(barcode: String, serverId: Int?)
    
    @Query("SELECT * FROM product_history WHERE barcode = :barcode")
    suspend fun getProductByBarcode(barcode: String): ProductHistoryEntity?
    
    @Query("DELETE FROM product_history WHERE timestamp < :threshold")
    suspend fun deleteOldProducts(threshold: Long)
    
    @Query("SELECT COUNT(*) FROM product_history")
    suspend fun getProductCount(): Int
    
    @Query("SELECT COUNT(*) FROM product_history WHERE isFavorite = 1")
    suspend fun getFavoriteCount(): Int

    @Query("SELECT * FROM product_history WHERE isSynced = 0")
    suspend fun getUnsyncedProducts(): List<ProductHistoryEntity>

    @Query("UPDATE product_history SET isSynced = 1 WHERE barcode = :barcode")
    suspend fun markAsSynced(barcode: String)

    @Query("DELETE FROM product_history")
    suspend fun deleteAllProducts()
}
