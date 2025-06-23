package com.example.myapplication.utils.db_ops.db_initops_helpers

import com.example.myapplication.utils.constants.Constants.Database

internal fun buildInsensitiveTableCreateSql(tableName: String): String =
    """
    CREATE TABLE IF NOT EXISTS $tableName (
        ${Database.COLUMN_ID} TEXT PRIMARY KEY,
        ${Database.COLUMN_TYPE} TEXT NOT NULL,
        ${Database.COLUMN_VALUE} TEXT NOT NULL
    );
    """.trimIndent()

internal fun buildSensitiveTableCreateSql(): String =
    """
    CREATE TABLE IF NOT EXISTS ${Database.TABLE_SENSITIVE_DATA} (
        ${Database.COLUMN_ID} TEXT PRIMARY KEY,
        ${Database.COLUMN_CIPHER_TEXT} BLOB NOT NULL,
        ${Database.COLUMN_IV} BLOB NOT NULL
    );
    """.trimIndent()