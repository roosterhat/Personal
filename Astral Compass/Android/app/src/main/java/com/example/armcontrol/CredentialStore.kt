package com.example.armcontrol.ephemeris

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

data class Credentials (
    val identity: String,
    val password: String
) {
    override fun toString() = "Credentials(identity=$identity, password=***)"
}

class CredentialsStore(private val context: Context) {
    private val prefs: SharedPreferences by lazy { open() }

    fun load(): Credentials? {
        val identity = prefs.getString(KEY_IDENTITY, null)?.takeIf { it.isNotBlank() }
        val password = prefs.getString(KEY_PASSWORD, null)?.takeIf { it.isNotEmpty() }
        return if (identity != null && password != null) Credentials(identity, password) else null
    }

    fun save(credentials: Credentials) {
        prefs.edit()
            .putString(KEY_IDENTITY, credentials.identity.trim())
            .putString(KEY_PASSWORD, credentials.password)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private fun open(): SharedPreferences = try {
        create()
    } catch (e: Exception) {
        context.deleteSharedPreferences(FILE_NAME)
        create()
    }

    private fun create(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private companion object {
        const val FILE_NAME = "spacetrack_credentials"
        const val KEY_IDENTITY = "identity"
        const val KEY_PASSWORD = "password"
    }
}
