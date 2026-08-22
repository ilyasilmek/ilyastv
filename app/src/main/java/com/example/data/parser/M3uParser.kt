package com.example.data.parser

import android.net.Uri
import com.example.data.model.AccountInfo
import com.example.data.model.ChannelItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.StringReader
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class ParseResult(
    val channels: List<ChannelItem>,
    val accountInfo: AccountInfo
)

object M3uParser {

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    private val TVG_ID_REGEX = Pattern.compile("""tvg-id="([^"]*)"""", Pattern.CASE_INSENSITIVE)
    private val TVG_NAME_REGEX = Pattern.compile("""tvg-name="([^"]*)"""", Pattern.CASE_INSENSITIVE)
    private val TVG_LOGO_REGEX = Pattern.compile("""tvg-logo="([^"]*)"""", Pattern.CASE_INSENSITIVE)
    private val GROUP_TITLE_REGEX = Pattern.compile("""group-title="([^"]*)"""", Pattern.CASE_INSENSITIVE)
    private val EXP_DATE_REGEX = Pattern.compile("""(?:exp_date|exp-date|x-exp|expiry)=["']?([0-9]+)["']?""", Pattern.CASE_INSENSITIVE)

    suspend fun parseFromUrl(url: String, playlistId: Long = 0): ParseResult = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "StreamFlow-IPTV/1.0 (Android; ExoPlayer)")
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("Oynatma listesi indirilemedi: HTTP ${response.code}")
        }
        val responseBody = response.body?.byteStream() ?: throw Exception("Boş sunucu yanıtı")
        parseFromInputStream(responseBody, playlistId, sourceUrl = url)
    }

    suspend fun parseFromInputStream(
        inputStream: InputStream,
        playlistId: Long = 0,
        sourceUrl: String? = null
    ): ParseResult = withContext(Dispatchers.IO) {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        parseReader(reader, playlistId, sourceUrl)
    }

    suspend fun parseFromString(
        content: String,
        playlistId: Long = 0,
        sourceUrl: String? = null
    ): ParseResult = withContext(Dispatchers.IO) {
        val reader = BufferedReader(StringReader(content))
        parseReader(reader, playlistId, sourceUrl)
    }

    private suspend fun parseReader(
        reader: BufferedReader,
        playlistId: Long,
        sourceUrl: String?
    ): ParseResult {
        val channels = mutableListOf<ChannelItem>()
        var line: String?
        var currentExtInf: String? = null
        var headerExpTimestamp: Long? = null

        var detectedHost: String? = null
        var detectedUser: String? = null
        var detectedPass: String? = null

        // Check if sourceUrl itself has credentials
        if (!sourceUrl.isNullOrBlank()) {
            val creds = extractCredentialsFromUrl(sourceUrl)
            if (creds != null) {
                detectedHost = creds.host
                detectedUser = creds.username
                detectedPass = creds.password
            }
        }

        while (reader.readLine().also { line = it } != null) {
            val trimmed = line?.trim() ?: continue
            if (trimmed.isEmpty()) continue

            if (trimmed.startsWith("#EXTM3U", ignoreCase = true)) {
                val expMatcher = EXP_DATE_REGEX.matcher(trimmed)
                if (expMatcher.find()) {
                    val rawNum = expMatcher.group(1)?.toLongOrNull()
                    if (rawNum != null) {
                        headerExpTimestamp = if (rawNum < 100000000000L) rawNum * 1000L else rawNum
                    }
                }
            } else if (trimmed.startsWith("#EXTINF:", ignoreCase = true)) {
                currentExtInf = trimmed
                val expMatcher = EXP_DATE_REGEX.matcher(trimmed)
                if (expMatcher.find() && headerExpTimestamp == null) {
                    val rawNum = expMatcher.group(1)?.toLongOrNull()
                    if (rawNum != null) {
                        headerExpTimestamp = if (rawNum < 100000000000L) rawNum * 1000L else rawNum
                    }
                }
            } else if (!trimmed.startsWith("#")) {
                // Stream URL
                val streamUrl = trimmed
                if (detectedUser == null || detectedPass == null || detectedHost == null) {
                    val creds = extractCredentialsFromUrl(streamUrl)
                    if (creds != null) {
                        detectedHost = creds.host
                        detectedUser = creds.username
                        detectedPass = creds.password
                    }
                }

                if (currentExtInf != null) {
                    val channel = parseExtInfLine(currentExtInf, streamUrl, playlistId)
                    channels.add(channel)
                    currentExtInf = null
                } else if (streamUrl.startsWith("http://") || streamUrl.startsWith("https://") || streamUrl.startsWith("rtmp://")) {
                    val name = streamUrl.substringAfterLast("/").substringBefore(".").ifEmpty { "Kanal ${channels.size + 1}" }
                    val streamType = determineStreamType(null, streamUrl, name)
                    channels.add(
                        ChannelItem(
                            playlistId = playlistId,
                            name = name,
                            streamUrl = streamUrl,
                            groupTitle = "Genel",
                            streamType = streamType,
                            quality = "1080p"
                        )
                    )
                }
            }
        }

        // Now attempt to query Xtream API for official account expiration & user info if credentials exist
        var fetchedAccountInfo: AccountInfo? = null
        if (detectedHost != null && detectedUser != null && detectedPass != null) {
            fetchedAccountInfo = fetchXtreamAccountInfo(detectedHost, detectedUser, detectedPass)
        }

        val finalAccountInfo = fetchedAccountInfo ?: AccountInfo(
            username = detectedUser ?: (if (sourceUrl != null) "M3U Linki" else "Yerel Dosya"),
            serverHost = detectedHost ?: (sourceUrl?.let { try { Uri.parse(it).host } catch (e: Exception) { null } }),
            status = "Aktif",
            expDateTimestamp = headerExpTimestamp,
            maxConnections = "1",
            activeConnections = "1",
            isTrial = false
        )

        return ParseResult(channels, finalAccountInfo)
    }

    private data class XtreamCredentials(val host: String, val username: String, val password: String)

    private fun extractCredentialsFromUrl(urlStr: String): XtreamCredentials? {
        try {
            val uri = Uri.parse(urlStr)
            val host = "${uri.scheme}://${uri.host}${if (uri.port != -1 && uri.port != 80 && uri.port != 443) ":${uri.port}" else ""}"

            // Format 1: query params ?username=X&password=Y
            val userParam = uri.getQueryParameter("username") ?: uri.getQueryParameter("user")
            val passParam = uri.getQueryParameter("password") ?: uri.getQueryParameter("pass")
            if (!userParam.isNullOrBlank() && !passParam.isNullOrBlank()) {
                return XtreamCredentials(host, userParam, passParam)
            }

            // Format 2: /live/username/password/ or /movie/username/password/ or /series/username/password/
            val segments = uri.pathSegments
            if (segments.size >= 3) {
                val seg0 = segments[0].lowercase()
                if (seg0 == "live" || seg0 == "movie" || seg0 == "series" || seg0 == "get.php") {
                    val user = segments[1]
                    val pass = segments[2].substringBefore(".")
                    if (user.isNotBlank() && pass.isNotBlank()) {
                        return XtreamCredentials(host, user, pass)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore parsing errors
        }
        return null
    }

    suspend fun fetchXtreamAccountInfo(host: String, user: String, pass: String): AccountInfo? = withContext(Dispatchers.IO) {
        try {
            val apiUrl = "$host/player_api.php?username=$user&password=$pass"
            val request = Request.Builder()
                .url(apiUrl)
                .header("User-Agent", "StreamFlow-IPTV/1.0 (Android; ExoPlayer)")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null
            val bodyString = response.body?.string() ?: return@withContext null

            val json = JSONObject(bodyString)
            if (!json.has("user_info")) return@withContext null

            val userInfo = json.getJSONObject("user_info")
            val username = userInfo.optString("username", user)
            val auth = userInfo.optInt("auth", 1)
            val status = when (userInfo.optString("status", "Active").lowercase()) {
                "active" -> "Aktif"
                "expired" -> "Süresi Doldu"
                "disabled" -> "Devre Dışı"
                "banned" -> "Engellendi"
                else -> "Aktif"
            }

            var expTimestamp: Long? = null
            if (userInfo.has("exp_date")) {
                val expStr = userInfo.optString("exp_date", "")
                val expNum = expStr.toLongOrNull()
                if (expNum != null && expNum > 0) {
                    expTimestamp = if (expNum < 100000000000L) expNum * 1000L else expNum
                }
            }

            val isTrial = userInfo.optString("is_trial", "0") == "1"
            val activeCons = userInfo.optString("active_cons", "1")
            val maxCons = userInfo.optString("max_connections", "1")

            var serverHost = host
            if (json.has("server_info")) {
                val serverInfo = json.getJSONObject("server_info")
                val url = serverInfo.optString("url", "")
                val port = serverInfo.optString("port", "")
                if (url.isNotBlank()) {
                    serverHost = if (port.isNotBlank()) "$url:$port" else url
                }
            }

            return@withContext AccountInfo(
                username = username,
                serverHost = serverHost,
                status = if (auth == 0) "Yetkisiz / Hatalı Şifre" else status,
                expDateTimestamp = expTimestamp,
                maxConnections = maxCons,
                activeConnections = activeCons,
                isTrial = isTrial
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseExtInfLine(extInf: String, streamUrl: String, playlistId: Long): ChannelItem {
        var tvgId: String? = null
        var tvgName: String? = null
        var tvgLogo: String? = null
        var groupTitle: String? = null

        val idMatcher = TVG_ID_REGEX.matcher(extInf)
        if (idMatcher.find()) tvgId = idMatcher.group(1)?.trim()

        val nameMatcher = TVG_NAME_REGEX.matcher(extInf)
        if (nameMatcher.find()) tvgName = nameMatcher.group(1)?.trim()

        val logoMatcher = TVG_LOGO_REGEX.matcher(extInf)
        if (logoMatcher.find()) tvgLogo = logoMatcher.group(1)?.trim()

        val groupMatcher = GROUP_TITLE_REGEX.matcher(extInf)
        if (groupMatcher.find()) groupTitle = groupMatcher.group(1)?.trim()

        val commaIndex = extInf.lastIndexOf(',')
        val displayName = if (commaIndex != -1 && commaIndex < extInf.length - 1) {
            extInf.substring(commaIndex + 1).trim()
        } else {
            tvgName ?: tvgId ?: "Kanal"
        }

        val category = cleanCategory(groupTitle, displayName)
        val streamType = determineStreamType(groupTitle, streamUrl, displayName)

        val quality = when {
            displayName.contains("4K", ignoreCase = true) || displayName.contains("UHD", ignoreCase = true) -> "4K"
            displayName.contains("HDR", ignoreCase = true) -> "HDR"
            displayName.contains("FHD", ignoreCase = true) || displayName.contains("1080", ignoreCase = true) -> "1080p"
            displayName.contains("HD", ignoreCase = true) || displayName.contains("720", ignoreCase = true) -> "720p"
            else -> "1080p"
        }

        return ChannelItem(
            playlistId = playlistId,
            name = displayName.ifEmpty { tvgName ?: "Kanal" },
            streamUrl = streamUrl,
            logoUrl = tvgLogo?.ifEmpty { null },
            groupTitle = category,
            streamType = streamType,
            tvgId = tvgId,
            tvgName = tvgName,
            quality = quality,
            currentProgram = generateTurkishProgram(displayName, category, streamType),
            programTime = if (streamType == "LIVE") "Canlı Yayın" else "İsteğe Bağlı (VOD)"
        )
    }

    fun determineStreamType(groupTitle: String?, streamUrl: String, displayName: String): String {
        val lowerUrl = streamUrl.lowercase()
        val lowerGroup = groupTitle?.lowercase() ?: ""
        val lowerName = displayName.lowercase()

        return when {
            // Filmler (Movies / VOD)
            lowerUrl.contains("/movie/") ||
            lowerGroup.contains("vod") || lowerGroup.contains("film") || lowerGroup.contains("movie") ||
            lowerGroup.contains("sinema") || lowerGroup.contains("cinema") || lowerGroup.contains("4k film") ||
            lowerGroup.contains("yerli film") || lowerGroup.contains("yabancı film") || lowerGroup.contains("vizyon") ||
            lowerName.startsWith("film:") || lowerName.startsWith("vod:") ||
            ((lowerUrl.endsWith(".mp4") || lowerUrl.endsWith(".mkv") || lowerUrl.endsWith(".avi")) && !lowerGroup.contains("live") && !lowerGroup.contains("canlı")) -> "MOVIE"

            // Diziler (Series)
            lowerUrl.contains("/series/") ||
            lowerGroup.contains("dizi") || lowerGroup.contains("series") || lowerGroup.contains("sezon") ||
            lowerGroup.contains("season") || lowerGroup.contains("episode") || lowerGroup.contains("bölüm") ||
            lowerGroup.contains("netflix") || lowerGroup.contains("exxen") || lowerGroup.contains("blutv") ||
            lowerGroup.contains("disney") || lowerGroup.contains("gain") || lowerGroup.contains("anime") ||
            lowerName.contains(" s0") || lowerName.contains(" e0") || lowerName.contains(" sezon") || lowerName.contains(" bölüm") -> "SERIES"

            // Canlı TV (Live TV)
            else -> "LIVE"
        }
    }

    private fun cleanCategory(raw: String?, displayName: String): String {
        if (raw.isNullOrBlank()) {
            val lower = displayName.lowercase()
            return when {
                lower.contains("sport") || lower.contains("spor") || lower.contains("futbol") || lower.contains("bein") -> "Spor"
                lower.contains("news") || lower.contains("haber") || lower.contains("cnn") || lower.contains("trthaber") -> "Haber"
                lower.contains("movie") || lower.contains("film") || lower.contains("sinema") || lower.contains("cinema") -> "Sinema & Film"
                lower.contains("dizi") || lower.contains("series") -> "Diziler"
                lower.contains("music") || lower.contains("müzik") || lower.contains("kral") || lower.contains("power") -> "Müzik"
                lower.contains("doc") || lower.contains("belgesel") || lower.contains("nat geo") || lower.contains("discovery") -> "Belgesel"
                lower.contains("kid") || lower.contains("çocuk") || lower.contains("cartoon") || lower.contains("disney") -> "Çocuk"
                lower.contains("ulusal") || lower.contains("trt") || lower.contains("atv") || lower.contains("kanal d") || lower.contains("star") || lower.contains("show") || lower.contains("tv8") -> "Ulusal Kanallar"
                else -> "Genel"
            }
        }

        val trimmed = raw.trim()
        val lower = trimmed.lowercase()
        return when {
            lower.contains("sport") || lower.contains("spor") -> "Spor"
            lower.contains("news") || lower.contains("haber") -> "Haber"
            lower.contains("movie") || lower.contains("film") || lower.contains("sinema") || lower.contains("cinema") -> "Sinema & Film"
            lower.contains("dizi") || lower.contains("series") -> "Diziler"
            lower.contains("music") || lower.contains("müzik") -> "Müzik"
            lower.contains("doc") || lower.contains("belgesel") || lower.contains("nat geo") || lower.contains("discovery") -> "Belgesel"
            lower.contains("kid") || lower.contains("çocuk") || lower.contains("cartoon") || lower.contains("disney") -> "Çocuk"
            lower.contains("ulusal") || lower.contains("turkey") || lower.contains("türkiye") || lower.contains("genel") -> "Ulusal Kanallar"
            else -> trimmed.replaceFirstChar { it.uppercase() }
        }
    }

    private fun generateTurkishProgram(name: String, category: String, streamType: String): String {
        return when (streamType) {
            "MOVIE" -> "Öne Çıkan Sinema Kuşağı"
            "SERIES" -> "Popüler Sezon & Bölümler"
            else -> when (category) {
                "Spor" -> "Canlı Maç & Spor Bülteni"
                "Haber" -> "Güncel Gelişmeler & Ana Haber Bülteni"
                "Sinema & Film" -> "Türk & Yabancı Sinema Kuşağı"
                "Diziler" -> "Yeni Sezon Bölümü"
                "Müzik" -> "En Çok Dinlenenler & Top 20 Müzik Listesi"
                "Belgesel" -> "Yeryüzü ve Doğa Belgeseli"
                "Çocuk" -> "Çizgi Film Kuşağı"
                "Ulusal Kanallar" -> "Canlı Yayın Akışı"
                else -> "Canlı HD Yayın"
            }
        }
    }
}

