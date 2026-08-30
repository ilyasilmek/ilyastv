package com.example.data.model

data class TmdbMetadata(
    val title: String,
    val originalTitle: String? = null,
    val overview: String? = null,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val rating: Double = 0.0,
    val voteCount: Int = 0,
    val releaseYear: String? = null,
    val genres: List<String> = emptyList(),
    val runtimeMinutes: Int? = null,
    val director: String? = null,
    val cast: List<String> = emptyList(),
    val tmdbId: Long? = null,
    val trailerUrl: String? = null
)
