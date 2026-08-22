package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AccountInfo(
    val username: String? = null,
    val serverHost: String? = null,
    val status: String = "Aktif",
    val expDateTimestamp: Long? = null, // in milliseconds
    val maxConnections: String? = null,
    val activeConnections: String? = null,
    val isTrial: Boolean = false,
    val rawMessage: String? = null
) {
    val isExpired: Boolean
        get() {
            val exp = expDateTimestamp ?: return false
            return exp > 0 && exp <= System.currentTimeMillis()
        }

    val remainingTimeText: String
        get() {
            val exp = expDateTimestamp ?: return "Süresiz / Belirtilmemiş"
            if (exp <= 0L) return "Süresiz"
            val now = System.currentTimeMillis()
            val diff = exp - now
            if (diff <= 0) {
                return "⚠️ Abonelik Süresi Doldu"
            }
            val totalSeconds = diff / 1000L
            val days = totalSeconds / (24 * 3600)
            val hours = (totalSeconds % (24 * 3600)) / 3600
            val minutes = (totalSeconds % 3600) / 60

            return when {
                days > 0 -> "$days Gün, $hours Saat kaldı"
                hours > 0 -> "$hours Saat, $minutes Dakika kaldı"
                else -> "$minutes Dakika kaldı"
            }
        }

    val expirationDateText: String
        get() {
            val exp = expDateTimestamp ?: return "Süresiz / Belirtilmemiş"
            if (exp <= 0L) return "Süresiz"
            return try {
                val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("tr", "TR"))
                sdf.format(Date(exp))
            } catch (e: Exception) {
                "Bilinmiyor"
            }
        }
}
