package com.example.data.sample

import com.example.data.model.ChannelItem
import com.example.data.model.PlaylistItem

object SampleData {
    val defaultPlaylist = PlaylistItem(
        id = 1,
        name = "İlyasTV Demo Liste",
        urlOrPath = "https://iptv-org.github.io/iptv/index.m3u",
        isLocalFile = false,
        channelCount = 8
    )

    val sampleChannels = listOf(
        ChannelItem(
            id = 1,
            playlistId = 1,
            name = "Global Sports Network",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            logoUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=200&q=80",
            groupTitle = "Sports",
            tvgId = "GSN.sports",
            tvgName = "Global Sports",
            currentProgram = "Premier League: Arsenal vs Chelsea",
            programTime = "7:00 PM - 9:30 PM",
            quality = "1080p",
            isFavorite = true,
            posterUrl = "https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=800&q=80"
        ),
        ChannelItem(
            id = 2,
            playlistId = 1,
            name = "World News 24",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            logoUrl = "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=200&q=80",
            groupTitle = "News",
            tvgId = "WN24.news",
            tvgName = "World News",
            currentProgram = "Evening Edition: Global Markets",
            programTime = "8:00 PM - 9:00 PM",
            quality = "4K",
            isFavorite = false,
            posterUrl = "https://images.unsplash.com/photo-1504711434969-e33886168f5c?w=800&q=80"
        ),
        ChannelItem(
            id = 3,
            playlistId = 1,
            name = "Sci-Fi Network HD",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            logoUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=200&q=80",
            groupTitle = "Movies",
            tvgId = "SCIFI.movie",
            tvgName = "Sci-Fi Network",
            currentProgram = "Cosmic Horizons - Ep. 4 \"The Nebula Drift\"",
            programTime = "8:30 PM - 10:00 PM",
            quality = "1080p",
            isFavorite = true,
            posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800&q=80"
        ),
        ChannelItem(
            id = 4,
            playlistId = 1,
            name = "Cinema Premiere",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            logoUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=200&q=80",
            groupTitle = "Movies",
            tvgId = "CINEMA.movie",
            tvgName = "Cinema Premiere",
            currentProgram = "Blade Runner 2049",
            programTime = "7:15 PM - 10:00 PM",
            quality = "HDR",
            isFavorite = true,
            posterUrl = "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?w=800&q=80"
        ),
        ChannelItem(
            id = 5,
            playlistId = 1,
            name = "Earth Discovery",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
            logoUrl = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=200&q=80",
            groupTitle = "Documentary",
            tvgId = "EARTH.doc",
            tvgName = "Earth Discovery",
            currentProgram = "Planet Earth: Jungles & Oceans",
            programTime = "8:00 PM - 9:00 PM",
            quality = "4K",
            isFavorite = false,
            posterUrl = "https://images.unsplash.com/photo-1511497584788-87676104235f?w=800&q=80"
        ),
        ChannelItem(
            id = 6,
            playlistId = 1,
            name = "Red Bull Extreme Live",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            logoUrl = "https://images.unsplash.com/photo-1517649763962-0c623266ddc0?w=200&q=80",
            groupTitle = "Sports",
            tvgId = "REDBULL.sports",
            tvgName = "Red Bull Extreme",
            currentProgram = "Downhill Mountain Bike World Cup",
            programTime = "6:00 PM - 8:30 PM",
            quality = "1080p",
            isFavorite = false,
            posterUrl = "https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=800&q=80"
        ),
        ChannelItem(
            id = 7,
            playlistId = 1,
            name = "Animation Central",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            logoUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=200&q=80",
            groupTitle = "Kids",
            tvgId = "ANIM.kids",
            tvgName = "Animation Central",
            currentProgram = "Fantastic Adventures Season 2",
            programTime = "5:00 PM - 7:00 PM",
            quality = "1080p",
            isFavorite = false,
            posterUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?w=800&q=80"
        ),
        ChannelItem(
            id = 8,
            playlistId = 1,
            name = "Neon Beats Music TV",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            logoUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=200&q=80",
            groupTitle = "Music",
            tvgId = "NEON.music",
            tvgName = "Neon Beats",
            currentProgram = "Electronic Dance & Synthwave Top 50",
            programTime = "9:00 PM - 11:30 PM",
            quality = "1080p",
            isFavorite = false,
            posterUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=800&q=80"
        )
    )
}
