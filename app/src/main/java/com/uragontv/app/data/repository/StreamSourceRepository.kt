package com.uragontv.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.uragontv.app.data.model.AspectRatioMode
import com.uragontv.app.data.model.Channel
import com.uragontv.app.data.model.ChannelCategory
import com.uragontv.app.data.parser.M3uParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class StreamSourceRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("uragon_tv_prefs", Context.MODE_PRIVATE)

    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels: StateFlow<List<Channel>> = _channels.asStateFlow()

    private val _recentlyPlayed = MutableStateFlow<List<Channel>>(emptyList())
    val recentlyPlayed: StateFlow<List<Channel>> = _recentlyPlayed.asStateFlow()

    private val _customPlaylistUrl = MutableStateFlow(
        prefs.getString(KEY_PLAYLIST_URL, "") ?: ""
    )
    val customPlaylistUrl: StateFlow<String> = _customPlaylistUrl.asStateFlow()

    private val _aspectRatio = MutableStateFlow(
        AspectRatioMode.entries.getOrNull(prefs.getInt(KEY_ASPECT_RATIO, 0)) ?: AspectRatioMode.FIT_16_9
    )
    val aspectRatio: StateFlow<AspectRatioMode> = _aspectRatio.asStateFlow()

    private val _autoReconnect = MutableStateFlow(
        prefs.getBoolean(KEY_AUTO_RECONNECT, true)
    )
    val autoReconnect: StateFlow<Boolean> = _autoReconnect.asStateFlow()

    private val _hwAcceleration = MutableStateFlow(
        prefs.getBoolean(KEY_HW_ACCEL, true)
    )
    val hwAcceleration: StateFlow<Boolean> = _hwAcceleration.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            loadChannels()
        }
    }

    suspend fun loadChannels() = withContext(Dispatchers.IO) {
        _isLoading.value = true
        val favoriteIds = getSavedFavorites()
        val customUrl = _customPlaylistUrl.value

        var list: List<Channel> = emptyList()

        // 1. Try remote custom URL if provided
        if (customUrl.isNotBlank() && (customUrl.startsWith("http://") || customUrl.startsWith("https://"))) {
            list = fetchRemoteM3u(customUrl)
        }

        // 2. If empty, load from channels.json in assets
        if (list.isEmpty()) {
            try {
                val jsonStream = context.assets.open("channels.json")
                val jsonText = jsonStream.bufferedReader().use { it.readText() }
                list = M3uParser.parseJson(jsonText)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 3. If still empty, load from sources.m3u in assets
        if (list.isEmpty()) {
            try {
                val m3uStream: InputStream = context.assets.open("sources.m3u")
                list = M3uParser.parseM3u(m3uStream)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val combinedList = list.toMutableList()

        // 4. Merge Live Events from assets if available
        try {
            val liveEventsStream: InputStream = context.assets.open("live-events.m3u")
            val liveEvents = M3uParser.parseM3u(liveEventsStream)
            combinedList.addAll(liveEvents)
        } catch (e: Exception) {
            // Optional local file
        }

        // Apply saved favorites
        val merged = combinedList.distinctBy { it.id }.map { ch ->
            ch.copy(isFavorite = favoriteIds.contains(ch.id))
        }

        _channels.value = merged
        _isLoading.value = false
    }

    private fun fetchRemoteM3u(urlStr: String): List<Channel> {
        return try {
            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "UragonTv/1.0 (Android TV)")
            if (conn.responseCode in 200..299) {
                conn.inputStream.use { stream ->
                    M3uParser.parseM3u(stream)
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun toggleFavorite(channelId: String) {
        val currentFavorites = getSavedFavorites().toMutableSet()
        if (currentFavorites.contains(channelId)) {
            currentFavorites.remove(channelId)
        } else {
            currentFavorites.add(channelId)
        }
        prefs.edit().putStringSet(KEY_FAVORITES, currentFavorites).apply()

        _channels.value = _channels.value.map { ch ->
            if (ch.id == channelId) ch.copy(isFavorite = !ch.isFavorite) else ch
        }
    }

    fun recordWatchedChannel(channel: Channel) {
        val current = _recentlyPlayed.value.toMutableList()
        current.removeAll { it.id == channel.id }
        current.add(0, channel)
        if (current.size > 12) {
            current.removeAt(current.size - 1)
        }
        _recentlyPlayed.value = current
    }

    fun setCustomPlaylistUrl(url: String) {
        prefs.edit().putString(KEY_PLAYLIST_URL, url).apply()
        _customPlaylistUrl.value = url
        CoroutineScope(Dispatchers.IO).launch {
            loadChannels()
        }
    }

    fun setAspectRatio(mode: AspectRatioMode) {
        prefs.edit().putInt(KEY_ASPECT_RATIO, mode.ordinal).apply()
        _aspectRatio.value = mode
    }

    fun setAutoReconnect(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_RECONNECT, enabled).apply()
        _autoReconnect.value = enabled
    }

    fun setHwAcceleration(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HW_ACCEL, enabled).apply()
        _hwAcceleration.value = enabled
    }

    fun getCategorizedChannels(): List<ChannelCategory> {
        val all = _channels.value
        val categories = mutableListOf<ChannelCategory>()

        val national = all.filter { it.category.contains("National", ignoreCase = true) }
        if (national.isNotEmpty()) categories.add(ChannelCategory("National Networks", national))

        val liveEvents = all.filter { it.category.contains("Live Events", ignoreCase = true) || it.category.contains("Events", ignoreCase = true) }
        if (liveEvents.isNotEmpty()) categories.add(ChannelCategory("Live Events & Specials", liveEvents))

        val regional = all.filter { it.category.contains("Regional", ignoreCase = true) }
        if (regional.isNotEmpty()) categories.add(ChannelCategory("Regional Streams (Bicol & More)", regional))

        val news = all.filter { it.category.contains("News", ignoreCase = true) }
        if (news.isNotEmpty()) categories.add(ChannelCategory("News 24/7", news))

        val sports = all.filter { it.category.contains("Sports", ignoreCase = true) }
        if (sports.isNotEmpty()) categories.add(ChannelCategory("Sports & Action", sports))

        val movies = all.filter { it.category.contains("Movie", ignoreCase = true) || it.category.contains("Cinema", ignoreCase = true) }
        if (movies.isNotEmpty()) categories.add(ChannelCategory("Blockbuster Movies", movies))

        val entertainment = all.filter { it.category.contains("Entertainment", ignoreCase = true) }
        if (entertainment.isNotEmpty()) categories.add(ChannelCategory("Entertainment & Music", entertainment))

        val other = all.filter { ch -> categories.none { cat -> cat.channels.any { it.id == ch.id } } }
        if (other.isNotEmpty()) categories.add(ChannelCategory("Other Channels", other))

        return categories
    }

    fun getRegionalChannels(): Map<String, List<Channel>> {
        return _channels.value.groupBy { it.region }
    }

    fun getFavorites(): List<Channel> {
        return _channels.value.filter { it.isFavorite }
    }

    private fun getSavedFavorites(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, setOf("gma-7", "tv5-ph", "bicol-tv", "one-news", "one-sports")) ?: emptySet()
    }

    companion object {
        private const val KEY_FAVORITES = "pref_favorites"
        private const val KEY_PLAYLIST_URL = "pref_playlist_url"
        private const val KEY_ASPECT_RATIO = "pref_aspect_ratio"
        private const val KEY_AUTO_RECONNECT = "pref_auto_reconnect"
        private const val KEY_HW_ACCEL = "pref_hw_accel"
    }
}
