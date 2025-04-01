package uz.dckroff.pcap.core.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.dckroff.pcap.core.domain.model.Chapter
import uz.dckroff.pcap.core.domain.model.Content
import uz.dckroff.pcap.core.domain.model.Section

/**
 * Интерфейс репозитория для работы с контентом учебника
 */
interface ContentRepository {
    /**
     * Получить все главы
     */
    fun getChapters(): Flow<List<Chapter>>
    
    /**
     * Получить главу по ID
     */
    suspend fun getChapterById(id: Long): Chapter?
    
    /**
     * Получить разделы для главы
     */
    fun getSectionsByChapterId(chapterId: Long): Flow<List<Section>>
    
    /**
     * Получить раздел по ID
     */
    suspend fun getSectionById(id: Long): Section?
    
    /**
     * Получить контент для раздела
     */
    fun getContentBySectionId(sectionId: Long): Flow<List<Content>>
    
    /**
     * Получить элемент контента по ID
     */
    suspend fun getContentById(id: Long): Content?
    
    /**
     * Поиск по контенту
     */
    fun searchContent(query: String): Flow<List<Content>>
} 