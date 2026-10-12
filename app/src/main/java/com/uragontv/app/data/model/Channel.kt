package com.uragontv.app.data.model

data class Channel(
    val id: String,
    val name: String,
    val logoUrl: String,
    val streamUrl: String,
    val category: String,
    val region: String,
    val isFavorite: Boolean = false,
    val streamType: String = "HLS",
    val resolution: String = "1080p FHD"
)

enum class AspectRatioMode(val label: String) {
    FIT_16_9("16:9 Fit"),
    ZOOM_FILL("Zoom / Fill"),
    STRETCH("Stretch")
}

data class ChannelCategory(
    val title: String,
    val channels: List<Channel>
)
