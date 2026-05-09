package com.example.halalyticscompose.data.model

import com.google.gson.annotations.SerializedName

/**
 * Generic response for simple API calls
 */
data class GenericResponse(
    @SerializedName("response_code")
    val responseCode: Int = 0,
    
    @SerializedName("success")
    val success: Boolean = false,
    
    @SerializedName("message")
    val message: String = "",
    
    @SerializedName("content")
    val content: Any? = null
) {
    val data: Any?
        get() = content
}

/**
 * Scan History Response
 */
data class ScanHistoryResponse(
    @SerializedName("response_code")
    val responseCode: Int = 0,
    
    @SerializedName("success")
    val success: Boolean = false,
    
    @SerializedName("message")
    val message: String = "",
    
    @SerializedName("content")
    val content: ScanHistoryContent? = null
)

data class ScanHistoryContent(
    @SerializedName("data")
    val data: List<ScanItem>? = null,
    @SerializedName("current_page")
    val currentPage: Int? = null,
    @SerializedName("total")
    val total: Int? = null
)

data class ScanItem(
    @SerializedName("id_scan")
    val id_scan: Int = 0,
    
    @SerializedName("user_id")
    val user_id: Int = 0,
    
    @SerializedName("product_id")
    val product_id: Int? = null,
    
    @SerializedName("nama_produk")
    val nama_produk: String = "",
    
    @SerializedName("barcode")
    val barcode: String? = null,
    
    @SerializedName("kategori")
    val kategori: String? = null,
    
    @SerializedName("status_halal")
    val status_halal: String = "",
    
    @SerializedName("status_kesehatan")
    val status_kesehatan: String = "",
    
    @SerializedName("tanggal_expired")
    val tanggal_expired: String? = null,
    
    @SerializedName("tanggal_scan")
    val tanggal_scan: String? = null,
    
    @SerializedName("created_at")
    val created_at: String? = null
)

/**
 * Product Response
 */
data class ProductResponse(
    @SerializedName("response_code")
    val responseCode: Int = 0,
    
    @SerializedName("success")
    val success: Boolean = false,
    
    @SerializedName("message")
    val message: String = "",
    
    @SerializedName("content")
    val content: ProductContent? = null
)

data class ProductContent(
    @SerializedName("data")
    val data: List<LocalProduct>? = null,
    
    @SerializedName("product")
    val product: LocalProduct? = null,
    
    @SerializedName("current_page")
    val currentPage: Int? = null,
    
    @SerializedName("total")
    val total: Int? = null
)



data class LocalProduct(
    @SerializedName("id_product")
    val idProduct: Int = 0,
    
    @SerializedName("nama_product")
    val namaProduct: String = "",
    
    @SerializedName("barcode")
    val barcode: String? = null,
    
    @SerializedName("komposisi")
    val komposisi: String? = null,
    
    @SerializedName("status")
    val status: String? = null,
    
    @SerializedName("info_gizi")
    val infoGizi: String? = null,
    
    @SerializedName("kategori_id")
    val kategoriId: Int? = null,
    
    @SerializedName("kategori")
    val kategori: Kategori? = null,
    
    @SerializedName("image_url")
    val imageUrl: String? = null,
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class Kategori(
    @SerializedName("id_kategori")
    val idKategori: Int = 0,
    
    @SerializedName("nama_kategori")
    val namaKategori: String = ""
)


data class HealthEncyclopedia(
    val id: Int = 0,
    val type: String = "",
    val alphabet: String = "",
    val title: String = "",
    val summary: String? = null,
    val content: String? = null,
    val source_link: String? = null
)

data class HealthEncyclopediaResponse(
    val success: Boolean = false,
    val data: List<HealthEncyclopedia> = emptyList(),
    val message: String? = null
)

data class HealthEncyclopediaDetailResponse(
    val success: Boolean = false,
    val data: HealthEncyclopedia? = null,
    val message: String? = null
)

