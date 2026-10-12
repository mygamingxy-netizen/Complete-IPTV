# UragonTv - Android TV Live Streaming Platform

**UragonTv** is a high-performance native Android TV and Google TV live streaming application engineered with Kotlin, Jetpack Compose, and Media3 ExoPlayer. It features an ultra-dark luxury aesthetic with metallic gold accents, physical remote control (D-Pad) focus navigation, and dynamic M3U / JSON IPTV playlist ingestion.

## Features

- **Jetpack Compose for TV:** 100% remote control (D-Pad) friendly navigation with smooth 1.08x focus scaling, metallic warm gold glow borders, and sound feedback.
- **Media3 ExoPlayer Engine:** Full-bleed 1080p FHD / 4K video playback supporting HTTP Live Streaming (`.m3u8` HLS), MPEG-DASH (`.mpd`), and MP4 streams with automatic reconnection logic on network drops.
- **Collapsible Navigation Sidebar:** Left vertical TV menu bar (Home, Categories, Regions, Favorites, Search, Settings) that expands on D-Pad focus and collapses for distraction-free browsing.
- **Top Featured Hero Showcase:** Displays live preview frame, channel metadata, category/region tags, and quick-action buttons.
- **Dynamic M3U & JSON Ingestion:** Parse streams dynamically from local assets or remote URLs (`StreamSourceRepository`), with custom URL editing in Settings.
- **On-Screen Display (OSD) Overlay:** Activated on any D-Pad key during playback with live channel status, quick channel carousel picker, favorite toggle, and aspect ratio controls (16:9 Fit, Zoom / Fill, Stretch).
- **Favorites & Search:** Bookmark favorite channels with persistent local storage and live search with an on-screen D-Pad virtual keyboard.
- **Rich Regional & National Content:** Curated lineup for Philippine National Networks, Bicolano/Uragon Regional, News 24/7, Sports, and Movies.

## Architecture

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Media:** AndroidX Media3 ExoPlayer 1.4.1 (HLS, DASH, UI)
- **Image Loading:** Coil Compose
- **State Management:** MVVM + Kotlin StateFlow
- **Storage:** SharedPreferences / DataStore
