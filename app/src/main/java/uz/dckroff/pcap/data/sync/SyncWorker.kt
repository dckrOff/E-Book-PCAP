package uz.dckroff.pcap.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import uz.dckroff.pcap.data.repository.ContentRepository
import uz.dckroff.pcap.data.repository.GlossaryRepository
import uz.dckroff.pcap.data.repository.QuizRepository

/**
 * Worker для выполнения синхронизации данных в фоне
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val contentRepository: ContentRepository,
    private val glossaryRepository: GlossaryRepository,
    private val quizRepository: QuizRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Timber.d("Начинаем синхронизацию данных")
            
            // Синхронизация контента
            syncContent()
            
            // Синхронизация глоссария
            syncGlossary()
            
            // Синхронизация тестов
            syncQuizzes()
            
            Timber.d("Синхронизация данных завершена успешно")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при синхронизации данных")
            Result.retry()
        }
    }
    
    /**
     * Синхронизирует контент (главы, разделы, содержимое)
     */
    private suspend fun syncContent() {
        Timber.d("Синхронизация контента")
        contentRepository.syncContent()
    }
    
    /**
     * Синхронизирует глоссарий (термины и связанные разделы)
     */
    private suspend fun syncGlossary() {
        Timber.d("Синхронизация глоссария")
        glossaryRepository.syncTerms()
    }
    
    /**
     * Синхронизирует тесты и вопросы
     */
    private suspend fun syncQuizzes() {
        Timber.d("Синхронизация тестов")
        quizRepository.syncQuizzes()
    }
} 