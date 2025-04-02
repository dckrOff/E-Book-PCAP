// Ключи для настроек оффлайн-режима
private val KEY_AUTO_SYNC = stringPreferencesKey("auto_sync_enabled")
private val KEY_DOWNLOAD_ALL_CONTENT = stringPreferencesKey("download_all_content")
private val KEY_LAST_SYNC_TIME = stringPreferencesKey("last_sync_time")

/**
 * Сохраняет настройку автоматической синхронизации
 */
suspend fun setAutoSyncEnabled(enabled: Boolean) {
    dataStore.edit { preferences ->
        preferences[KEY_AUTO_SYNC] = enabled.toString()
    }
}

/**
 * Получает настройку автоматической синхронизации
 */
suspend fun getAutoSyncEnabled(): Boolean {
    return dataStore.data.first()[KEY_AUTO_SYNC]?.toBoolean() ?: true
}

/**
 * Сохраняет настройку скачивания всего контента
 */
suspend fun setDownloadAllContent(enabled: Boolean) {
    dataStore.edit { preferences ->
        preferences[KEY_DOWNLOAD_ALL_CONTENT] = enabled.toString()
    }
}

/**
 * Получает настройку скачивания всего контента
 */
suspend fun getDownloadAllContent(): Boolean {
    return dataStore.data.first()[KEY_DOWNLOAD_ALL_CONTENT]?.toBoolean() ?: false
}

/**
 * Сохраняет время последней синхронизации
 */
suspend fun setLastSyncTime(timestamp: Long) {
    dataStore.edit { preferences ->
        preferences[KEY_LAST_SYNC_TIME] = timestamp.toString()
    }
}

/**
 * Получает время последней синхронизации
 */
suspend fun getLastSyncTime(): Long {
    return dataStore.data.first()[KEY_LAST_SYNC_TIME]?.toLongOrNull() ?: 0L
} 