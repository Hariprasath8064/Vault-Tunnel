package com.example.myapplication.utils.key_ops.key_ops_helpers

import android.util.Log
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import com.example.myapplication.utils.constants.Constants

internal fun decryptData(alias: String, cipherText: ByteArray, iv: ByteArray): String? {
    return try {
        val secretKey = getSecretKey(alias) ?: return null
        val cipher = Cipher.getInstance(Constants.KeyOps.TRANSFORMATION)
        val spec = GCMParameterSpec(Constants.KeyOps.GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        val plainBytes = cipher.doFinal(cipherText)
        String(plainBytes, Charsets.UTF_8)
    } catch (e: Exception) {
        Log.e(Constants.KeyOps.TAG, "${Constants.KeyOps.ERROR_DECRYPTION}: ${e.message}")
        null
    }
}