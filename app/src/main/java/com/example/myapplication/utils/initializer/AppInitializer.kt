package com.example.myapplication.utils.initializer

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.example.myapplication.utils.db_ops.DatabaseInitializer
import com.example.myapplication.utils.key_ops.KeyManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.myapplication.utils.constants.Constants


object AppInitializer {

    /**
     * Performs one-time application initialization.
     *
     * Checks if the app has already been initialized using shared preferences.
     * If not, initializes the key manager and marks initialization as complete.
     * Always initializes the database with the provided insensitive tables.
     *
     * @param context The application context.
     * @param insensitiveTables List of insensitive database table names to initialize.
     */
    fun initializeOnce(context: Context, insensitiveTables: List<String>) {
        val prefs = context.getSharedPreferences(Constants.Appinit.PREF_NAME, Context.MODE_PRIVATE)
        DatabaseInitializer.initialize(context, insensitiveTables)

        if (!prefs.getBoolean(Constants.Appinit.INIT_KEY, false)) {
            CoroutineScope(Dispatchers.IO).launch {
                val success = KeyManager.initializeKey()
                if (success) {
                    Log.i("AppInitializer", "Initialization successful.")
                    prefs.edit { putBoolean(Constants.Appinit.INIT_KEY, true) }
                } else {
                    Log.e("AppInitializer", "Initialization failed.")
                }
            }
        } else {
            Log.i("AppInitializer", "App already initialized — skipping.")
        }

    }
}
