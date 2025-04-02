package uz.dckroff.pcap.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import uz.dckroff.pcap.core.domain.repository.UserProgressRepository
import uz.dckroff.pcap.data.cache.CacheManager
import uz.dckroff.pcap.database.dao.ChapterDao
import uz.dckroff.pcap.database.dao.SectionDao
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация репозитория для работы с прогрессом пользователя
 */
@Singleton
class UserProgressRepositoryImpl @Inject constructor(
    private val cacheManager: CacheManager,
    private val sectionDao: SectionDao,
    private val chapterDao: ChapterDao
) : UserProgressRepository {

    companion object {
        private const val KEY_READ_SECTIONS = "read_sections"
        private const val KEY_TEST_RESULTS = "test_results"
    }

    /**
     * Отмечает раздел как прочитанный
     */
    override suspend fun markSectionAsRead(sectionId: Long) {
        withContext(Dispatchers.IO) {
            try {
                // Получаем текущий список прочитанных разделов
                val readSections = getReadSections().toMutableList()
                
                // Добавляем раздел, если его еще нет в списке
                if (!readSections.contains(sectionId)) {
                    readSections.add(sectionId)
                    
                    // Сохраняем обновленный список
                    val sectionsJson = readSections.joinToString(",")
                    cacheManager.putString(KEY_READ_SECTIONS, sectionsJson)
                    
                    Timber.d("Раздел $sectionId отмечен как прочитанный")
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при отметке раздела как прочитанного")
            }
        }
    }

    /**
     * Проверяет, прочитан ли раздел
     */
    override suspend fun isSectionRead(sectionId: Long): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                getReadSections().contains(sectionId)
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при проверке статуса прочтения раздела")
                false
            }
        }
    }

    /**
     * Получает список прочитанных разделов
     */
    override suspend fun getReadSections(): List<Long> {
        return withContext(Dispatchers.IO) {
            try {
                val sectionsJson = cacheManager.getString(KEY_READ_SECTIONS, "")
                if (sectionsJson.isBlank()) {
                    emptyList()
                } else {
                    sectionsJson.split(",").mapNotNull { it.toLongOrNull() }
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при получении списка прочитанных разделов")
                emptyList()
            }
        }
    }

    /**
     * Получает процент завершения для главы
     */
    override suspend fun getChapterCompletionPercentage(chapterId: Long): Int {
        return withContext(Dispatchers.IO) {
            try {
                // Получаем все разделы для главы
                val sections = sectionDao.getSectionsByChapter(chapterId)
                if (sections.isEmpty()) return@withContext 0
                
                // Получаем список прочитанных разделов
                val readSections = getReadSections()
                
                // Считаем количество прочитанных разделов в главе
                val readCount = sections.count { readSections.contains(it.id) }
                
                // Вычисляем процент
                (readCount * 100) / sections.size
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при получении процента завершения главы")
                0
            }
        }
    }

    /**
     * Отмечает тест как пройденный с указанием результата
     */
    override suspend fun markTestCompleted(testId: Long, score: Int) {
        withContext(Dispatchers.IO) {
            try {
                // Получаем текущие результаты тестов
                val testResultsStr = cacheManager.getString(KEY_TEST_RESULTS, "{}")
                val testResultsMap = testResultsStr.split(",")
                    .filter { it.contains(":") }
                    .associate { 
                        val parts = it.split(":")
                        parts[0].toLong() to parts[1].toInt()
                    }.toMutableMap()
                
                // Добавляем новый результат или обновляем существующий
                testResultsMap[testId] = score
                
                // Сохраняем обновленные результаты
                val updatedResultsStr = testResultsMap.entries.joinToString(",") { "${it.key}:${it.value}" }
                cacheManager.putString(KEY_TEST_RESULTS, updatedResultsStr)
                
                Timber.d("Тест $testId отмечен как пройденный с результатом $score")
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при сохранении результата теста")
            }
        }
    }

    /**
     * Получает результат прохождения теста
     */
    override suspend fun getTestResult(testId: Long): Int? {
        return withContext(Dispatchers.IO) {
            try {
                // Получаем текущие результаты тестов
                val testResultsStr = cacheManager.getString(KEY_TEST_RESULTS, "{}")
                val testResultsMap = testResultsStr.split(",")
                    .filter { it.contains(":") }
                    .associate { 
                        val parts = it.split(":")
                        parts[0].toLong() to parts[1].toInt()
                    }
                
                // Возвращаем результат для конкретного теста
                testResultsMap[testId]
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при получении результата теста")
                null
            }
        }
    }
} 