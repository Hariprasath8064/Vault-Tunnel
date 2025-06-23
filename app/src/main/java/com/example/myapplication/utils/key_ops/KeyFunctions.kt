package com.example.myapplication.utils.key_ops

import com.example.myapplication.utils.key_ops.key_ops_helpers.encryptData
import com.example.myapplication.utils.key_ops.key_ops_helpers.decryptData

object CryptoManager {

    /**
     * Encrypts the given plain text using a secret key stored in the Android Keystore.
     *
     * @param alias The alias of the secret key to be used for encryption.
     * @param plainText The plain text to be encrypted.
     * @return A pair containing the encrypted data (cipherText) and the initialization vector (iv),
     * or `null` if encryption fails.
     */

    fun encrypt(alias: String, plainText: String): Pair<ByteArray, ByteArray>? {
        return encryptData(alias, plainText)
    }

    /**
     * Decrypts the given cipher text using a secret key stored in the Android Keystore.
     *
     * @param alias The alias of the secret key to be used for decryption.
     * @param cipherText The encrypted data to be decrypted.
     * @param iv The initialization vector used during encryption.
     * @return The decrypted plain text, or `null` if decryption fails.
     */

    fun decrypt(alias: String, cipherText: ByteArray, iv: ByteArray): String? {
        return decryptData(alias, cipherText, iv)
    }
}
