package com.example.myapplication.utils.key_ops.key_ops_helpers

import android.util.Log
import java.security.KeyStore
import javax.crypto.SecretKey
import com.example.myapplication.utils.constants.Constants

internal fun getSecretKey(alias: String): SecretKey? {
    return try {
        val keyStore = KeyStore.getInstance(Constants.KeyOps.ANDROID_KEYSTORE).apply { load(null) }
        keyStore.getKey(alias, null) as? SecretKey
    } catch (e: Exception) {
        Log.e(Constants.KeyOps.TAG, "${Constants.KeyOps.ERROR_KEY_RETRIEVE}: ${e.message}")
        null
    }
}