package com.example.halalyticscompose.data.model

import com.google.gson.annotations.SerializedName

data class DonationCampaignDto(
    val id: Long,
    val title: String,
    val slug: String,
    val description: String?,
    val image: String?,
    @SerializedName("target_amount") val targetAmount: Double,
    @SerializedName("collected_amount") val collectedAmount: Double,
    @SerializedName("donor_count") val donorCount: Int,
    val category: String?,
    @SerializedName("is_urgent") val isUrgent: Boolean = false,
    @SerializedName("progress_percent") val progressPercent: Float = 0f
)

data class DonationCampaignsResponse(
    val success: Boolean,
    val data: List<DonationCampaignDto>?
)

data class DonationCreateRequest(
    @SerializedName("campaign_id") val campaignId: Long,
    val amount: Double,
    @SerializedName("payment_method") val paymentMethod: String = "midtrans",
    @SerializedName("is_anonymous") val isAnonymous: Boolean = false,
    @SerializedName("donor_name") val donorName: String? = null,
    @SerializedName("donor_message") val donorMessage: String? = null
)

data class DonationCreateData(
    @SerializedName("donation_id") val donationId: Long,
    @SerializedName("transaction_id") val transactionId: String,
    @SerializedName("snap_token") val snapToken: String?,
    @SerializedName("payment_url") val paymentUrl: String?,
    @SerializedName("is_mock") val isMock: Boolean = false
)

data class DonationCreateResponse(
    val success: Boolean,
    val data: DonationCreateData?
)

data class DonationHistoryItem(
    val id: Long,
    val amount: Double,
    @SerializedName("payment_status") val paymentStatus: String,
    val campaign: DonationCampaignDto?,
    @SerializedName("paid_at") val paidAt: String?,
    @SerializedName("created_at") val createdAt: String?
)

data class DonationHistoryResponse(
    val success: Boolean,
    val data: List<DonationHistoryItem>?
)

data class AiChatRequest(
    val message: String
)

data class AiChatResponse(
    val success: Boolean,
    val reply: String?
)
