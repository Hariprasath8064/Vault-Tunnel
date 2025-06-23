package com.example.myapplication.utils.db_ops

import android.content.Context
import com.example.basehttpapp.utils.db_ops.storeops_helpers.storeSensitiveDataInternal
import com.example.basehttpapp.utils.db_ops.storeops_helpers.retrieveSensitiveDataInternal
import com.example.basehttpapp.utils.db_ops.storeops_helpers.storeInsensitiveDataInternal
import com.example.basehttpapp.utils.db_ops.storeops_helpers.retrieveInsensitiveDataInternal

object SecureDataStore {

    /**
     * Stores sensitive data securely in the database.
     *
     * @param context The [Context] used to access the database.
     * @param id The unique identifier for the sensitive data.
     * @param plainText The plain text data to be encrypted and stored.
     * @return `true` if the data is successfully stored, `false` otherwise.
     */
    fun storeSensitiveData(context: Context, id: String, plainText: String): Boolean =
        storeSensitiveDataInternal(context, id, plainText)

    /**
     * Retrieves sensitive data securely from the database.
     *
     * @param context The [Context] used to access the database.
     * @param id The unique identifier for the sensitive data.
     * @return The decrypted plain text data if found, or `null` if not found.
     */
    fun retrieveSensitiveData(context: Context, id: String): String? =
        retrieveSensitiveDataInternal(context, id)

    /**
     * Stores insensitive data in the specified table in the database.
     *
     * @param context The [Context] used to access the database.
     * @param tableName The name of the table where the data will be stored.
     * @param id The unique identifier for the data.
     * @param type The type of the data being stored.
     * @param value The value of the data being stored.
     * @return `true` if the data is successfully stored, `false` otherwise.
     */
    fun storeInsensitiveData(context: Context, tableName: String, id: String, type: String, value: String): Boolean =
        storeInsensitiveDataInternal(context, tableName, id, type, value)

    /**
     * Retrieves insensitive data from the specified table in the database.
     *
     * @param context The [Context] used to access the database.
     * @param tableName The name of the table where the data is stored.
     * @param id The unique identifier for the data.
     * @return The value of the data if found, or `null` if not found.
     */
    fun retrieveInsensitiveData(context: Context, tableName: String, id: String): String? =
        retrieveInsensitiveDataInternal(context, tableName, id)
}
