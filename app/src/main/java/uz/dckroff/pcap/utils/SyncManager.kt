package uz.dckroff.pcap.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import uz.dckroff.pcap.data.preferences.UserPreferences
import uz.dckroff.pcap.data.repository.BookRepository
import uz.dckroff.pcap.data.repository.ChapterRepository
import uz.dckroff.pcap.data.repository.QuizRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Менеджер синхронизации данных.
 * Отвечает за загрузку и кэширование всех необходимых данных для оффлайн-режима.
 */
@Singleton
class SyncManager @Inject constructor(
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val quizRepository: QuizRepository,
    private val userPreferences: UserPreferences
) {
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var syncJob: Job? = null
    
    /**
     * Запускает синхронизацию всех данных
     */
    fun startSync() {
        if (syncJob?.isActive == true) {
            Timber.d("Sync already in progress")
            return
        }
        
        syncJob = syncScope.launch {
            try {
                Timber.d("Starting sync")
                
                // 1. Синхронизируем книги
                val books = bookRepository.getAllBooks().getOrThrow()
                Timber.d("Synced ${books.size} books")
                
                // 2. Синхронизируем главы для каждой книги
                books.forEach { book ->
                    val chapters = chapterRepository.getChaptersByBookId(book.id).getOrThrow()
                    Timber.d("Synced ${chapters.size} chapters for book ${book.title}")
                    
                    // 3. Если включена настройка скачивания всего контента,
                    // загружаем содержимое всех глав
                    if (userPreferences.getDownloadAllContent()) {
                        chapters.forEach { chapter ->
                            chapterRepository.getChapterContent(chapter.id).getOrThrow()
                            Timber.d("Downloaded content for chapter ${chapter.title}")
                        }
                    }
                }
                
                // 4. Синхронизируем тесты
                val quizzes = quizRepository.getAllQuizzes().getOrThrow()
                Timber.d("Synced ${quizzes.size} quizzes")
                
                // 5. Если включена настройка скачивания всего контента,
                // загружаем вопросы для всех тестов
                if (userPreferences.getDownloadAllContent()) {
                    quizzes.forEach { quiz ->
                        quizRepository.getQuizQuestions(quiz.id).getOrThrow()
                        Timber.d("Downloaded questions for quiz ${quiz.title}")
                    }
                }
                
                // Обновляем время последней синхронизации
                userPreferences.setLastSyncTime(System.currentTimeMillis())
                Timber.d("Sync completed successfully")
            } catch (e: Exception) {
                Timber.e(e, "Sync failed")
                throw e
            }
        }
    }
    
    /**
     * Отменяет текущую синхронизацию
     */
    fun cancelSync() {
        syncJob?.cancel()
        syncJob = null
        Timber.d("Sync cancelled")
    }
    
    /**
     * Проверяет, идет ли в данный момент синхронизация
     */
    fun isSyncing(): Boolean {
        return syncJob?.isActive == true
    }
} 