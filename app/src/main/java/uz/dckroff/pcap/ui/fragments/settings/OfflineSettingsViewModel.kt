package uz.dckroff.pcap.ui.fragments.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import uz.dckroff.pcap.data.preferences.UserPreferences
import uz.dckroff.pcap.utils.OfflineManager
import uz.dckroff.pcap.utils.Utils
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

// Модель данных для настроек оффлайн-режима
data class OfflineSettings(
    val autoSync: Boolean = true,
    val downloadAllContent: Boolean = false
)

// Состояния процесса синхронизации
sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    object Success : SyncState()
    data class Error(val error: Throwable) : SyncState()
}

@HiltViewModel
class OfflineSettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val offlineManager: OfflineManager
) : ViewModel() {

    private val _settings = MutableStateFlow(OfflineSettings())
    val settings: StateFlow<OfflineSettings> = _settings.asStateFlow()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _cacheSize = MutableStateFlow("0 MB")
    val cacheSize: StateFlow<String> = _cacheSize.asStateFlow()

    private val _lastSyncTime = MutableStateFlow("")
    val lastSyncTime: StateFlow<String> = _lastSyncTime.asStateFlow()

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    init {
        loadSettings()
        calculateCacheSize()
        loadLastSyncTime()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val autoSync = userPreferences.getAutoSyncEnabled()
                val downloadAllContent = userPreferences.getDownloadAllContent()
                _settings.value = OfflineSettings(autoSync, downloadAllContent)
            } catch (e: Exception) {
                Timber.e(e, "Failed to load offline settings")
            }
        }
    }

    private fun loadLastSyncTime() {
        viewModelScope.launch {
            try {
                val lastSyncTimeMillis = userPreferences.getLastSyncTime()
                _lastSyncTime.value = if (lastSyncTimeMillis > 0) {
                    dateFormat.format(Date(lastSyncTimeMillis))
                } else {
                    ""
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load last sync time")
            }
        }
    }

    fun setAutoSync(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferences.setAutoSyncEnabled(enabled)
                _settings.value = _settings.value.copy(autoSync = enabled)
            } catch (e: Exception) {
                Timber.e(e, "Failed to update auto sync setting")
            }
        }
    }

    fun setDownloadAllContent(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferences.setDownloadAllContent(enabled)
                _settings.value = _settings.value.copy(downloadAllContent = enabled)
                
                if (enabled) {
                    syncData() // Запускаем синхронизацию, если включено скачивание всего контента
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to update download all content setting")
            }
        }
    }

    fun syncData() {
        viewModelScope.launch {
            _syncState.value = SyncState.Syncing
            try {
                offlineManager.startSync()
                _syncState.value = SyncState.Success
            } catch (e: Exception) {
                Timber.e(e, "Sync failed")
                _syncState.value = SyncState.Error(e)
            }
        }
    }

    fun updateLastSyncTime() {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            userPreferences.setLastSyncTime(currentTime)
            _lastSyncTime.value = dateFormat.format(Date(currentTime))
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            try {
                // Очистка кэша приложения
                offlineManager.clearCache()
                
                // Обновляем размер кэша после очистки
                calculateCacheSize()
                
                // Сбрасываем время последней синхронизации
                userPreferences.setLastSyncTime(0)
                _lastSyncTime.value = ""
            } catch (e: Exception) {
                Timber.e(e, "Failed to clear cache")
            }
        }
    }

    private fun calculateCacheSize() {
        viewModelScope.launch {
            try {
                val cacheDir = File(offlineManager.getCacheDirectory())
                val size = Utils.getDirectorySize(cacheDir)
                _cacheSize.value = Utils.formatFileSize(size)
            } catch (e: Exception) {
                Timber.e(e, "Failed to calculate cache size")
                _cacheSize.value = "0 MB"
            }
        }
    }
} 