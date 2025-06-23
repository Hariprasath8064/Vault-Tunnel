package com.example.myapplication.utils.key_ops

import com.example.myapplication.utils.key_ops.key_ops_helpers.generateAesSecretKey
import com.example.myapplication.utils.key_ops.key_ops_helpers.generateRsaKeyPair
object KeyGenerator {
    /**
     * Generates a secret key using the Android Keystore system.
     * Attempts to use StrongBox if available, otherwise falls back to TEE (Trusted Execution Environment).
     *
     * @param alias The alias for the secret key to be generated.
     * @return `true` if the key is successfully generated, `false` if the key already exists or an error occurs.
     */

    fun generateSecretKey(alias: String): Boolean {
        return generateAesSecretKey(alias)
    }
    /**
     * Generates a key pair (RSA) using the Android Keystore system.
     * Attempts to use StrongBox if available, otherwise falls back to TEE (Trusted Execution Environment).
     *
     * @param alias The alias for the key pair to be generated.
     * @return `true` if the key pair is successfully generated, `false` if the key pair already exists or an error occurs.
     */

    fun generateKeyPair(alias: String): Boolean {
        return generateRsaKeyPair(alias)
    }
}
