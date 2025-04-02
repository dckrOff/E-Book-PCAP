package uz.dckroff.pcap.features.settings.domain

import kotlinx.coroutines.flow.Flow
import uz.dckroff.pcap.features.settings.domain.model.Settings
import uz.dckroff.pcap.features.settings.domain.model.TextSize
import uz.dckroff.pcap.features.settings.domain.model.ThemeMode

/**
 * Интерфейс репозитория для работы с настройками приложения
 */
interface SettingsRepository {
    /**
     * Получить текущие настройки в виде потока
     */
    fun getSettingsFlow(): Flow<Settings>
    
    /**
     * Получить текущие настройки
     */
    fun getCurrentSettings(): Settings
    
    /**
     * Установить режим темы
     */
    suspend fun setThemeMode(themeMode: ThemeMode)
    
    /**
     * Установить размер текста
     */
    suspend fun setTextSize(textSize: TextSize)
    
    /**
     * Установить статус автоматического сохранения
     */
    suspend fun setAutoSaveEnabled(enabled: Boolean)
    
    /**
     * Установить статус уведомлений
     */
    suspend fun setNotificationsEnabled(enabled: Boolean)
} 