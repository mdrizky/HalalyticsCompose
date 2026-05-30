package com.example.halalyticscompose.data.repository

import com.example.halalyticscompose.data.model.*
import com.example.halalyticscompose.data.api.ApiService
import com.example.halalyticscompose.data.network.ExternalApiService
import com.example.halalyticscompose.data.local.Dao.CachedScanResultDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val apiService: ApiService,
    private val externalApiService: ExternalApiService,
    private val cachedDao: CachedScanResultDao
) {
    suspend fun getProductWithHalalInfo(barcode: String, token: String? = null): Result<Product> {
        println("🔍 getProductWithHalalInfo called for barcode: $barcode")
        try {
            // 1. Try Unified Scan if token is available
            if (token != null) {
                println("🚀 Trying Unified Scan for barcode: $barcode")
                val bearerToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val unifiedResponse = apiService.scanUnified(bearerToken, barcode)
                if (unifiedResponse.isSuccessful && unifiedResponse.body()?.success == true) {
                    val data = unifiedResponse.body()!!.data
                    if (data != null) {
                        return Result.success(mapUnifiedToProduct(data))
                    }
                }
            }

            // 2. Try Halalytics External API (Public Endpoint)
            println("🌐 Trying Halalytics External API for barcode: $barcode")
            try {
                val externalResponse = externalApiService.getProductDetail(barcode)
                if (externalResponse.isSuccessful && externalResponse.body()?.responseCode == 200) {
                    val productItem = externalResponse.body()?.content
                    if (productItem != null) {
                        return Result.success(mapProductItemToProduct(productItem))
                    }
                }
            } catch (e: Exception) {
                println("❌ External API Exception: ${e.message}")
            }

            // 3. Fallback to Open Food Facts API directly (Raw Data)
            println("📡 Trying Raw Open Food Facts for barcode: $barcode")
            try {
                val offResponse = apiService.getOpenFoodFactsProduct(barcode)
                if (offResponse.isSuccessful && offResponse.body()?.status == 1) {
                    val offProduct = offResponse.body()?.product
                    if (offProduct != null) {
                        return Result.success(mapOpenFoodFactsToProduct(offProduct, barcode))
                    }
                }
            } catch (e: Exception) {
                println("❌ Open Food Facts Exception: ${e.message}")
            }

            // 4. Fallback to Legacy local API
            println("🔌 Trying legacy API for barcode: $barcode")
            val response = apiService.getProduct(barcode)
            
            if (response.isSuccessful && response.body()?.success == true) {
                println("✅ Halalytics API success for barcode: $barcode")
                val responseData = response.body()!!.data
                if (responseData != null) {
                    val productInfo = responseData.product
                    val product = Product(
                        id = productInfo.id,
                        barcode = productInfo.barcode,
                        name = productInfo.name,
                        brand = productInfo.brand ?: "Unknown",
                        category = productInfo.category ?: "Unknown",
                        image = productInfo.image,
                        halalInfo = HalalInfo(
                            halalStatus = HalalStatus.fromString(responseData.halal_info.halal_status),
                            certificateNumber = responseData.halal_info.halal_certificate_number,
                            certificationBody = responseData.halal_info.certification_body,
                            validUntil = responseData.halal_info.certificate_valid_until,
                            lastChecked = responseData.halal_info.last_checked_at,
                            source = responseData.halal_source
                        )
                    )
                    return Result.success(product)
                }
            } 

            // 5. Fallback to local cache
            val cached = cachedDao.getByBarcode(barcode)
            if (cached != null) {
                println("📦 Found cached result for barcode: $barcode")
                return Result.success(mapCachedToProduct(cached))
            }

            return Result.failure(Exception("Product not found anywhere"))

        } catch (e: Exception) {
            println("❌ General Error: ${e.message}")
            val cached = cachedDao.getByBarcode(barcode)
            return if (cached != null) {
                Result.success(mapCachedToProduct(cached))
            } else {
                Result.failure(e)
            }
        }
    }

    private fun mapUnifiedToProduct(data: UnifiedProductData): Product {
        return Product(
            id = data.id,
            barcode = data.barcode,
            name = data.namaProduct,
            brand = data.brand ?: "Unknown",
            category = data.kategori ?: "Umum",
            image = data.image,
            halalInfo = HalalInfo(
                halalStatus = HalalStatus.fromString(data.halalStatus),
                certificateNumber = null,
                certificationBody = null,
                validUntil = null,
                lastChecked = null,
                source = data.source
            ),
            ingredientsText = data.komposisi,
            isVerified = data.isVerified,
            verificationStatus = data.verificationStatus,
            quantity = data.quantity,
            packaging = data.packaging,
            labelsTags = data.labels?.split(",")?.map { it.trim() },
            stores = data.stores,
            countries = data.countries?.split(",")?.map { it.trim() },
            nutriScore = data.nutriscore,
            novaGroup = data.novaGroup,
            aiSummary = data.aiSummary
        )
    }

    private fun mapCachedToProduct(cached: com.example.halalyticscompose.data.local.Entities.CachedScanResult): Product {
        return Product(
            id = cached.id,
            barcode = cached.barcode ?: "",
            name = cached.productName,
            brand = "Cached Brand",
            category = "Cached Category",
            image = cached.imageUrl ?: "",
            halalInfo = HalalInfo(
                halalStatus = HalalStatus.fromString(cached.halalStatus),
                certificateNumber = null,
                certificationBody = null,
                validUntil = null,
                lastChecked = java.util.Date(cached.scannedAt).toString(),
                source = "offline_mode"
            )
        )
    }
    
    private fun mapOpenFoodFactsToProduct(offProduct: OpenFoodFactsProduct, barcode: String): Product {
        return Product(
            id = offProduct.id.hashCode(),
            barcode = barcode,
            name = offProduct.product_name ?: offProduct.generic_name ?: "Unknown Product",
            brand = offProduct.brands ?: "Unknown Brand",
            category = offProduct.categories_tags?.firstOrNull() ?: "Unknown Category",
            image = offProduct.image_front_url ?: offProduct.image_url,
            halalInfo = HalalInfo(
                halalStatus = HalalStatus.UNKNOWN,
                certificateNumber = "",
                certificationBody = "",
                validUntil = "",
                lastChecked = "",
                source = "open_food_facts"
            ),
            nutriScore = offProduct.nutriscore_grade,
            ingredientsText = offProduct.ingredients_text,
            novaGroup = offProduct.nova_group,
            imageUrl = offProduct.image_url,
            imageFrontUrl = offProduct.image_front_url,
            imageIngredientsUrl = offProduct.image_ingredients_url,
            imageNutritionUrl = offProduct.image_nutrition_url
        )
    }

    private fun mapProductItemToProduct(item: ProductItem): Product {
        return Product(
            id = item.id?.hashCode() ?: 0,
            barcode = item.code ?: "",
            name = item.getDisplayName(),
            brand = item.brands ?: "Unknown Brand",
            category = item.categories ?: "Unknown Category",
            image = item.getBestImageUrl(),
            halalInfo = HalalInfo(
                halalStatus = HalalStatus.fromString(item.getHalalStatus()),
                certificateNumber = null,
                certificationBody = null,
                validUntil = null,
                lastChecked = null,
                source = "halalytics_proxy"
            ),
            nutriScore = item.nutriscoreGrade,
            ingredientsText = item.ingredientsText,
            quantity = item.quantity,
            imageUrl = item.imageUrl,
            imageFrontUrl = item.imageFrontUrl,
            brandsTags = item.brandsTags,
            categoriesTags = item.categoriesTags,
            labelsTags = item.labelsTags,
            novaGroup = item.novaGroup,
            halalNotes = item.halalAnalysis?.recommendation
        )
    }

    suspend fun checkHalalStatus(
        barcode: String,
        productName: String,
        brand: String?
    ): Result<HalalInfo> {
        return try {
            val response = apiService.checkHalal(
                HalalCheckRequest(barcode, productName, brand)
            )
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!
                val halalInfo = HalalInfo(
                    halalStatus = HalalStatus.fromString(data.halal_status),
                    certificateNumber = data.certificate_number,
                    certificationBody = data.certification_body,
                    validUntil = data.valid_until,
                    lastChecked = data.last_checked,
                    source = data.source
                )
                Result.success(halalInfo)
            } else {
                Result.failure(Exception("Failed to check halal status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductAlternatives(barcode: String, token: String? = null): Result<HalalAlternativeResponse> {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val bearerToken = if (token != null && !token.startsWith("Bearer ")) "Bearer $token" else token ?: ""
                val response = apiService.getProductAlternatives(barcode, bearerToken)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.success(body.data)
                    } else {
                        Result.failure(Exception(body?.message ?: "Alternative data is null"))
                    }
                } else {
                    Result.failure(Exception("Failed to get alternatives: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
