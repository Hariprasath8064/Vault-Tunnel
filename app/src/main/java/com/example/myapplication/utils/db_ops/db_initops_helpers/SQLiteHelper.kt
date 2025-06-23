package com.example.myapplication.utils.db_ops.db_initops_helpers

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.myapplication.utils.constants.Constants.Database

internal class SQLiteHelper(
    context: Context,
    private val onCreateSensitiveTable: (SQLiteDatabase) -> Unit
) : SQLiteOpenHelper(context, Database.DATABASE_NAME, null, Database.DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        onCreateSensitiveTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database upgrade logic here if needed
    }
}