package uz.dckroff.pcap.utils

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import uz.dckroff.pcap.data.repository.ThemeMode

/**
 * Утилита для управления темой приложения
 */
object ThemeUtils {

    /**
     * Применяет режим темы к приложению
     */
    fun applyTheme(themeMode: ThemeMode) {
        val nightMode = when (themeMode) {
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            ThemeMode.SYSTEM -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                } else {
                    AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                }
            }
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
} 