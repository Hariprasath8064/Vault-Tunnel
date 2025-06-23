package com.example.basehttpapp.utils.db_ops.storeops_helpers

import android.content.Context
import android.util.Log
import com.example.myapplication.utils.constants.Constants.Database
import com.example.myapplication.utils.db_ops.DatabaseInitializer.getWritableDatabase

internal fun storeInsensitiveDataInternal(context: Context, tableName: String, id: String, type: String, value: String): Boolean {
    val db = getWritableDatabase(context)
    val stmt = db.compileStatement("""
        INSERT OR REPLACE INTO $tableName (${Database.COLUMN_ID}, ${Database.COLUMN_TYPE}, ${Database.COLUMN_VALUE}) VALUES (?, ?, ?);
    """.trimIndent())

    stmt.bindString(1, id)
    stmt.bindString(2, type)
    stmt.bindString(3, value)

    return try {
        stmt.executeInsert()
        Log.i(Database.TAG_SECURE_DATA_STORE, "Insensitive data stored in $tableName for ID: $id")
        true
    } catch (e: Exception) {
        Log.e(Database.TAG_SECURE_DATA_STORE, "Failed to store insensitive data: ${e.message}", e)
        false
    }
}