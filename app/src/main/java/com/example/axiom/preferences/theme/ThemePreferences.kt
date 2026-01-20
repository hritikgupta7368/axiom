package com.example.axiom.preferences.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.axiom.DataStore.appDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider

enum class ThemeMode {
    LIGHT, DARK
}


private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")

class ThemePreferences(
    private val context: Context
) {

    val themeMode: Flow<ThemeMode> =
        context.appDataStore.data.map {
            ThemeMode.valueOf(
                it[THEME_MODE_KEY] ?: ThemeMode.DARK.name
            )
        }

    suspend fun setTheme(mode: ThemeMode) {
        context.appDataStore.edit {
            it[THEME_MODE_KEY] = mode.name
        }
    }
}

class ThemeViewModel(
    private val prefs: ThemePreferences
) : ViewModel() {

    val themeMode = prefs.themeMode

    fun toggleTheme() {
        viewModelScope.launch {
            val current = themeMode.first()
            prefs.setTheme(
                if (current == ThemeMode.DARK)
                    ThemeMode.LIGHT
                else
                    ThemeMode.DARK
            )
        }
    }
}

class ThemeViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
            val prefs = ThemePreferences(context.applicationContext)
            return ThemeViewModel(prefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
