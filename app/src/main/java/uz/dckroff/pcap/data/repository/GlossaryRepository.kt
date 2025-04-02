/**
 * Синхронизирует термины с сервером
 * В случае офлайн-режима работает с локальными данными
 */
suspend fun syncTerms() {
    try {
        // Получаем данные с сервера, если есть соединение
        if (networkUtils.isNetworkAvailable()) {
            val terms = remoteDataSource.getTerms()
            
            // Сохраняем термины в базу данных
            termDao.insertTerms(terms)
            
            // Для каждого термина получаем связанные разделы
            terms.forEach { term ->
                val relatedSections = remoteDataSource.getRelatedSections(term.id)
                
                // Сохраняем связи терминов и разделов
                termSectionCrossRefDao.insertTermSectionCrossRefs(
                    relatedSections.map { 
                        TermSectionCrossRef(termId = term.id, sectionId = it.id) 
                    }
                )
            }
            
            // Сохраняем данные в кэш
            cacheManager.saveData(CACHE_KEY_GLOSSARY_SYNCED, true)
            cacheManager.saveData(CACHE_KEY_GLOSSARY_LAST_SYNC, System.currentTimeMillis())
        }
    } catch (e: Exception) {
        Timber.e(e, "Ошибка при синхронизации терминов")
        // Если произошла ошибка, используем локальные данные
    }
}

companion object {
    private const val CACHE_KEY_GLOSSARY_SYNCED = "glossary_synced"
    private const val CACHE_KEY_GLOSSARY_LAST_SYNC = "glossary_last_sync"
} 