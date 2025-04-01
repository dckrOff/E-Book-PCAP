package uz.dckroff.pcap.core.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.dckroff.pcap.core.domain.model.Test
import uz.dckroff.pcap.core.domain.model.TestQuestion

/**
 * Интерфейс репозитория для работы с тестами
 */
interface TestRepository {
    /**
     * Получить тесты для раздела
     */
    fun getTestsBySectionId(sectionId: Long): Flow<List<Test>>
    
    /**
     * Получить тест по ID
     */
    suspend fun getTestById(id: Long): Test?
    
    /**
     * Получить вопросы для теста
     */
    fun getQuestionsByTestId(testId: Long): Flow<List<TestQuestion>>
    
    /**
     * Получить вопрос по ID
     */
    suspend fun getQuestionById(id: Long): TestQuestion?
    
    /**
     * Проверить правильность ответов на вопросы теста
     * @param answers Карта ID вопроса -> ответ пользователя
     * @return Карта ID вопроса -> результат проверки (true/false)
     */
    suspend fun checkTestAnswers(testId: Long, answers: Map<Long, String>): Map<Long, Boolean>
} 