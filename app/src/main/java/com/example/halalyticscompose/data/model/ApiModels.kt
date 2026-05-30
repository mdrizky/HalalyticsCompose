package com.example.halalyticscompose.data.model

import com.google.gson.annotations.SerializedName

/**
 * Health and Intake related models
 * (Moved to respective model files to avoid redeclaration)
 */

/**
 * Generic API Wrapper for consistency
 */
data class GenericApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null
)
