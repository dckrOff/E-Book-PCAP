package uz.dckroff.pcap.features.content

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import uz.dckroff.pcap.core.domain.model.ChapterWithSections
import uz.dckroff.pcap.core.domain.usecase.GetBookContentsUseCase
import uz.dckroff.pcap.core.ui.base.BaseViewModel
import javax.inject.Inject

/**
 * Состояние UI для экрана содержания
 */
data class ContentListState(
    val isLoading: Boolean = false,
    val content: List<ChapterWithSections> = emptyList(),
    val error: String? = null
)

/**
 * События UI для экрана содержания
 */
sealed class ContentListEvent {
    object LoadContent : ContentListEvent()
    data class ContentItemClicked(val sectionId: Long, val contentId: Long) : ContentListEvent()
}

/**
 * Эффекты UI для экрана содержания
 */
sealed class ContentListEffect {
    data class NavigateToContent(val contentId: Long) : ContentListEffect()
}

/**
 * ViewModel для экрана содержания
 */
@HiltViewModel
class ContentListViewModel @Inject constructor(
    private val getBookContentsUseCase: GetBookContentsUseCase
) : BaseViewModel<ContentListState, ContentListEvent, ContentListEffect>() {

    init {
        setState(ContentListState())
    }

    override fun handleEvent(event: ContentListEvent) {
        when (event) {
            is ContentListEvent.LoadContent -> {
                loadContent()
            }
            is ContentListEvent.ContentItemClicked -> {
                navigateToContent(event.contentId)
            }
        }
    }

    /**
     * Загрузка структуры содержания учебника
     */
    private fun loadContent() {
        // Устанавливаем состояние загрузки
        setState(state.value?.copy(isLoading = true, error = null) ?: ContentListState(isLoading = true))
        
        // В реальном приложении здесь был бы запрос к репозиторию для получения структуры учебника
        // Для демонстрации создаем моковые данные
        
        viewModelScope.launch {
            try {
                // Симуляция загрузки данных - создаем тестовую структуру учебника
                val chapters = listOf(
                    ChapterWithSections(
                        id = 1L,
                        title = "Глава 1. Введение в параллельные вычисления",
                        orderIndex = 1,
                        sections = listOf(
                            uz.dckroff.pcap.core.domain.model.Section(
                                id = 1L,
                                chapterId = 1L,
                                title = "1.1 Основные понятия параллельных вычислений",
                                orderIndex = 1,
                                contentId = 101L
                            ),
                            uz.dckroff.pcap.core.domain.model.Section(
                                id = 2L,
                                chapterId = 1L,
                                title = "1.2 История развития параллельных вычислений",
                                orderIndex = 2,
                                contentId = 102L
                            ),
                            uz.dckroff.pcap.core.domain.model.Section(
                                id = 3L,
                                chapterId = 1L,
                                title = "1.3 Примеры параллельных алгоритмов",
                                orderIndex = 3,
                                contentId = 103L
                            )
                        )
                    ),
                    ChapterWithSections(
                        id = 2L,
                        title = "Глава 2. Архитектура параллельных вычислительных систем",
                        orderIndex = 2,
                        sections = listOf(
                            uz.dckroff.pcap.core.domain.model.Section(
                                id = 4L,
                                chapterId = 2L,
                                title = "2.1 Классификация параллельных архитектур",
                                orderIndex = 1,
                                contentId = 201L
                            ),
                            uz.dckroff.pcap.core.domain.model.Section(
                                id = 5L,
                                chapterId = 2L,
                                title = "2.2 Многоядерные процессоры",
                                orderIndex = 2,
                                contentId = 202L
                            ),
                            uz.dckroff.pcap.core.domain.model.Section(
                                id = 6L,
                                chapterId = 2L,
                                title = "2.3 GPU и специализированные ускорители",
                                orderIndex = 3,
                                contentId = 203L
                            )
                        )
                    )
                )
                
                // Обновляем состояние
                setState(ContentListState(
                    isLoading = false,
                    content = chapters,
                    error = null
                ))
                
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке содержания")
                setState(ContentListState(
                    isLoading = false,
                    error = "Ошибка при загрузке содержания: ${e.localizedMessage}"
                ))
            }
        }
    }

    /**
     * Навигация к контенту
     */
    private fun navigateToContent(contentId: Long) {
        sendEffect(ContentListEffect.NavigateToContent(contentId))
    }
}