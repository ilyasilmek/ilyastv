package com.example.data.model

data class EpgProgramItem(
    val title: String,
    val description: String? = null,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val isPast: Boolean = false,
    val isNow: Boolean = false,
    val isFuture: Boolean = false,
    val catchupUrl: String? = null
) {
    val durationMinutes: Long
        get() = ((endTimeMillis - startTimeMillis) / (60 * 1000)).coerceAtLeast(1)
}
