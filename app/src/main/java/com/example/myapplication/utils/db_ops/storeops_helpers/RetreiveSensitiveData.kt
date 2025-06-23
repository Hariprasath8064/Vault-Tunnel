package com.example.basehttpapp.utils.db_ops.storeops_helpers

import android.content.Context
import android.util.Log
import com.example.myapplication.utils.constants.Constants.Database
import com.example.myapplication.utils.db_ops.DatabaseInitializer.getWritableDatabase
import com.example.myapplication.utils.key_ops.KeyManager.getStoredAlias
import com.example.myapplication.utils.key_ops.CryptoManager.decrypt

internal fun retrieveSensitiveDataInternal(context: Context, id: String): String? {
    val alias = getStoredAlias()
    val db = getWritableDatabase(context)
    val cursor = db.rawQuery(
        "SELECT ${Database.COLUMN_CIPHER_TEXT}, ${Database.COLUMN_IV} FROM ${Database.TABLE_SENSITIVE_DATA} WHERE ${Database.COLUMN_ID} = ?",
        arrayOf(id)
    )

    cursor.use {
        if (it.moveToFirst()) {
            val cipherText = it.getBlob(0)
            val iv = it.getBlob(1)
            return decrypt(alias, cipherText, iv)
        }
    }

    Log.w(Database.TAG_SECURE_DATA_STORE, "No sensitive data found for ID: $id")
    return null
}