package com.example.halalyticscompose.data.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productHistoryDao: ProductHistoryDao
) {
    
    // Insert new product to history
    suspend fun insertProduct(product: ProductHistoryEntity) {
        productHistoryDao.insertProduct(product)
    }
    
    // Get all products from history
    fun getAllProducts(): Flow<List<ProductHistoryEntity>> {
        return productHistoryDao.getAllProducts()
    }
    
    // Get favorite products
    fun getFavoriteProducts(): Flow<List<ProductHistoryEntity>> {
        return productHistoryDao.getFavoriteProducts()
    }
    
    // Get recent products (for home screen)
    fun getRecentProducts(limit: Int = 5): Flow<List<ProductHistoryEntity>> {
        return productHistoryDao.getRecentProducts(limit)
    }
    
    // Delete product from history
    suspend fun deleteProduct(barcode: String) {
        productHistoryDao.deleteProduct(barcode)
    }
    
    // Toggle favorite status
    suspend fun toggleFavorite(barcode: String) {
        val product = productHistoryDao.getProductByBarcode(barcode)
        product?.let {
            productHistoryDao.updateFavoriteStatus(barcode, !it.isFavorite)
        }
    }
    
    // Add to favorites
    suspend fun addToFavorites(barcode: String) {
        productHistoryDao.updateFavoriteStatus(barcode, true)
    }
    
    // Remove from favorites
    suspend fun removeFromFavorites(barcode: String) {
        productHistoryDao.updateFavoriteStatus(barcode, false)
        productHistoryDao.updateFavoriteServerId(barcode, null)
    }

    // Update server ID for favorite
    suspend fun updateFavoriteServerId(barcode: String, serverId: Int?) {
        productHistoryDao.updateFavoriteServerId(barcode, serverId)
    }
    
    // Check if product is favorite
    suspend fun isFavorite(barcode: String): Boolean {
        val product = productHistoryDao.getProductByBarcode(barcode)
        return product?.isFavorite ?: false
    }
    
    // Get product by barcode
    suspend fun getProductByBarcode(barcode: String): ProductHistoryEntity? {
        return productHistoryDao.getProductByBarcode(barcode)
    }
    
    // Clean old products (keep only last 100)
    suspend fun cleanOldProducts() {
        val count = productHistoryDao.getProductCount()
        if (count > 100) {
            // Get the timestamp threshold (keep only last 100)
            // This is a simplified approach - you might want to implement a more sophisticated cleanup
            val threshold = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L) // 30 days ago
            productHistoryDao.deleteOldProducts(threshold)
        }
    }
    
    // Get statistics
    fun getStatistics(): Flow<Map<String, Int>> {
        return getAllProducts().map { products ->
            val stats = mutableMapOf<String, Int>()
            stats["total"] = products.size
            stats["favorites"] = products.count { it.isFavorite }
            stats["halal"] = products.count { it.status.equals("Halal", ignoreCase = true) }
            stats["haram"] = products.count { it.status.equals("Haram", ignoreCase = true) }
            stats["syubhat"] = products.count { it.status.equals("Syubhat", ignoreCase = true) }
            stats
        }
    }
    
    // Mark product as synced
    suspend fun markAsSynced(barcode: String) {
        productHistoryDao.markAsSynced(barcode)
    }

    suspend fun clearAllLocalData() {
        productHistoryDao.deleteAllProducts()
    }
}
