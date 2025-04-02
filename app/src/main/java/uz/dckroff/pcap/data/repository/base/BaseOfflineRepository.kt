package uz.dckroff.pcap.data.repository.base

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import uz.dckroff.pcap.data.cache.CacheManager
import uz.dckroff.pcap.utils.NetworkUtils

/**
 * Базовый класс репозитория с поддержкой работы офлайн
 */
abstract class BaseOfflineRepository(
    protected val cacheManager: CacheManager,
    protected val networkUtils: NetworkUtils
) {

    /**
     * Получает данные, используя стратегию "Сначала кэш, потом сеть"
     *
     * @param cacheKey Ключ для кэширования данных
     * @param maxAgeMillis Максимальный возраст данных в кэше (миллисекунды)
     * @param fetchFromNetwork Функция для получения данных из сети
     * @param saveToDatabase Функция для сохранения данных в базу данных
     * @param fetchFromDatabase Функция для получения данных из базы данных
     */
    protected fun <T> getWithCacheFirst(
        cacheKey: String,
        maxAgeMillis: Long,
        fetchFromNetwork: suspend () -> T,
        saveToDatabase: suspend (T) -> Unit,
        fetchFromDatabase: () -> Flow<T>
    ): Flow<T> = flow {
        // Проверяем, есть ли данные в кэше и не устарели ли они
        val isDataStale = cacheManager.isDataStale(cacheKey, maxAgeMillis)
        
        // Сначала эмитим данные из базы данных, если они есть
        val localData = fetchFromDatabase()
        emitAll(localData.catch { e ->
            Timber.e(e, "Ошибка при получении данных из базы данных: $cacheKey")
        })
        
        // Если данные устарели или нет локальных данных, и есть подключение к сети,
        // получаем свежие данные
        if ((isDataStale || localData.first() == null) && networkUtils.isNetworkAvailable()) {
            try {
                val remoteData = fetchFromNetwork()
                saveToDatabase(remoteData)
                
                // Обновляем кэш
                cacheManager.saveData(cacheKey, System.currentTimeMillis())
                
                // Эмитим обновленные данные из базы данных
                emitAll(fetchFromDatabase().catch { e ->
                    Timber.e(e, "Ошибка при получении обновленных данных из базы данных: $cacheKey")
                })
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при получении данных из сети: $cacheKey")
                // В случае ошибки при получении данных из сети, просто используем локальные данные
                // Они уже были эмитированы выше
            }
        }
    }
    
    /**
     * Получает данные, используя стратегию "Сначала сеть, потом кэш"
     */
    protected fun <T> getWithNetworkFirst(
        cacheKey: String,
        fetchFromNetwork: suspend () -> T,
        saveToDatabase: suspend (T) -> Unit,
        fetchFromDatabase: () -> Flow<T>
    ): Flow<T> = flow {
        // Если есть подключение к сети, сначала пытаемся получить данные из сети
        if (networkUtils.isNetworkAvailable()) {
            try {
                val remoteData = fetchFromNetwork()
                saveToDatabase(remoteData)
                
                // Обновляем кэш
                cacheManager.saveData(cacheKey, System.currentTimeMillis())
                
                // Эмитим данные из базы данных
                emitAll(fetchFromDatabase().catch { e ->
                    Timber.e(e, "Ошибка при получении данных из базы данных после обновления: $cacheKey")
                })
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при получении данных из сети: $cacheKey")
                
                // В случае ошибки при получении данных из сети, используем локальные данные
                emitAll(fetchFromDatabase().catch { dbError ->
                    Timber.e(dbError, "Ошибка при получении данных из базы данных: $cacheKey")
                })
            }
        } else {
            // Если нет подключения к сети, просто берем данные из локальной базы
            emitAll(fetchFromDatabase().catch { e ->
                Timber.e(e, "Ошибка при получении данных из базы данных: $cacheKey")
            })
        }
    }
} 