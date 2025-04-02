package uz.dckroff.pcap.utils

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import uz.dckroff.pcap.R
import uz.dckroff.pcap.data.sync.SyncManager
import uz.dckroff.pcap.data.sync.SyncState
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Менеджер для управления оффлайн-функциональностью
 */
@Singleton
class OfflineManager @Inject constructor(
    private val context: Context,
    private val networkUtils: NetworkUtils,
    private val syncManager: SyncManager
) {
    private var isInitialized = false
    
    /**
     * Инициализирует менеджер оффлайн-режима
     */
    fun initialize(applicationScope: CoroutineScope) {
        if (isInitialized) return
        
        // Инициализируем менеджер синхронизации
        syncManager.initialize()
        
        // Следим за состоянием сети
        applicationScope.launch(Dispatchers.Main) {
            networkUtils.observeNetworkState().collectLatest { isConnected ->
                if (isConnected) {
                    // Показываем уведомление о восстановлении соединения
                    Toast.makeText(
                        context,
                        R.string.connection_restored,
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Запускаем синхронизацию данных
                    startSync()
                } else {
                    // Показываем уведомление о переходе в оффлайн-режим
                    Toast.makeText(
                        context,
                        R.string.offline_mode_enabled,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        
        // Следим за состоянием синхронизации
        applicationScope.launch {
            syncManager.observeSyncState().collectLatest { state ->
                when (state) {
                    SyncState.CONNECTED -> Timber.d("Соединение установлено")
                    SyncState.DISCONNECTED -> Timber.d("Соединение потеряно")
                    SyncState.SYNCING -> Timber.d("Синхронизация данных...")
                }
            }
        }
        
        isInitialized = true
    }
    
    /**
     * Запускает принудительную синхронизацию данных
     */
    fun startSync() {
        syncManager.startImmediateSync()
    }
    
    /**
     * Отменяет синхронизацию данных
     */
    fun cancelSync() {
        syncManager.cancelSync()
    }
    
    /**
     * Проверяет, доступна ли сеть
     */
    fun isNetworkAvailable(): Boolean {
        return networkUtils.isNetworkAvailable()
    }
    
    /**
     * Очищает кэш приложения
     */
    fun clearCache() {
        try {
            // Отменяем текущую синхронизацию
            syncManager.cancelSync()
            
            // Очищаем кэш в менеджере кэша
            context.cacheDir.deleteRecursively()
            
            Timber.d("Cache cleared successfully")
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear cache")
            throw e
        }
    }
    
    /**
     * Возвращает путь к директории кэша
     */
    fun getCacheDirectory(): String {
        return context.cacheDir.absolutePath
    }
} 