package uz.dckroff.pcap.core.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.dckroff.pcap.core.domain.model.UserProgress

/**
 * Интерфейс репозитория для работы с прогрессом пользователя
 */
interface UserProgressRepository {
    /**
     * Получить прогресс для раздела
     */
    fun getProgressForSection(sectionId: Long): Flow<UserProgress?>
    
    /**
     * Получить весь прогресс пользователя
     */
    fun getAllProgress(): Flow<List<UserProgress>>
    
    /**
     * Обновить прогресс для раздела
     */
    suspend fun updateProgress(progress: UserProgress)
    
    /**
     * Получить общий процент завершения учебника
     */
    fun getTotalCompletionPercentage(): Flow<Int>
    
    /**
     * Получить процент завершения для главы
     */
    fun getChapterCompletionPercentage(chapterId: Long): Flow<Int>
} 