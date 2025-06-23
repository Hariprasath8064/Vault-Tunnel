package com.example.myapplication.utils.key_ops.key_ops_helpers

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.KeyStore
import javax.crypto.KeyGenerator
import com.example.myapplication.utils.constants.Constants

internal fun generateAesSecretKey(alias: String): Boolean {
    try {
        val keyStore = KeyStore.getInstance(Constants.KeyGen.ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(alias)) {
            Log.w(Constants.KeyGen.TAG, String.format(Constants.KeyGen.ERROR_KEY_EXISTS, alias))
            return false
        }
        try {
            Log.i(Constants.KeyGen.TAG, Constants.KeyGen.STRONGBOX_KEY_GEN)
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                Constants.KeyGen.ANDROID_KEYSTORE
            )

            val strongBoxSpec = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(Constants.KeyGen.KEY_SIZE_AES)
                .setIsStrongBoxBacked(true)
                .build()

            keyGenerator.init(strongBoxSpec)
            keyGenerator.generateKey()

            Log.i(Constants.KeyGen.TAG, String.format(Constants.KeyGen.STRONGBOX_KEY_SUCCESS, alias))
            return true
        } catch (e: Exception) {
            Log.w(Constants.KeyGen.TAG, "${Constants.KeyGen.STRONGBOX_NOT_AVAILABLE}: ${e.message}")
            val fallbackKeyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                Constants.KeyGen.ANDROID_KEYSTORE
            )

            val fallbackSpec = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(Constants.KeyGen.KEY_SIZE_AES)
                .setIsStrongBoxBacked(false)
                .build()

            fallbackKeyGenerator.init(fallbackSpec)
            fallbackKeyGenerator.generateKey()

            Log.i(Constants.KeyGen.TAG, String.format(Constants.KeyGen.TEE_KEY_SUCCESS, alias))
            return true
        }
    } catch (e: Exception) {
        Log.e(Constants.KeyGen.TAG, "${Constants.KeyGen.ERROR_KEY_GEN_FAILED}: ${e.message}", e)
        return false
    }
}