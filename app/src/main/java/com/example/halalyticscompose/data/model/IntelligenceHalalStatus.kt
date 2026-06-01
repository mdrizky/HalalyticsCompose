package com.example.halalyticscompose.data.model

enum class IntelligenceHalalStatus {
    HALAL,
    SYUBHAT,
    HARAM,
    UNKNOWN;

    companion object {
        fun fromString(value: String): IntelligenceHalalStatus {
            return when (value.uppercase()) {
                "HALAL" -> HALAL
                "SYUBHAT" -> SYUBHAT
                "HARAM" -> HARAM
                else -> UNKNOWN
            }
        }
    }
}
