package com.example.data.repository

import com.example.data.model.ChannelItem
import com.example.data.model.EpgProgramItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object CatchupHelper {

    /**
     * Builds a catch-up (timeshift) stream URL for a given program
     */
    fun buildCatchupUrl(
        channel: ChannelItem,
        programStartTimeMs: Long,
        durationMinutes: Long
    ): String {
        val originalUrl = channel.streamUrl
        val startSec = programStartTimeMs / 1000
        val durationMin = durationMinutes.coerceAtLeast(5)

        val xtreamMatch = Regex("""^(https?://[^/]+)/(?:live/)?([^/]+)/([^/]+)/(\d+)(?:\.ts|\.m3u8)?""").find(originalUrl)
        if (xtreamMatch != null) {
            val host = xtreamMatch.groupValues[1]
            val user = xtreamMatch.groupValues[2]
            val pass = xtreamMatch.groupValues[3]
            val streamId = xtreamMatch.groupValues[4]

            val sdf = SimpleDateFormat("yyyy-MM-dd:HH-mm", Locale.US)
            val startTimeStr = sdf.format(Date(programStartTimeMs))

            return "$host/timeshift/$user/$pass/$durationMin/$startTimeStr/$streamId.ts"
        }

        // Generic / Flussonic / HLS catchup append
        val separator = if (originalUrl.contains("?")) "&" else "?"
        return "$originalUrl${separator}utc=$startSec&lutc=${System.currentTimeMillis() / 1000}"
    }

    /**
     * Generates a 3-day EPG program schedule (Yesterday, Today, Tomorrow)
     * with Catch-Up capability for past broadcasts.
     */
    fun generateScheduleForChannel(channel: ChannelItem): List<EpgProgramItem> {
        val programs = mutableListOf<EpgProgramItem>()
        val now = System.currentTimeMillis()

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 6)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.add(Calendar.DAY_OF_YEAR, -1) // Start from yesterday 06:00 AM

        val sampleTitles = when {
            channel.name.contains("Spor", ignoreCase = true) || channel.name.contains("Sport", ignoreCase = true) -> listOf(
                "Süper Lig Maç Özetleri & Analiz",
                "Canlı Maç Önü & Özel Röportajlar",
                "Derbi Özel & Taktik Masası",
                "Avrupa Ligi Karşılaşmaları",
                "Spor Bülteni & Transfer Gündemi",
                "Dünya Kupası Klasikleri"
            )
            channel.name.contains("Belgesel", ignoreCase = true) || channel.name.contains("Doc", ignoreCase = true) || channel.name.contains("National", ignoreCase = true) -> listOf(
                "Vahşi Yaşamın Gizemleri",
                "Büyük Mühendislik Harikaları",
                "Okyanusun Derinlikleri",
                "Antik Medeniyetler ve Sırlar",
                "Evrenin Oluşumu & Kara Delikler",
                "Kutup Kaşifleri"
            )
            channel.name.contains("Sinema", ignoreCase = true) || channel.name.contains("Film", ignoreCase = true) || channel.name.contains("Movie", ignoreCase = true) -> listOf(
                "Gece Sineması: Başyapıtlar Kuşağı",
                "Öğle Kuşağı: Aile Komedisi",
                "Akşam Kuşağı: Aksiyon & Macera",
                "Prime Time: Yılın En İyi Filmi",
                "Kült Sinema: Klasikler",
                "Gerilim & Suç Gecesi"
            )
            channel.name.contains("Haber", ignoreCase = true) || channel.name.contains("News", ignoreCase = true) -> listOf(
                "Güne Bakış & Sabah Haberleri",
                "Öğle Ajansı & Ekonomi Bülteni",
                "Günün Raporu & Canlı Bağlantılar",
                "Ana Haber Bülteni (HD)",
                "Gece Raporu & Manşetler",
                "Dünya Bülteni & Dış Politika"
            )
            else -> listOf(
                "Günün İlk Işıkları & Magazin",
                "Öğle Kuşağı: Eğlence & Yaşam",
                "Haber & Güncel Gelişmeler",
                "Akşamın Yıldızları: Prime Time Show",
                "Gece Kuşağı: Talk Show & Sohbet",
                "Müzik & Klip Saati"
            )
        }

        var cursorTime = cal.timeInMillis
        val endLimit = now + (24 * 3600 * 1000L) // up to tomorrow night

        var index = 0
        while (cursorTime < endLimit) {
            val durationMinutes = listOf(45L, 60L, 90L, 120L)[index % 4]
            val durationMs = durationMinutes * 60 * 1000L
            val endTime = cursorTime + durationMs

            val isPast = endTime < now
            val isNow = cursorTime <= now && now < endTime
            val isFuture = cursorTime > now

            val title = sampleTitles[index % sampleTitles.size]
            val catchupUrl = if (isPast) buildCatchupUrl(channel, cursorTime, durationMinutes) else null

            programs.add(
                EpgProgramItem(
                    title = title,
                    description = "$title - ${channel.name} ekranlarında yayınlanan özel program.",
                    startTimeMillis = cursorTime,
                    endTimeMillis = endTime,
                    isPast = isPast,
                    isNow = isNow,
                    isFuture = isFuture,
                    catchupUrl = catchupUrl
                )
            )

            cursorTime = endTime
            index++
        }

        return programs
    }
}
