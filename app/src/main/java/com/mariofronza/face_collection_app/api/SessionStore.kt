package com.mariofronza.face_collection_app.api

import android.content.Context
import com.google.gson.Gson

interface SessionStore {
    var current: Session?
}

class PreferencesSessionStore(context: Context) : SessionStore {
    private val preferences = context.getSharedPreferences("vision-session", Context.MODE_PRIVATE)
    private val gson = Gson()

    override var current: Session?
        get() = preferences.getString(KEY, null)?.let { gson.fromJson(it, Session::class.java) }
        set(value) {
            preferences.edit().apply {
                if (value == null) remove(KEY) else putString(KEY, gson.toJson(value))
            }.apply()
        }

    private companion object {
        const val KEY = "session"
    }
}
