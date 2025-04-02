package uz.dckroff.pcap.features.settings.data

import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import uz.dckroff.pcap.features.settings.domain.SettingsRepository
import uz.dckroff.pcap.features.settings.domain.model.Settings
import uz.dckroff.pcap.features.settings.domain.model.TextSize
import uz.dckroff.pcap.features.settings.domain.model.ThemeMode
import javax.inject.Inject

/**
 * Реализация репозитория настроек, использующая SharedPreferences
 */
class SettingsRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : SettingsRepository {

    private val _settingsFlow = MutableStateFlow(loadSettings())
    
    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_TEXT_SIZE = "text_size"
        private const val KEY_AUTO_SAVE = "auto_save"
        private const val KEY_NOTIFICATIONS = "notifications"
    }
    
    override fun getSettingsFlow(): Flow<Settings> = _settingsFlow.asStateFlow()
    
    override fun getCurrentSettings(): Settings = _settingsFlow.value
    
    override suspend fun setThemeMode(themeMode: ThemeMode) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit()
                .putString(KEY_THEME_MODE, themeMode.name)
                .apply()
                
            val current = _settingsFlow.value
            _settingsFlow.value = current.copy(themeMode = themeMode)
        }
    }
    
    override suspend fun setTextSize(textSize: TextSize) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit()
                .putString(KEY_TEXT_SIZE, textSize.name)
                .apply()
                
            val current = _settingsFlow.value
            _settingsFlow.value = current.copy(textSize = textSize)
        }
    }
    
    override suspend fun setAutoSaveEnabled(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit()
                .putBoolean(KEY_AUTO_SAVE, enabled)
                .apply()
                
            val current = _settingsFlow.value
            _settingsFlow.value = current.copy(autoSaveEnabled = enabled)
        }
    }
    
    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit()
                .putBoolean(KEY_NOTIFICATIONS, enabled)
                .apply()
                
            val current = _settingsFlow.value
            _settingsFlow.value = current.copy(notificationsEnabled = enabled)
        }
    }
    
    /**
     * Загружает настройки из SharedPreferences
     */
    private fun loadSettings(): Settings {
        val themeModeStr = sharedPreferences.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        val themeMode = themeModeStr?.let { ThemeMode.valueOf(it) } ?: ThemeMode.SYSTEM
        
        val textSizeStr = sharedPreferences.getString(KEY_TEXT_SIZE, TextSize.MEDIUM.name)
        val textSize = textSizeStr?.let { TextSize.valueOf(it) } ?: TextSize.MEDIUM
        
        val autoSaveEnabled = sharedPreferences.getBoolean(KEY_AUTO_SAVE, true)
        val notificationsEnabled = sharedPreferences.getBoolean(KEY_NOTIFICATIONS, true)
        
        return Settings(
            themeMode = themeMode,
            textSize = textSize,
            autoSaveEnabled = autoSaveEnabled,
            notificationsEnabled = notificationsEnabled
        )
    }
} 