package uz.dckroff.pcap.features.reading

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import uz.dckroff.pcap.core.domain.model.Content
import uz.dckroff.pcap.core.domain.usecase.GetContentBySectionUseCase
import uz.dckroff.pcap.core.ui.base.BaseViewModel
import javax.inject.Inject

/**
 * Состояние UI для экрана чтения
 */
data class ReadingState(
    val isLoading: Boolean = false,
    val content: Content? = null,
    val hasPreviousSection: Boolean = false,
    val hasNextSection: Boolean = false,
    val error: String? = null
)

/**
 * События UI для экрана чтения
 */
sealed class ReadingEvent {
    data class LoadContent(val contentId: Long) : ReadingEvent()
    object RefreshContent : ReadingEvent()
    object NextSection : ReadingEvent()
    object PreviousSection : ReadingEvent()
    object AddBookmark : ReadingEvent()
    object AddNote : ReadingEvent()
}

/**
 * Эффекты UI для экрана чтения
 */
sealed class ReadingEffect {
    data class NavigateToContent(val contentId: Long) : ReadingEffect()
    data class AddedBookmark(val contentId: Long) : ReadingEffect()
    data class ShowNote(val contentId: Long, val existingNote: String? = null) : ReadingEffect()
}

/**
 * ViewModel для экрана чтения
 */
@HiltViewModel
class ReadingViewModel @Inject constructor(
    private val getContentBySectionUseCase: GetContentBySectionUseCase
) : BaseViewModel<ReadingState, ReadingEvent, ReadingEffect>() {
    
    // ID текущего контента и раздела
    private var currentContentId: Long = 0
    private var currentSectionId: Long = 0
    
    // Список контента в текущем разделе
    private var sectionContent: List<Content> = emptyList()
    
    // Индекс текущего контента в разделе
    private var currentContentIndex: Int = 0

    init {
        setState(ReadingState())
    }

    override fun handleEvent(event: ReadingEvent) {
        when (event) {
            is ReadingEvent.LoadContent -> {
                loadContent(event.contentId)
            }
            is ReadingEvent.RefreshContent -> {
                loadContent(currentContentId, isRefresh = true)
            }
            is ReadingEvent.NextSection -> {
                navigateToNextContent()
            }
            is ReadingEvent.PreviousSection -> {
                navigateToPreviousContent()
            }
            is ReadingEvent.AddBookmark -> {
                addBookmark()
            }
            is ReadingEvent.AddNote -> {
                showNoteEditor()
            }
        }
    }

    /**
     * Загрузка контента по ID
     */
    private fun loadContent(contentId: Long, isRefresh: Boolean = false) {
        currentContentId = contentId
        
        // Устанавливаем состояние загрузки
        setState(state.value?.copy(isLoading = true, error = null) ?: ReadingState(isLoading = true))
        
        // В реальном приложении здесь был бы запрос к репозиторию для получения контента по ID
        // Для демонстрации создаем моковые данные
        
        // Это пример обработки запроса, который в реальности был бы асинхронным
        viewModelScope.launch {
            try {
                // Симуляция загрузки данных
                val content = Content(
                    id = contentId,
                    sectionId = 1L,  // Для примера используем фиксированный ID раздела
                    title = "Введение в параллельные вычисления",
                    contentType = uz.dckroff.pcap.core.domain.model.ContentType.TEXT,
                    contentData = "<p>Параллельные вычисления - это способ организации компьютерных вычислений, при котором несколько инструкций могут выполняться одновременно.</p><p>Основные преимущества:</p><ul><li>Увеличение производительности</li><li>Эффективное использование ресурсов</li><li>Решение сложных вычислительных задач</li></ul>",
                    orderIndex = 1
                )
                
                currentSectionId = content.sectionId
                
                // Симуляция загрузки всего контента раздела
                sectionContent = listOf(
                    content,
                    Content(
                        id = contentId + 1,
                        sectionId = 1L,
                        title = "История развития параллельных вычислений",
                        contentType = uz.dckroff.pcap.core.domain.model.ContentType.TEXT,
                        contentData = "<p>История параллельных вычислений начинается в 1960-х годах...</p>",
                        orderIndex = 2
                    ),
                    Content(
                        id = contentId + 2,
                        sectionId = 1L,
                        title = "Примеры параллельных алгоритмов",
                        contentType = uz.dckroff.pcap.core.domain.model.ContentType.CODE,
                        contentData = "public void parallelProcessing() {\n  // Пример кода\n  ExecutorService executor = Executors.newFixedThreadPool(4);\n  // Дальнейшая реализация\n}",
                        orderIndex = 3
                    )
                )
                
                // Определяем индекс текущего контента
                currentContentIndex = sectionContent.indexOfFirst { it.id == contentId }.takeIf { it >= 0 } ?: 0
                
                // Обновляем состояние
                setState(ReadingState(
                    isLoading = false,
                    content = content,
                    hasPreviousSection = currentContentIndex > 0,
                    hasNextSection = currentContentIndex < sectionContent.size - 1,
                    error = null
                ))
                
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке контента")
                setState(ReadingState(
                    isLoading = false,
                    error = "Ошибка при загрузке контента: ${e.localizedMessage}"
                ))
            }
        }
    }

    /**
     * Переход к следующему элементу контента
     */
    private fun navigateToNextContent() {
        if (currentContentIndex < sectionContent.size - 1) {
            val nextContent = sectionContent[currentContentIndex + 1]
            sendEffect(ReadingEffect.NavigateToContent(nextContent.id))
        }
    }

    /**
     * Переход к предыдущему элементу контента
     */
    private fun navigateToPreviousContent() {
        if (currentContentIndex > 0) {
            val previousContent = sectionContent[currentContentIndex - 1]
            sendEffect(ReadingEffect.NavigateToContent(previousContent.id))
        }
    }

    /**
     * Добавление закладки для текущего контента
     */
    private fun addBookmark() {
        val content = state.value?.content ?: return
        
        // В реальном приложении здесь был бы запрос к репозиторию для добавления закладки
        // Для демонстрации просто отправляем эффект
        sendEffect(ReadingEffect.AddedBookmark(content.id))
    }

    /**
     * Отображение редактора заметок
     */
    private fun showNoteEditor() {
        val content = state.value?.content ?: return
        
        // В реальном приложении здесь был бы запрос к репозиторию для получения существующей заметки
        // Для демонстрации просто отправляем эффект
        sendEffect(ReadingEffect.ShowNote(content.id))
    }
} 