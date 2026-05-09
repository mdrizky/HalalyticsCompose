package com.example.halalyticscompose.data.model

import com.google.gson.annotations.SerializedName

data class UserListResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: List<User> = emptyList()
)
