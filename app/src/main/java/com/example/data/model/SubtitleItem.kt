package com.example.data.model

data class SubtitleItem(
    val id: String,
    val title: String,
    val language: String,
    val languageCode: String,
    val downloadUrl: String,
    val sourceName: String = "OpenSubtitles / SubDL",
    val rating: String? = null,
    val isHearingImpaired: Boolean = false
)
