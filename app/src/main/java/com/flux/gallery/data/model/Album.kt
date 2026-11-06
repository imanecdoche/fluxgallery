package com.flux.gallery.data.model

import android.net.Uri

data class Album(
    val id: String,
    val name: String,
    val count: Int,
    val coverUri: Uri?,
    val isSmartAlbum: Boolean = false,
    val iconRes: Int? = null,
    val items: List<MediaItem> = emptyList()
)
