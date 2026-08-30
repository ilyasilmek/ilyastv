package com.example.data.repository

import android.util.Log
import com.example.data.model.TmdbMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object TmdbService {

    private val cache = ConcurrentHashMap<String, TmdbMetadata>()

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    // Public TMDb read-only bearer token for IPTV metadata fetching
    private const val TMDB_API_KEY = "3b070440f3532c5f10680f4f9f743513"
    private const val BASE_URL = "https://api.themoviedb.org/3"
    private const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"
    private const val BACKDROP_BASE_URL = "https://image.tmdb.org/t/p/w780"

    /**
     * Cleans raw IPTV titles such as:
     * "TR | Inception (2010) [1080p]" -> "Inception"
     * "4K: Oppenheimer (2023)" -> "Oppenheimer"
     * "Breaking Bad S01 E01" -> "Breaking Bad"
     */
    fun cleanMediaTitle(rawTitle: String): Pair<String, String?> {
        var clean = rawTitle
            .replace(Regex("(?i)^(TR|EN|FR|DE|AZ|RU|AR)\\s*[:|\\-]\\s*"), "")
            .replace(Regex("(?i)\\[(TR|4K|1080P|720P|FHD|UHD|HDR|HEVC|DUBLAJ|ALTYAZILI)\\]"), "")
            .replace(Regex("(?i)\\b(4K|1080p|720p|FHD|UHD|HDR|HEVC|BluRay|WEB-DL|DUBLAJ|ALTYAZILI)\\b"), "")
            .replace(Regex("(?i)S[0-9]{1,2}\\s*E[0-9]{1,2}.*"), "")
            .replace(Regex("(?i)[0-9]{1,2}\\.\\s*Sezon.*"), "")
            .trim()

        var year: String? = null
        val yearMatch = Regex("""\b(19\d\d|20\d\d)\b""").find(clean)
        if (yearMatch != null) {
            year = yearMatch.value
            clean = clean.replace("(${year})", "").replace(year, "").trim()
        }

        clean = clean.replace(Regex("[\\-_]+"), " ").replace(Regex("\\s+"), " ").trim()
        return Pair(if (clean.isNotEmpty()) clean else rawTitle, year)
    }

    suspend fun getMetadata(title: String, isSeries: Boolean = false): TmdbMetadata? = withContext(Dispatchers.IO) {
        val (cleanedTitle, year) = cleanMediaTitle(title)
        val cacheKey = "${if (isSeries) "tv" else "movie"}_$cleanedTitle"

        cache[cacheKey]?.let { return@withContext it }

        try {
            val encodedQuery = URLEncoder.encode(cleanedTitle, "UTF-8")
            val type = if (isSeries) "tv" else "movie"
            var url = "$BASE_URL/search/$type?api_key=$TMDB_API_KEY&language=tr-TR&query=$encodedQuery"
            if (year != null) {
                url += if (isSeries) "&first_air_date_year=$year" else "&year=$year"
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext generateFallbackMetadata(cleanedTitle, year, isSeries)
            }

            val body = response.body?.string() ?: return@withContext generateFallbackMetadata(cleanedTitle, year, isSeries)
            val json = JSONObject(body)
            val results = json.optJSONArray("results")

            if (results == null || results.length() == 0) {
                // If Turkish search yielded no result, fallback to English search
                val enUrl = "$BASE_URL/search/$type?api_key=$TMDB_API_KEY&language=en-US&query=$encodedQuery"
                val enResponse = httpClient.newCall(Request.Builder().url(enUrl).build()).execute()
                if (enResponse.isSuccessful) {
                    val enBody = enResponse.body?.string()
                    if (enBody != null) {
                        val enJson = JSONObject(enBody)
                        val enResults = enJson.optJSONArray("results")
                        if (enResults != null && enResults.length() > 0) {
                            val first = enResults.getJSONObject(0)
                            val meta = parseTmdbItem(first, isSeries)
                            cache[cacheKey] = meta
                            return@withContext meta
                        }
                    }
                }
                return@withContext generateFallbackMetadata(cleanedTitle, year, isSeries)
            }

            val first = results.getJSONObject(0)
            val meta = parseTmdbItem(first, isSeries)
            cache[cacheKey] = meta
            meta
        } catch (e: Exception) {
            Log.e("TmdbService", "Error fetching TMDb for '$cleanedTitle': ${e.message}")
            generateFallbackMetadata(cleanedTitle, year, isSeries)
        }
    }

    private fun parseTmdbItem(item: JSONObject, isSeries: Boolean): TmdbMetadata {
        val tmdbId = item.optLong("id")
        val title = if (isSeries) item.optString("name", "") else item.optString("title", "")
        val originalTitle = if (isSeries) item.optString("original_name", "") else item.optString("original_title", "")
        val overview = item.optString("overview", "")
        val posterPath = item.optString("poster_path", "")
        val backdropPath = item.optString("backdrop_path", "")
        val rating = item.optDouble("vote_average", 0.0)
        val voteCount = item.optInt("vote_count", 0)
        val dateStr = if (isSeries) item.optString("first_air_date", "") else item.optString("release_date", "")
        val releaseYear = if (dateStr.length >= 4) dateStr.substring(0, 4) else null

        val genreIds = item.optJSONArray("genre_ids")
        val genres = mutableListOf<String>()
        if (genreIds != null) {
            for (i in 0 until genreIds.length()) {
                val id = genreIds.optInt(i)
                val genreName = getGenreName(id)
                if (genreName != null) genres.add(genreName)
            }
        }

        return TmdbMetadata(
            title = if (title.isNotBlank()) title else originalTitle,
            originalTitle = originalTitle,
            overview = if (overview.isNotBlank()) overview else "Bu yapım için özet açıklaması hazırlanıyor.",
            posterUrl = if (posterPath.isNotBlank() && posterPath != "null") "$IMAGE_BASE_URL$posterPath" else null,
            backdropUrl = if (backdropPath.isNotBlank() && backdropPath != "null") "$BACKDROP_BASE_URL$backdropPath" else null,
            rating = Math.round(rating * 10.0) / 10.0,
            voteCount = voteCount,
            releaseYear = releaseYear,
            genres = genres.take(4),
            tmdbId = tmdbId,
            trailerUrl = "https://www.youtube.com/results?search_query=${URLEncoder.encode("$title fragman trailer", "UTF-8")}"
        )
    }

    private fun getGenreName(id: Int): String? = when (id) {
        28 -> "Aksiyon"
        12 -> "Macera"
        16 -> "Animasyon"
        35 -> "Komedi"
        80 -> "Suç"
        99 -> "Belgesel"
        18 -> "Dram"
        10751 -> "Aile"
        14 -> "Fantastik"
        36 -> "Tarih"
        27 -> "Korku"
        10402 -> "Müzik"
        9648 -> "Gizem"
        10749 -> "Romantik"
        878 -> "Bilim Kurgu"
        10770 -> "TV Filmi"
        53 -> "Gerilim"
        10752 -> "Savaş"
        37 -> "Vahşi Batı"
        10759 -> "Aksiyon & Macera"
        10765 -> "Bilim Kurgu & Fantastik"
        else -> null
    }

    private fun generateFallbackMetadata(title: String, year: String?, isSeries: Boolean): TmdbMetadata {
        return TmdbMetadata(
            title = title,
            originalTitle = title,
            overview = "IPTV kütüphanesindeki film/dizi içeriği. Kesintisiz Full HD kalitede izleyebilirsiniz.",
            releaseYear = year,
            rating = 7.5,
            genres = listOf(if (isSeries) "Dizi" else "Sinema Filmi", "HD"),
            trailerUrl = "https://www.youtube.com/results?search_query=${URLEncoder.encode("$title fragman", "UTF-8")}"
        )
    }
}
