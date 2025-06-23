package com.example.myapplication.utils.key_ops.key_ops_helpers

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.KeyPairGenerator
import java.security.KeyStore
import com.example.myapplication.utils.constants.Constants

internal fun generateRsaKeyPair(alias: String): Boolean {
    try {
        val keyStore = KeyStore.getInstance(Constants.KeyGen.ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(alias)) {
            Log.w(Constants.KeyGen.TAG, String.format(Constants.KeyGen.ERROR_KEY_PAIR_EXISTS, alias))
            return false
        }
        try {
            Log.i(Constants.KeyGen.TAG, Constants.KeyGen.STRONGBOX_KEY_PAIR_GEN)
            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA,
                Constants.KeyGen.ANDROID_KEYSTORE
            )

            val strongBoxSpec = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .setKeySize(Constants.KeyGen.KEY_SIZE_RSA)
                .setIsStrongBoxBacked(true)
                .build()

            keyPairGenerator.initialize(strongBoxSpec)
            keyPairGenerator.generateKeyPair()

            Log.i(Constants.KeyGen.TAG, String.format(Constants.KeyGen.STRONGBOX_KEY_PAIR_SUCCESS, alias))
            return true
        } catch (e: Exception) {
            Log.w(Constants.KeyGen.TAG, "${Constants.KeyGen.STRONGBOX_NOT_AVAILABLE}: ${e.message}")
            val fallbackKeyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA,
                Constants.KeyGen.ANDROID_KEYSTORE
            )

            val fallbackSpec = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .setKeySize(Constants.KeyGen.KEY_SIZE_RSA)
                .setIsStrongBoxBacked(false)
                .build()

            fallbackKeyPairGenerator.initialize(fallbackSpec)
            fallbackKeyPairGenerator.generateKeyPair()

            Log.i(Constants.KeyGen.TAG, String.format(Constants.KeyGen.TEE_KEY_PAIR_SUCCESS, alias))
            return true
        }
    } catch (e: Exception) {
        Log.e(Constants.KeyGen.TAG, "${Constants.KeyGen.ERROR_KEY_PAIR_GEN_FAILED}: ${e.message}", e)
        return false
    }
}