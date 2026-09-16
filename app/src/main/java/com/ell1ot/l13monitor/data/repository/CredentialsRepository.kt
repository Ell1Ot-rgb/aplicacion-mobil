package com.ell1ot.l13monitor.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import javax.inject.Named

/** Secrets and config, stored encrypted at rest. Reads are synchronous for OkHttp interceptors. */
@Singleton
class CredentialsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("L13_BASE_URL_DEFAULT") private val defaultBaseUrl: String,
) {
    private val prefs by lazy {
        try {
            EncryptedSharedPreferences.create(
                context,
                FILE_NAME,
                MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        } catch (e: Exception) {
            android.util.Log.w("CredentialsRepo", "EncryptedSharedPreferences unavailable, using standard prefs fallback", e)
            context.getSharedPreferences(FILE_NAME + "_fallback", Context.MODE_PRIVATE)
        }
    }

    val baseUrl: String
        get() = prefs.getString(KEY_BASE_URL, null)?.takeIf { it.isNotBlank() } ?: defaultBaseUrl

    val bearerToken: String?
        get() = prefs.getString(KEY_BEARER, null)?.takeIf { it.isNotBlank() }

    fun save(baseUrl: String, bearerToken: String) {
        prefs.edit()
            .putString(KEY_BASE_URL, baseUrl.trim())
            .putString(KEY_BEARER, bearerToken.trim())
            .apply()
    }

    fun clear() = prefs.edit().clear().apply()

    private companion object {
        const val FILE_NAME = "l13_credentials"
        const val KEY_BASE_URL = "base_url"
        const val KEY_BEARER = "bearer_token"
    }
}
