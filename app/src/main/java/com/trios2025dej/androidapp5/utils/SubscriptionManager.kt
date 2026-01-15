package com.trios2025dej.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.trios2025dej.models.Podcast

object SubscriptionManager {
    private const val PREFS_NAME = "subscriptions"
    private val gson = Gson()

    fun subscribe(context: Context, podcast: Podcast) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val all = getAllSubscribed(context).toMutableList()
        if (all.none { it.id == podcast.id }) {
            all.add(podcast)
            prefs.edit().putString(PREFS_NAME, gson.toJson(all)).apply()
        }
    }

    fun unsubscribe(context: Context, podcastId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val all = getAllSubscribed(context).toMutableList()
        all.removeAll { it.id == podcastId }
        prefs.edit().putString(PREFS_NAME, gson.toJson(all)).apply()
    }

    fun isSubscribed(context: Context, podcastId: String) =
        getAllSubscribed(context).any { it.id == podcastId }

    fun getAllSubscribed(context: Context): List<Podcast> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(PREFS_NAME, null) ?: return emptyList()
        val type = object : TypeToken<List<Podcast>>() {}.type
        return gson.fromJson(json, type)
    }
}
