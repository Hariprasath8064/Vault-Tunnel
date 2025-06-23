package com.example.myapplication.utils.key_ops

import android.util.Log
import com.example.myapplication.utils.constants.Constants

object KeyManager {
    /**
     * Initializes a secret key in the Android Keystore system.
     * Attempts to generate a StrongBox-backed key if available, otherwise falls back to TEE.
     *
     * @return `true` if the key is successfully initialized, `false` otherwise.
     */
    fun initializeKey(): Boolean {
        val success = KeyGenerator.generateSecretKey(Constants.KeyOps.ALIAS)
        if (success) {
            Log.i(Constants.KeyOps.KM_TAG, "Alias successfully initialized.")
        }
        return success
    }

    /**
     * Retrieves the alias of the secret key stored in the Android Keystore system.
     *
     * @return The alias of the secret key.
     */

    fun getStoredAlias(): String {
        return Constants.KeyOps.ALIAS
    }
}
