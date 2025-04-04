package com.bda.projectpulse.utils

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyGenParameterSpec(
            KeyGenParameterSpec.Builder(
                "_androidx_security_master_key_",
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        )
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveString(key: String, value: String): Boolean {
        return try {
            sharedPreferences.edit().putString(key, value).commit()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getString(key: String): String? {
        return try {
            sharedPreferences.getString(key, null)
        } catch (e: Exception) {
            null
        }
    }

    fun removeString(key: String): Boolean {
        return try {
            sharedPreferences.edit().remove(key).commit()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun saveApiKey(apiKey: String): Boolean {
        return saveString("openrouter_api_key", apiKey)
    }

    fun getApiKey(): String? {
        return getString("openrouter_api_key")
    }
} 