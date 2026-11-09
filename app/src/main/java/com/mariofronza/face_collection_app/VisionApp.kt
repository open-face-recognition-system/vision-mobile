package com.mariofronza.face_collection_app

import android.app.Application
import android.content.Context
import com.mariofronza.face_collection_app.api.ApiService
import com.mariofronza.face_collection_app.api.PreferencesSessionStore

class VisionApp : Application() {
    val session by lazy { PreferencesSessionStore(this) }
    val api by lazy { ApiService.create(BuildConfig.API_URL, session) }

    private val preferences by lazy { getSharedPreferences("vision", MODE_PRIVATE) }

    var consented: Boolean
        get() = preferences.getBoolean("consented", false)
        set(value) = preferences.edit().putBoolean("consented", value).apply()
}

val Context.vision: VisionApp
    get() = applicationContext as VisionApp
