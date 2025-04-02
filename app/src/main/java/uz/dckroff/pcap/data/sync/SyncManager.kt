package uz.dckroff.pcap.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uz.dckroff.pcap.utils.NetworkUtils
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Менеджер синхронизации данных
 */
@Singleton
class SyncManager @Inject constructor(
    private val context: Context,
    private val networkUtils: NetworkUtils
) {
    private val workManager = WorkManager.getInstance(context)
    
    companion object {
        private const val SYNC_WORK_NAME = "data_sync_work"
        private const val SYNC_INTERVAL_HOURS = 6L
    }
    
    /**
     * Инициализирует периодическую синхронизацию данных
     */
    fun initialize() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            SYNC_INTERVAL_HOURS, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )
    }
    
    /**
     * Запускает немедленную синхронизацию данных
     */
    fun startImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            SYNC_INTERVAL_HOURS, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            syncWorkRequest
        )
    }
    
    /**
     * Отменяет все задачи синхронизации
     */
    fun cancelSync() {
        workManager.cancelUniqueWork(SYNC_WORK_NAME)
    }
    
    /**
     * Возвращает Flow с состоянием синхронизации
     */
    fun observeSyncState(): Flow<SyncState> {
        return networkUtils.observeNetworkState().map { isConnected ->
            when {
                isConnected -> SyncState.CONNECTED
                else -> SyncState.DISCONNECTED
            }
        }
    }
}

/**
 * Состояние синхронизации
 */
enum class SyncState {
    CONNECTED,      // Подключено к сети, синхронизация возможна
    DISCONNECTED,   // Нет подключения к сети, работа в офлайн режиме
    SYNCING         // Выполняется синхронизация данных
} 