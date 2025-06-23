package com.example.myapplication.utils.db_ops.db_initops_helpers

private val TABLE_NAME_REGEX = Regex("^[a-zA-Z0-9_]+$")

internal fun isValidTableName(tableName: String): Boolean =
    TABLE_NAME_REGEX.matches(tableName)