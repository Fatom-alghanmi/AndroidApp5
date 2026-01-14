package com.trios2025dej.utils

import android.content.Context

object SubscriptionManager {
    private const val PREFS_NAME = "podcast_subscriptions"
    private const val KEY_SUBSCRIBED_IDS = "subscribed_ids"

    fun isSubscribed(context: Context, podcastId: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(KEY_SUBSCRIBED_IDS, emptySet()) ?: emptySet()
        return set.contains(podcastId)
    }

    fun subscribe(context: Context, podcastId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(KEY_SUBSCRIBED_IDS, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        set.add(podcastId)
        prefs.edit().putStringSet(KEY_SUBSCRIBED_IDS, set).apply()
    }

    fun unsubscribe(context: Context, podcastId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(KEY_SUBSCRIBED_IDS, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        set.remove(podcastId)
        prefs.edit().putStringSet(KEY_SUBSCRIBED_IDS, set).apply()
    }
}
