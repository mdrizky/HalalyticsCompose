package com.example.halalyticscompose.data.model

import com.google.gson.annotations.SerializedName

data class BloodEvent(
    val id: Int,
    val title: String,
    val description: String?,
    @SerializedName("event_date") val eventDate: String,
    val location: String,
    val image: String?,
    @SerializedName("is_full") val isFull: Boolean = false
)

data class BloodStock(
    @SerializedName("blood_type") val bloodType: String,
    val units: Int,
    val status: String // "Safe", "Critical", etc.
)

data class DonorCard(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("blood_type") val bloodType: String?,
    @SerializedName("total_donations") val totalDonations: Int?,
    @SerializedName("is_voluntary_donor") val isVoluntaryDonor: Boolean = false,
    @SerializedName("next_eligible_date") val nextEligibleDate: String?,
    @SerializedName("last_donation_date") val lastDonationDate: String?
)

data class DonorAppointment(
    val id: Int,
    @SerializedName("event_id") val eventId: Int,
    val status: String, // "Scheduled", "Completed", "Cancelled"
    @SerializedName("appointment_date") val appointmentDate: String,
    val event: BloodEvent?
)

data class EmergencyBloodRequest(
    val id: Int,
    @SerializedName("blood_type") val bloodType: String,
    val hospital: String,
    val reason: String?,
    @SerializedName("created_at") val createdAt: String
)
