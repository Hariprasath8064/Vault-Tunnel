package com.example.myapplication.utils.db_ops

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.myapplication.utils.constants.Constants.Database
import com.example.myapplication.utils.db_ops.db_initops_helpers.buildSensitiveTableCreateSql
import com.example.myapplication.utils.db_ops.db_initops_helpers.buildInsensitiveTableCreateSql
import com.example.myapplication.utils.db_ops.db_initops_helpers.isValidTableName
import com.example.myapplication.utils.db_ops.db_initops_helpers.SQLiteHelper

object DatabaseInitializer {

    private const val TAG = Database.TAG
    private lateinit var dbHelper: SQLiteHelper

    /**
     * Initializes the database by creating tables for the provided insensitive table names.
     *
     * @param context The [Context] used to access the database.
     * @param insensitiveTableNames A list of table names to be created in the database.
     * Only valid table names (alphanumeric with underscores) will be processed.
     */
    fun initialize(context: Context, insensitiveTableNames: List<String>) {
        dbHelper = SQLiteHelper(context) { db ->
            db.execSQL(buildSensitiveTableCreateSql())
            Log.i(TAG, "${Database.TABLE_SENSITIVE_DATA} table created or already exists.")
        }
        val db = dbHelper.writableDatabase
        for (tableName in insensitiveTableNames) {
            if (!isValidTableName(tableName)) {
                Log.w(TAG, "Invalid table name: $tableName — skipping.")
                continue
            }
            db.execSQL(buildInsensitiveTableCreateSql(tableName))
            Log.i(TAG, "Insensitive table '$tableName' created or already exists.")
        }
    }

    /**
     * Retrieves a writable instance of the database.
     *
     * @param context The [Context] used to access the database.
     * @return A writable [SQLiteDatabase] instance.
     */
    fun getWritableDatabase(context: Context): SQLiteDatabase {
        if (!DatabaseInitializer::dbHelper.isInitialized) {
            dbHelper = SQLiteHelper(context) { db ->
                db.execSQL(buildSensitiveTableCreateSql())
                Log.i(TAG, "${Database.TABLE_SENSITIVE_DATA} table created or already exists.")
            }
        }
        return dbHelper.writableDatabase
    }

}