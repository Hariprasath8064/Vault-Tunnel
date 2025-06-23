package com.example.myapplication.utils.key_ops.key_ops_helpers

import android.util.Log
import javax.crypto.Cipher
import com.example.myapplication.utils.constants.Constants

internal fun encryptData(alias: String, plainText: String): Pair<ByteArray, ByteArray>? {
    return try {
        val secretKey = getSecretKey(alias) ?: return null
        val cipher = Cipher.getInstance(Constants.KeyOps.TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        Pair(cipherText, iv)
    } catch (e: Exception) {
        Log.e(Constants.KeyOps.TAG, "${Constants.KeyOps.ERROR_ENCRYPTION}: ${e.message}")
        null
    }
}