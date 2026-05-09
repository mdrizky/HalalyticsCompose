package com.example.halalyticscompose.data.model

import com.google.gson.annotations.SerializedName

data class CategoryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<CategoryItem>
)

data class CategoryItem(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("icon") val icon: String,
    @SerializedName("slug") val slug: String
)
