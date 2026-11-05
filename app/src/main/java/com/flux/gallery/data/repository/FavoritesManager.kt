package com.flux.gallery.data.repository

import android.content.Context
import android.content.SharedPreferences

class FavoritesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getFavoriteIds(): Set<Long> {
        val stringSet = prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
        return stringSet.mapNotNull { it.toLongOrNull() }.toSet()
    }

    fun isFavorite(id: Long): Boolean {
        return getFavoriteIds().contains(id)
    }

    fun toggleFavorite(id: Long): Boolean {
        val current = getFavoriteIds().toMutableSet()
        val willBeFav = if (current.contains(id)) {
            current.remove(id)
            false
        } else {
            current.add(id)
            true
        }
        prefs.edit().putStringSet(KEY_FAVORITES, current.map { it.toString() }.toSet()).apply()
        return willBeFav
    }

    fun toggleFavorites(ids: Set<Long>): Boolean {
        if (ids.isEmpty()) return false
        val current = getFavoriteIds().toMutableSet()
        val allAreFav = ids.all { current.contains(it) }
        val resultIsAdded = if (allAreFav) {
            current.removeAll(ids)
            false
        } else {
            current.addAll(ids)
            true
        }
        prefs.edit().putStringSet(KEY_FAVORITES, current.map { it.toString() }.toSet()).apply()
        return resultIsAdded
    }

    companion object {
        private const val PREFS_NAME = "gallery_prefs"
        private const val KEY_FAVORITES = "favorite_media_ids"
    }
}
