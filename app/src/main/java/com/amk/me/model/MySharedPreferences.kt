package com.amk.me.model

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class MySharedPreferences {


    fun getEncryptedSharedPreferences(
        context: Context,
        sharedPrefsFile: String
    ): SharedPreferences? {
        val masterKeyAlias1 = "_androidx_security_master_key_"
        val KEY_SIZE = 256
        return try {
            val spec = KeyGenParameterSpec.Builder(
                masterKeyAlias1,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_SIZE)
                .build()
            val masterKey: MasterKey = MasterKey.Builder(context)
                .setKeyGenParameterSpec(spec)
                .build()
            EncryptedSharedPreferences.create(
                context,
                sharedPrefsFile,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            null
        }
    }
}