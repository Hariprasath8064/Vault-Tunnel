package com.example.basehttpapp.utils.db_ops.storeops_helpers

import android.content.Context
import android.util.Log
import com.example.myapplication.utils.constants.Constants.Database
import com.example.myapplication.utils.db_ops.DatabaseInitializer.getWritableDatabase

internal fun retrieveInsensitiveDataInternal(context: Context, tableName: String, id: String): String? {
    val db = getWritableDatabase(context)
    val cursor = db.rawQuery(
        "SELECT ${Database.COLUMN_VALUE} FROM $tableName WHERE ${Database.COLUMN_ID} = ?",
        arrayOf(id)
    )

    cursor.use {
        if (it.moveToFirst()) {
            return it.getString(0)
        }
    }

    Log.w(Database.TAG_SECURE_DATA_STORE, "No insensitive data found in $tableName for ID: $id")
    return null
}