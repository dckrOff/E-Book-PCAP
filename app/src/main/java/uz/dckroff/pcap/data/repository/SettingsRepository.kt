package uz.dckroff.pcap.data.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

enum class TextSize {
    SMALL, MEDIUM, LARGE, EXTRA_LARGE
}

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFERENCES_NAME, Context.MODE_PRIVATE
    )

    // Тема
    fun getThemeMode(): ThemeMode {
        val themeModeString = sharedPreferences.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(themeModeString ?: ThemeMode.SYSTEM.name)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        sharedPreferences.edit().putString(KEY_THEME_MODE, themeMode.name).apply()
    }

    // Размер текста
    fun getTextSize(): TextSize {
        val textSizeString = sharedPreferences.getString(KEY_TEXT_SIZE, TextSize.MEDIUM.name)
        return try {
            TextSize.valueOf(textSizeString ?: TextSize.MEDIUM.name)
        } catch (e: IllegalArgumentException) {
            TextSize.MEDIUM
        }
    }

    suspend fun setTextSize(textSize: TextSize) {
        sharedPreferences.edit().putString(KEY_TEXT_SIZE, textSize.name).apply()
    }

    // Автосохранение
    fun isAutoSaveEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_AUTO_SAVE, true)
    }

    suspend fun setAutoSave(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_AUTO_SAVE, enabled).apply()
    }

    // Уведомления
    fun areNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS, true)
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply()
    }

    // Расширенные настройки для текстового контента
    fun getLineSpacing(): Float {
        return sharedPreferences.getFloat(KEY_LINE_SPACING, 1.0f)
    }

    suspend fun setLineSpacing(spacing: Float) {
        sharedPreferences.edit().putFloat(KEY_LINE_SPACING, spacing).apply()
    }

    companion object {
        private const val PREFERENCES_NAME = "uz.dckroff.pcap.settings"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_TEXT_SIZE = "text_size"
        private const val KEY_AUTO_SAVE = "auto_save"
        private const val KEY_NOTIFICATIONS = "notifications"
        private const val KEY_LINE_SPACING = "line_spacing"
    }
} 