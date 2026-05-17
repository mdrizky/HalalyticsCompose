package com.example.halalyticscompose.data.model

import com.google.gson.annotations.SerializedName

// Notifications
data class NotificationResponse(
    val success: Boolean,
    val data: PaginatedNotifications,
    @SerializedName("unread_count") val unreadCount: Int
)

data class PaginatedNotifications(
    val data: List<NotificationItem>,
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("last_page") val lastPage: Int
)

data class NotificationItem(
    val id: Int,
    val title: String,
    val message: String,
    val type: String, // system, scan, umkm, favorite
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("action_type") val actionType: String?,
    @SerializedName("action_value") val actionValue: String?,
    // Relations can be nullable
    @SerializedName("related_product") val relatedProduct: Product?, 
    // @SerializedName("related_umkm") val relatedUmkm: UmkmProduct? // Define if needed
)

// Scan History (Realtime)
data class RealtimeScanHistoryResponse(
    val success: Boolean,
    val data: PaginatedScanHistories? = null,
    val stats: ScanStats? = null,
    val message: String? = null
)

data class ScanHistoryDetailResponse(
    val success: Boolean,
    val data: ScanHistoryDetail? = null,
    val message: String? = null
)

data class PaginatedScanHistories(
    val data: List<ScanHistoryItem> = emptyList()
)

data class ScanHistoryItem(
    val id: Int,
    @SerializedName("product_name") val productName: String? = null,
    @SerializedName("product_image") val productImage: String? = null,
    @SerializedName("barcode") val barcode: String? = null,
    @SerializedName("halal_status") val halalStatus: String? = "unknown",
    val source: String? = "unknown",
    @SerializedName("scan_method") val scanMethod: String? = "unknown",
    @SerializedName("created_at") val createdAt: String? = null,
    // Legacy support fields (aliases)
    val status: String = halalStatus ?: "unknown",
    val timestamp: Long = 0L
)

data class ScanHistoryDetail(
    val id: Int,
    @SerializedName("product_name") val productName: String? = null,
    @SerializedName("product_image") val productImage: String? = null,
    @SerializedName("barcode") val barcode: String? = null,
    @SerializedName("halal_status") val halalStatus: String? = "unknown",
    val source: String? = "unknown",
    @SerializedName("scan_method") val scanMethod: String? = "unknown",
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("confidence_score") val confidenceScore: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerializedName("nutrition_snapshot") val nutritionSnapshot: Map<String, Any>? = null
)

data class ScanStats(
    @SerializedName("total_scans") val totalScans: Int,
    @SerializedName("today_scans") val todayScans: Int,
    @SerializedName("week_scans") val weekScans: Int,
    @SerializedName("halal_count") val halalCount: Int
)

// Favorites
data class FavoriteResponse(
    val success: Boolean,
    val data: List<FavoriteItem>
)

data class FavoriteItem(
    val id: Int,
    @SerializedName("product_name") val productName: String,
    @SerializedName("product_image") val productImage: String?,
    @SerializedName("halal_status") val halalStatus: String,
    val category: String?,
    val barcode: String?, // Added field from backend
    @SerializedName("has_status_changed") val hasStatusChanged: Boolean,
    @SerializedName("user_notes") val userNotes: String?,
    @SerializedName("created_at") val createdAt: String
)

// Firebase Updates
data class NotificationUpdate(
    val id: Int = 0,
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val is_read: Boolean = false,
    val created_at: Long = 0,
    val action_type: String? = null,
    val action_value: String? = null
)

data class ScanHistoryUpdate(
    val id: Int = 0,
    val product_name: String = "",
    val barcode: String? = null,
    val halal_status: String = "",
    val created_at: Long = 0
)

data class AdminStats(
    val total_users: Int = 0,
    val total_scans: Int = 0,
    val total_products: Int = 0,
    val pending_verifications: Int = 0
)

// Requests
data class RecordScanRequest(
    val scannable_type: String,
    val scannable_id: Int,
    val product_name: String,
    val product_image: String?,
    val barcode: String?,
    val halal_status: String,
    val scan_method: String,
    val source: String,
    val latitude: Double?,
    val longitude: Double?,
    val confidence_score: Int?,
    val nutrition_snapshot: Map<String, Any>?
)

data class AddFavoriteRequest(
    val favoritable_type: String = "App\\Models\\ProductModel",
    val favoritable_id: Int? = null,
    val barcode: String? = null,
    val product_name: String,
    val product_image: String? = null,
    val halal_status: String,
    val category: String? = null,
    val user_notes: String? = null
)

data class UnreadCountResponse(
    val success: Boolean,
    val count: Int
)
