package com.example.halalyticscompose.utils

/**
 * Normalizes backend roles and maps them to navigation destinations.
 * Backend may send: admin, user, nutritionist, ahli_gizi, expert.
 */
object RoleHelper {

    fun normalizeRole(role: String?): String {
        return when (role?.trim()?.lowercase()) {
            "admin", "administrator" -> "admin"
            "nutritionist", "ahli_gizi", "ahli gizi", "expert", "pakar" -> "ahli_gizi"
            else -> "user"
        }
    }

    fun isAdmin(role: String?): Boolean = normalizeRole(role) == "admin"

    fun isNutritionist(role: String?): Boolean = normalizeRole(role) == "ahli_gizi"

    /**
     * Primary home route after splash / login.
     */
    fun homeRoute(role: String?): String = when (normalizeRole(role)) {
        "admin" -> "home"
        "ahli_gizi" -> "nutritionist_home"
        else -> "home"
    }

    fun displayName(role: String?): String = when (normalizeRole(role)) {
        "admin" -> "Administrator"
        "ahli_gizi" -> "Ahli Gizi"
        else -> "Pengguna"
    }
}
