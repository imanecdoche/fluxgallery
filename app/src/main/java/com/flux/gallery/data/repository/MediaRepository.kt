package com.flux.gallery.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.flux.gallery.data.model.MediaDateGroup
import com.flux.gallery.data.model.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MediaRepository(private val context: Context) {

    suspend fun loadRecentMedia(): List<MediaDateGroup> = withContext(Dispatchers.IO) {
        val mediaList = mutableListOf<MediaItem>()

        // 1. Fetch Images
        val imageProjection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Images.Media.RELATIVE_PATH else MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATA
        )

        try {
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imageProjection,
                null,
                null,
                "${MediaStore.Images.Media.DATE_TAKEN} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val dateTakenCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                val dateModCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
                val sizeCol = cursor.getColumnIndex(MediaStore.Images.Media.SIZE)
                val widthCol = cursor.getColumnIndex(MediaStore.Images.Media.WIDTH)
                val heightCol = cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT)
                val dataCol = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                val relPathCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cursor.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH)
                } else -1

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: "IMG_$id"
                    var dateTaken = cursor.getLong(dateTakenCol)
                    if (dateTaken <= 0L) {
                        dateTaken = cursor.getLong(dateModCol) * 1000L
                    }
                    val mime = cursor.getString(mimeCol) ?: "image/jpeg"
                    val size = if (sizeCol != -1) cursor.getLong(sizeCol) else 0L
                    val width = if (widthCol != -1) cursor.getInt(widthCol) else 0
                    val height = if (heightCol != -1) cursor.getInt(heightCol) else 0
                    val fullPath = if (dataCol != -1) cursor.getString(dataCol) ?: "" else ""
                    val relPath = if (relPathCol != -1) cursor.getString(relPathCol) ?: "" else fullPath

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    mediaList.add(
                        MediaItem(
                            id = id,
                            uri = contentUri,
                            displayName = name,
                            dateTaken = dateTaken,
                            mimeType = mime,
                            isVideo = false,
                            relativePath = relPath,
                            fullPath = fullPath,
                            sizeBytes = size,
                            width = width,
                            height = height
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Fetch Videos
        val videoProjection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATE_TAKEN,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Video.Media.RELATIVE_PATH else MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATA
        )

        try {
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                videoProjection,
                null,
                null,
                "${MediaStore.Video.Media.DATE_TAKEN} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val dateTakenCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN)
                val dateModCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
                val durCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeCol = cursor.getColumnIndex(MediaStore.Video.Media.SIZE)
                val widthCol = cursor.getColumnIndex(MediaStore.Video.Media.WIDTH)
                val heightCol = cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT)
                val dataCol = cursor.getColumnIndex(MediaStore.Video.Media.DATA)
                val relPathCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cursor.getColumnIndex(MediaStore.Video.Media.RELATIVE_PATH)
                } else -1

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: "VID_$id"
                    var dateTaken = cursor.getLong(dateTakenCol)
                    if (dateTaken <= 0L) {
                        dateTaken = cursor.getLong(dateModCol) * 1000L
                    }
                    val mime = cursor.getString(mimeCol) ?: "video/mp4"
                    val duration = cursor.getLong(durCol)
                    val size = if (sizeCol != -1) cursor.getLong(sizeCol) else 0L
                    val width = if (widthCol != -1) cursor.getInt(widthCol) else 0
                    val height = if (heightCol != -1) cursor.getInt(heightCol) else 0
                    val fullPath = if (dataCol != -1) cursor.getString(dataCol) ?: "" else ""
                    val relPath = if (relPathCol != -1) cursor.getString(relPathCol) ?: "" else fullPath

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    mediaList.add(
                        MediaItem(
                            id = id,
                            uri = contentUri,
                            displayName = name,
                            dateTaken = dateTaken,
                            mimeType = mime,
                            isVideo = true,
                            durationMs = duration,
                            relativePath = relPath,
                            fullPath = fullPath,
                            sizeBytes = size,
                            width = width,
                            height = height
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Sort all media by date taken descending
        mediaList.sortByDescending { it.dateTaken }

        // Group by relative day
        groupMediaByDate(mediaList)
    }

    private fun groupMediaByDate(mediaList: List<MediaItem>): List<MediaDateGroup> {
        if (mediaList.isEmpty()) return emptyList()

        val calendarNow = Calendar.getInstance()
        val todayYear = calendarNow.get(Calendar.YEAR)
        val todayDayOfYear = calendarNow.get(Calendar.DAY_OF_YEAR)

        val itemCal = Calendar.getInstance()
        val indonesianLocale = Locale("id", "ID")
        val dayMonthFormat = SimpleDateFormat("d MMM", indonesianLocale)
        val fullDateFormat = SimpleDateFormat("d MMM yyyy", indonesianLocale)

        val groupsMap = LinkedHashMap<String, MutableList<MediaItem>>()

        for (item in mediaList) {
            itemCal.timeInMillis = item.dateTaken
            val itemYear = itemCal.get(Calendar.YEAR)
            val itemDayOfYear = itemCal.get(Calendar.DAY_OF_YEAR)

            val headerTitle = when {
                itemYear == todayYear && itemDayOfYear == todayDayOfYear -> "Hari ini"
                itemYear == todayYear && itemDayOfYear == todayDayOfYear - 1 -> "Kemarin"
                itemYear == todayYear -> dayMonthFormat.format(Date(item.dateTaken))
                else -> fullDateFormat.format(Date(item.dateTaken))
            }

            groupsMap.getOrPut(headerTitle) { mutableListOf() }.add(item)
        }

        return groupsMap.map { (title, items) ->
            MediaDateGroup(
                title = title,
                locationSubtext = null,
                items = items
            )
        }
    }
}
