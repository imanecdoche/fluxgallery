package com.flux.gallery.data.model

import android.net.Uri

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val dateTaken: Long,
    val mimeType: String,
    val isVideo: Boolean,
    val durationMs: Long = 0L,
    val relativePath: String = "",
    val fullPath: String = "",
    val bucketName: String = "",
    val sizeBytes: Long = 0L,
    val width: Int = 0,
    val height: Int = 0
) {
    val megapixels: String
        get() {
            if (width <= 0 || height <= 0) return ""
            val mp = (width.toLong() * height.toLong()) / 1_000_000.0
            return String.format(java.util.Locale.US, "%.0fMP", mp)
        }

    val formattedSize: String
        get() {
            if (sizeBytes <= 0) return ""
            val kb = sizeBytes / 1024.0
            val mb = kb / 1024.0
            return if (mb >= 1.0) {
                String.format(java.util.Locale.GERMANY, "%.2f MB", mb)
            } else {
                String.format(java.util.Locale.GERMANY, "%.1f KB", kb)
            }
        }
}

data class MediaDateGroup(
    val title: String,
    val locationSubtext: String? = null,
    val items: List<MediaItem>
)
