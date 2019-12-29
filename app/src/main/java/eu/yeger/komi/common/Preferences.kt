package eu.yeger.komi.common

import androidx.compose.ambient
import androidx.compose.unaryPlus
import androidx.core.content.edit
import androidx.ui.core.ContextAmbient

//
// Preference Manager
//

internal object Preferences {
    private val context = +ambient(ContextAmbient)
    private val preferences = context.applicationContext.getSharedPreferences("Komi", 0)

    fun retrieveBoolean(key: String, defaultValue: Boolean = false) =
        preferences.getBoolean(key, defaultValue)

    fun storeBoolean(key: String, value: Boolean) {
        preferences.edit {
            putBoolean(key, value)
        }
    }

    fun retrieveInt(key: String, defaultValue: Int = 0) =
        preferences.getInt(key, defaultValue)

    fun storeInt(key: String, value: Int) {
        preferences.edit {
            putInt(key, value)
        }
    }
}
