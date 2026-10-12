package com.uragontv.app.data.parser

import com.uragontv.app.data.model.Channel
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.UUID

object M3uParser {

    /**
     * Parses standard M3U/M3U8 playlist format content.
     */
    fun parseM3u(inputStream: InputStream): List<Channel> {
        val channels = mutableListOf<Channel>()
        val reader = BufferedReader(InputStreamReader(inputStream))
        var line: String?

        var currentTvgId: String? = null
        var currentTvgName: String? = null
        var currentLogo: String? = null
        var currentGroup: String? = null
        var currentTitle: String? = null

        while (reader.readLine().also { line = it } != null) {
            val trimmed = line?.trim() ?: continue
            if (trimmed.isEmpty()) continue

            if (trimmed.startsWith("#EXTINF:", ignoreCase = true)) {
                // Parse attributes
                currentTvgId = extractAttribute(trimmed, "tvg-id")
                currentTvgName = extractAttribute(trimmed, "tvg-name")
                currentLogo = extractAttribute(trimmed, "tvg-logo")
                currentGroup = extractAttribute(trimmed, "group-title")

                // Extract title after last comma
                val commaIndex = trimmed.lastIndexOf(',')
                currentTitle = if (commaIndex != -1 && commaIndex < trimmed.length - 1) {
                    trimmed.substring(commaIndex + 1).trim()
                } else {
                    currentTvgName ?: "Live Channel"
                }
            } else if (!trimmed.startsWith("#")) {
                // This is the stream URL
                val streamUrl = trimmed
                val name = currentTitle ?: currentTvgName ?: "Channel ${channels.size + 1}"
                val id = currentTvgId?.takeIf { it.isNotBlank() }
                    ?: (name.lowercase().replace("\\s+".toRegex(), "-") + "-" + channels.size)
                val category = currentGroup?.takeIf { it.isNotBlank() } ?: categorizeByName(name)
                val region = inferRegion(name, category)
                val logo = currentLogo?.takeIf { it.isNotBlank() } ?: defaultLogoFor(category)

                channels.add(
                    Channel(
                        id = id,
                        name = name,
                        logoUrl = logo,
                        streamUrl = streamUrl,
                        category = category,
                        region = region,
                        isFavorite = false,
                        streamType = if (streamUrl.contains(".mpd")) "DASH" else "HLS",
                        resolution = "1080p FHD"
                    )
                )

                // Reset for next channel
                currentTvgId = null
                currentTvgName = null
                currentLogo = null
                currentGroup = null
                currentTitle = null
            }
        }
        return channels
    }

    /**
     * Parses JSON array format of channels from assets or remote endpoint.
     */
    fun parseJson(jsonString: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id", UUID.randomUUID().toString())
                val name = obj.optString("name", "Channel $i")
                val logoUrl = obj.optString("logoUrl", defaultLogoFor("General"))
                val streamUrl = obj.optString("streamUrl", "")
                val category = obj.optString("category", "General")
                val region = obj.optString("region", "Nationwide")
                val isFav = obj.optBoolean("isFavorite", false)

                if (streamUrl.isNotBlank()) {
                    channels.add(
                        Channel(
                            id = id,
                            name = name,
                            logoUrl = logoUrl,
                            streamUrl = streamUrl,
                            category = category,
                            region = region,
                            isFavorite = isFav,
                            streamType = if (streamUrl.contains(".mpd")) "DASH" else "HLS",
                            resolution = "1080p FHD"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return channels
    }

    private fun extractAttribute(line: String, attr: String): String? {
        val regex = Regex("$attr=\"([^\"]*)\"", RegexOption.IGNORE_CASE)
        val match = regex.find(line)
        return match?.groupValues?.getOrNull(1)
    }

    private fun categorizeByName(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.contains("news") || lower.contains("balita") || lower.contains("anc") || lower.contains("teleradyo") -> "News 24/7"
            lower.contains("sports") || lower.contains("pba") || lower.contains("nba") || lower.contains("football") -> "Sports"
            lower.contains("cinema") || lower.contains("movie") || lower.contains("pelikula") || lower.contains("blockbuster") -> "Movies"
            lower.contains("bicol") || lower.contains("uragon") || lower.contains("cebu") || lower.contains("davao") || lower.contains("ilocos") || lower.contains("regional") -> "Regional / Local"
            lower.contains("gma") || lower.contains("tv5") || lower.contains("a2z") || lower.contains("ptv") || lower.contains("ibc") -> "National Networks"
            lower.contains("music") || lower.contains("myx") || lower.contains("pop") -> "Entertainment"
            else -> "Entertainment"
        }
    }

    private fun inferRegion(name: String, category: String): String {
        val lower = name.lowercase()
        return when {
            lower.contains("bicol") || lower.contains("uragon") || lower.contains("naga") || lower.contains("legazpi") -> "Bicol / Luzon"
            lower.contains("cebu") || lower.contains("visayas") || lower.contains("iloilo") || lower.contains("bacolod") -> "Visayas"
            lower.contains("davao") || lower.contains("mindanao") || lower.contains("cagayan") || lower.contains("zamboanga") -> "Mindanao"
            lower.contains("dw") || lower.contains("bloomberg") || lower.contains("france") || lower.contains("al jazeera") || lower.contains("bbc") -> "International"
            category.contains("Regional", ignoreCase = true) -> "Luzon / Bicol"
            else -> "Nationwide"
        }
    }

    private fun defaultLogoFor(category: String): String {
        return when {
            category.contains("News", ignoreCase = true) -> "https://images.unsplash.com/photo-1504711434969-e33886168f5c?w=320"
            category.contains("Sports", ignoreCase = true) -> "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=320"
            category.contains("Movies", ignoreCase = true) -> "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=320"
            category.contains("Regional", ignoreCase = true) -> "https://images.unsplash.com/photo-1518791841217-8f162f1e1131?w=320"
            else -> "https://images.unsplash.com/photo-1594909122845-11baa439b7bf?w=320"
        }
    }
}
