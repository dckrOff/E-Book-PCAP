package uz.dckroff.pcap.data.cache.impl

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import uz.dckroff.pcap.data.cache.CacheManager
import java.lang.reflect.Type
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPrefsCacheManager @Inject constructor(
    context: Context,
    private val gson: Gson
) : CacheManager {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        CACHE_PREFS_NAME, Context.MODE_PRIVATE
    )
    
    private val flowMap = mutableMapOf<String, MutableSharedFlow<Any?>>()

    companion object {
        private const val CACHE_PREFS_NAME = "pcap_cache"
        private const val KEY_TIMESTAMP_PREFIX = "timestamp_"
    }

    override suspend fun <T> saveData(key: String, data: T) {
        withContext(Dispatchers.IO) {
            val json = gson.toJson(data)
            sharedPreferences.edit()
                .putString(key, json)
                .putLong(KEY_TIMESTAMP_PREFIX + key, System.currentTimeMillis())
                .apply()
            
            // Обновляем Flow для наблюдателей
            getOrCreateFlow<T>(key).emit(data)
        }
    }

    override suspend fun <T> getData(key: String, defaultValue: T?): T? {
        return withContext(Dispatchers.IO) {
            val json = sharedPreferences.getString(key, null) ?: return@withContext defaultValue
            
            try {
                val type = defaultValue?.javaClass as? Type
                if (type != null) {
                    gson.fromJson<T>(json, type)
                } else {
                    gson.fromJson(json, Any::class.java) as? T ?: defaultValue
                }
            } catch (e: Exception) {
                defaultValue
            }
        }
    }

    override suspend fun isDataStale(key: String, maxAgeMillis: Long): Boolean {
        return withContext(Dispatchers.IO) {
            val timestamp = sharedPreferences.getLong(KEY_TIMESTAMP_PREFIX + key, 0)
            if (timestamp == 0L) return@withContext true
            
            val age = System.currentTimeMillis() - timestamp
            age > maxAgeMillis
        }
    }

    override suspend fun removeData(key: String) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit()
                .remove(key)
                .remove(KEY_TIMESTAMP_PREFIX + key)
                .apply()
            
            // Обновляем Flow для наблюдателей (null означает удаление)
            getOrCreateFlow<Any>(key).emit(null)
        }
    }

    override fun <T> observeData(key: String): Flow<T?> {
        @Suppress("UNCHECKED_CAST")
        return getOrCreateFlow<T>(key).asSharedFlow() as Flow<T?>
    }

    override suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().clear().apply()
            
            // Обновляем все Flow для наблюдателей
            flowMap.values.forEach { flow -> flow.emit(null) }
        }
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun <T> getOrCreateFlow(key: String): MutableSharedFlow<T?> {
        return synchronized(flowMap) {
            if (!flowMap.containsKey(key)) {
                flowMap[key] = MutableSharedFlow<Any?>(replay = 1)
            }
            flowMap[key] as MutableSharedFlow<T?>
        }
    }
} 