package com.example.basehttpapp.utils.db_ops.storeops_helpers

import android.content.Context
import android.util.Log
import com.example.myapplication.utils.constants.Constants.Database
import com.example.myapplication.utils.db_ops.DatabaseInitializer.getWritableDatabase
import com.example.myapplication.utils.key_ops.KeyManager.getStoredAlias
import com.example.myapplication.utils.key_ops.CryptoManager.encrypt

internal fun storeSensitiveDataInternal(context: Context, id: String, plainText: String): Boolean {
    val alias = getStoredAlias()
    val (cipherText, iv) = encrypt(alias, plainText) ?: return false

    val db = getWritableDatabase(context)
    val stmt = db.compileStatement("""
        INSERT OR REPLACE INTO ${Database.TABLE_SENSITIVE_DATA} (${Database.COLUMN_ID}, ${Database.COLUMN_CIPHER_TEXT}, ${Database.COLUMN_IV}) VALUES (?, ?, ?);
    """.trimIndent())

    stmt.bindString(1, id)
    stmt.bindBlob(2, cipherText)
    stmt.bindBlob(3, iv)

    return try {
        stmt.executeInsert()
        Log.i(Database.TAG_SECURE_DATA_STORE, "Sensitive data stored for ID: $id")
        true
    } catch (e: Exception) {
        Log.e(Database.TAG_SECURE_DATA_STORE, "Failed to store sensitive data: ${e.message}", e)
        false
    }
}