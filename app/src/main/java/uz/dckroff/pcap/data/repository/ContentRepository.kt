/**
 * Синхронизирует контент с сервером
 * В случае офлайн-режима работает с локальными данными
 */
suspend fun syncContent() {
    try {
        // Получаем данные с сервера, если есть соединение
        if (networkUtils.isNetworkAvailable()) {
            val chapters = remoteDataSource.getChapters()
            
            // Сохраняем главы в базу данных
            chapterDao.insertChapters(chapters)
            
            // Для каждой главы получаем разделы
            chapters.forEach { chapter ->
                val sections = remoteDataSource.getSections(chapter.id)
                
                // Сохраняем разделы в базу данных
                sectionDao.insertSections(sections)
                
                // Для каждого раздела получаем содержимое
                sections.forEach { section ->
                    val content = remoteDataSource.getContent(section.id)
                    
                    // Сохраняем содержимое в базу данных
                    contentDao.insertContent(content)
                }
            }
            
            // Сохраняем данные в кэш
            cacheManager.saveData(CACHE_KEY_CONTENT_SYNCED, true)
            cacheManager.saveData(CACHE_KEY_CONTENT_LAST_SYNC, System.currentTimeMillis())
        }
    } catch (e: Exception) {
        Timber.e(e, "Ошибка при синхронизации контента")
        // Если произошла ошибка, используем локальные данные
    }
}

companion object {
    private const val CACHE_KEY_CONTENT_SYNCED = "content_synced"
    private const val CACHE_KEY_CONTENT_LAST_SYNC = "content_last_sync"
} 