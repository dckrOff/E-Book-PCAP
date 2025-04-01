package uz.dckroff.pcap.core.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.dckroff.pcap.core.domain.model.Bookmark

/**
 * Интерфейс репозитория для работы с закладками
 */
interface BookmarksRepository {
    /**
     * Получить все закладки
     */
    fun getAllBookmarks(): Flow<List<Bookmark>>
    
    /**
     * Получить закладку по ID
     */
    suspend fun getBookmarkById(id: Long): Bookmark?
    
    /**
     * Добавить закладку
     */
    suspend fun addBookmark(bookmark: Bookmark): Long
    
    /**
     * Обновить закладку
     */
    suspend fun updateBookmark(bookmark: Bookmark)
    
    /**
     * Удалить закладку
     */
    suspend fun deleteBookmark(id: Long)
    
    /**
     * Проверить, существует ли закладка для контента
     */
    fun hasBookmarkForContent(contentId: Long): Flow<Boolean>
} 