package uz.dckroff.pcap.data.cache

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Менеджер кэша для управления кэшированными данными.
 * Предоставляет методы для сохранения, получения и проверки актуальности кэшированных данных.
 */
@Singleton
class CacheManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()
    
    // Время жизни кэша по умолчанию - 24 часа
    private val defaultCacheExpirationTime = 24 * 60 * 60 * 1000L // 24 часа в миллисекундах
    
    // Директория для хранения метаданных кэша
    private val cacheMetadataDir by lazy {
        File(context.cacheDir, "cache_metadata").apply {
            if (!exists()) mkdirs()
        }
    }
    
    // Директория для хранения данных кэша
    private val cacheDataDir by lazy {
        File(context.cacheDir, "cache_data").apply {
            if (!exists()) mkdirs()
        }
    }
    
    // Хранилище потоков для наблюдения за изменениями в кэше
    private val cacheFlows = mutableMapOf<String, MutableStateFlow<Any?>>()
    
    /**
     * Проверяет, устарел ли кэш для указанного ключа
     */
    suspend fun isCacheStale(cacheKey: String, expirationTime: Long = defaultCacheExpirationTime): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val metadataFile = File(cacheMetadataDir, "$cacheKey.meta")
                if (!metadataFile.exists()) {
                    return@withContext true
                }
                
                val timestamp = metadataFile.readText().toLongOrNull() ?: 0L
                val currentTime = System.currentTimeMillis()
                
                currentTime - timestamp > expirationTime
            } catch (e: Exception) {
                Timber.e(e, "Error checking cache staleness for key: $cacheKey")
                true
            }
        }
    }
    
    /**
     * Обновляет метаданные кэша для указанного ключа
     */
    suspend fun updateCacheTimestamp(cacheKey: String) {
        withContext(Dispatchers.IO) {
            try {
                val metadataFile = File(cacheMetadataDir, "$cacheKey.meta")
                metadataFile.writeText(System.currentTimeMillis().toString())
            } catch (e: Exception) {
                Timber.e(e, "Error updating cache timestamp for key: $cacheKey")
            }
        }
    }
    
    /**
     * Удаляет метаданные кэша для указанного ключа
     */
    suspend fun invalidateCache(cacheKey: String) {
        withContext(Dispatchers.IO) {
            try {
                val metadataFile = File(cacheMetadataDir, "$cacheKey.meta")
                if (metadataFile.exists()) {
                    metadataFile.delete()
                }
            } catch (e: Exception) {
                Timber.e(e, "Error invalidating cache for key: $cacheKey")
            }
        }
    }
    
    /**
     * Удаляет все метаданные кэша
     */
    suspend fun clearAllCacheMetadata() {
        withContext(Dispatchers.IO) {
            try {
                cacheMetadataDir.listFiles()?.forEach { it.delete() }
            } catch (e: Exception) {
                Timber.e(e, "Error clearing all cache metadata")
            }
        }
    }
    
    /**
     * Генерирует ключ кэша на основе параметров
     */
    fun generateCacheKey(baseKey: String, vararg params: Any): String {
        return if (params.isEmpty()) {
            baseKey
        } else {
            "$baseKey-${params.joinToString("-")}"
        }
    }

    /**
     * Сохраняет данные в кэш
     *
     * @param key Ключ для идентификации данных
     * @param data Данные для сохранения
     */
    suspend fun <T> saveData(key: String, data: T) {
        withContext(Dispatchers.IO) {
            try {
                val dataFile = File(cacheDataDir, "$key.json")
                val json = gson.toJson(data)
                dataFile.writeText(json)
                
                // Обновляем временную метку
                updateCacheTimestamp(key)
                
                // Обновляем Flow, если он существует
                updateCacheFlow(key, data)
                
                Timber.d("Saved data to cache with key: $key")
            } catch (e: Exception) {
                Timber.e(e, "Error saving data to cache with key: $key")
            }
        }
    }

    /**
     * Получает данные из кэша
     *
     * @param key Ключ для идентификации данных
     * @return Данные из кэша или null, если данные не найдены
     */
    suspend fun <T> getData(key: String, defaultValue: T? = null): T? {
        return withContext(Dispatchers.IO) {
            try {
                val dataFile = File(cacheDataDir, "$key.json")
                if (!dataFile.exists()) {
                    return@withContext defaultValue
                }
                
                val json = dataFile.readText()
                val type = object : TypeToken<T>() {}.type
                gson.fromJson<T>(json, type)
            } catch (e: Exception) {
                Timber.e(e, "Error getting data from cache with key: $key")
                defaultValue
            }
        }
    }

    /**
     * Проверяет, устаревшие ли данные в кэше
     *
     * @param key Ключ для идентификации данных
     * @param maxAgeMillis Максимальный возраст данных в миллисекундах
     * @return true, если данные устарели, иначе false
     */
    suspend fun isDataStale(key: String, maxAgeMillis: Long): Boolean {
        return isCacheStale(key, maxAgeMillis)
    }

    /**
     * Удаляет данные из кэша
     *
     * @param key Ключ для идентификации данных
     */
    suspend fun removeData(key: String) {
        withContext(Dispatchers.IO) {
            try {
                val dataFile = File(cacheDataDir, "$key.json")
                if (dataFile.exists()) {
                    dataFile.delete()
                }
                
                // Удаляем метаданные
                invalidateCache(key)
                
                // Обновляем Flow, если он существует
                updateCacheFlow(key, null)
                
                Timber.d("Removed data from cache with key: $key")
            } catch (e: Exception) {
                Timber.e(e, "Error removing data from cache with key: $key")
            }
        }
    }

    /**
     * Наблюдает за данными в кэше
     *
     * @param key Ключ для идентификации данных
     * @return Flow данных из кэша
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> observeData(key: String): Flow<T?> {
        // Создаем или получаем существующий Flow для данного ключа
        val flow = cacheFlows.getOrPut(key) {
            MutableStateFlow<Any?>(null)
        }
        
        // Запускаем асинхронное получение текущих данных для инициализации Flow
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            try {
                val data = getData<T>(key)
                (flow as MutableStateFlow<T?>).value = data
            } catch (e: Exception) {
                Timber.e(e, "Error initializing cache flow for key: $key")
            }
        }
        
        return flow as StateFlow<T?>
    }

    /**
     * Очищает весь кэш
     */
    suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            try {
                // Очищаем директорию с данными
                cacheDataDir.listFiles()?.forEach { it.delete() }
                
                // Очищаем метаданные
                clearAllCacheMetadata()
                
                // Сбрасываем все Flow
                cacheFlows.keys.forEach { key ->
                    updateCacheFlow(key, null)
                }
                
                Timber.d("All cache cleared")
            } catch (e: Exception) {
                Timber.e(e, "Error clearing all cache")
            }
        }
    }
    
    /**
     * Обновляет Flow с указанным ключом
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> updateCacheFlow(key: String, data: T?) {
        cacheFlows[key]?.let { flow ->
            (flow as MutableStateFlow<T?>).value = data
        }
    }
} 