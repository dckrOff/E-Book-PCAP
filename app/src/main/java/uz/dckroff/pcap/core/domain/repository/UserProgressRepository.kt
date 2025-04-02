package uz.dckroff.pcap.core.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.dckroff.pcap.core.domain.model.UserProgress

/**
 * Репозиторий для работы с прогрессом пользователя
 */
interface UserProgressRepository {
    
    /**
     * Отмечает раздел как прочитанный
     * @param sectionId ID раздела
     */
    suspend fun markSectionAsRead(sectionId: Long)
    
    /**
     * Проверяет, прочитан ли раздел
     * @param sectionId ID раздела
     * @return true, если раздел прочитан, иначе false
     */
    suspend fun isSectionRead(sectionId: Long): Boolean
    
    /**
     * Получает список прочитанных разделов
     * @return список ID прочитанных разделов
     */
    suspend fun getReadSections(): List<Long>
    
    /**
     * Получает процент завершения для главы
     * @param chapterId ID главы
     * @return процент завершения (0-100)
     */
    suspend fun getChapterCompletionPercentage(chapterId: Long): Int
    
    /**
     * Отмечает тест как пройденный с указанием результата
     * @param testId ID теста
     * @param score результат (процент правильных ответов, 0-100)
     */
    suspend fun markTestCompleted(testId: Long, score: Int)
    
    /**
     * Получает результат прохождения теста
     * @param testId ID теста
     * @return результат прохождения или null, если тест не пройден
     */
    suspend fun getTestResult(testId: Long): Int?
} 