package uz.dckroff.pcap.features.reading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import uz.dckroff.pcap.core.domain.model.Content
import uz.dckroff.pcap.core.domain.model.Section
import uz.dckroff.pcap.core.domain.repository.BookmarksRepository
import uz.dckroff.pcap.core.domain.repository.ContentRepository
import uz.dckroff.pcap.core.domain.repository.UserProgressRepository
import uz.dckroff.pcap.core.domain.usecase.GetContentBySectionUseCase
import uz.dckroff.pcap.data.model.Bookmark
import javax.inject.Inject

/**
 * ViewModel для экрана чтения контента
 */
@HiltViewModel
class ReadingViewModel @Inject constructor(
    private val getContentBySectionUseCase: GetContentBySectionUseCase,
    private val contentRepository: ContentRepository,
    private val bookmarksRepository: BookmarksRepository,
    private val userProgressRepository: UserProgressRepository
) : ViewModel() {

    // Состояние контента
    private val _contentState = MutableStateFlow(ContentState())
    val contentState: StateFlow<ContentState> = _contentState.asStateFlow()

    // Состояние закладки
    private val _isBookmarked = MutableStateFlow(false)
    val isBookmarked: StateFlow<Boolean> = _isBookmarked.asStateFlow()

    /**
     * Загружает контент для указанного раздела
     */
    fun loadContent(sectionId: Long) {
        viewModelScope.launch {
            try {
                _contentState.value = _contentState.value.copy(isLoading = true)

                // Загружаем данные о разделе
                val section = contentRepository.getSectionById(sectionId)
                if (section == null) {
                    _contentState.value = _contentState.value.copy(
                        isLoading = false,
                        error = "Раздел не найден"
                    )
                    return@launch
                }

                // Загружаем контент для раздела
                val contentItems = getContentBySectionUseCase(sectionId)
                
                // Сортируем контент по индексу
                val sortedContent = contentItems.sortedBy { it.orderIndex }

                _contentState.value = _contentState.value.copy(
                    isLoading = false,
                    section = section,
                    contentItems = sortedContent,
                    error = null
                )

                // Проверяем, есть ли закладка для этого раздела
                checkBookmarkStatus(sectionId)
                
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке контента для раздела $sectionId")
                _contentState.value = _contentState.value.copy(
                    isLoading = false,
                    error = "Ошибка загрузки: ${e.message}"
                )
            }
        }
    }

    /**
     * Проверяет, добавлен ли раздел в закладки
     */
    private fun checkBookmarkStatus(sectionId: Long) {
        viewModelScope.launch {
            try {
                val bookmarks = bookmarksRepository.getBookmarks()
                _isBookmarked.value = bookmarks.any { it.sectionId == sectionId }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при проверке статуса закладки")
            }
        }
    }

    /**
     * Переключает статус закладки для раздела
     */
    fun toggleBookmark(sectionId: Long) {
        viewModelScope.launch {
            try {
                val section = contentState.value.section ?: return@launch
                
                if (_isBookmarked.value) {
                    // Удаляем закладку
                    bookmarksRepository.removeBookmark(sectionId)
                    _isBookmarked.value = false
                } else {
                    // Добавляем закладку
                    val bookmark = Bookmark(
                        bookmarkId = 0, // ID будет сгенерирован базой данных
                        sectionId = sectionId,
                        title = section.title,
                        timestamp = System.currentTimeMillis()
                    )
                    bookmarksRepository.addBookmark(bookmark)
                    _isBookmarked.value = true
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при переключении закладки")
            }
        }
    }
    
    /**
     * Отмечает раздел как прочитанный
     */
    fun markSectionAsRead(sectionId: Long) {
        viewModelScope.launch {
            try {
                userProgressRepository.markSectionAsRead(sectionId)
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при отметке раздела как прочитанного")
            }
        }
    }
}

/**
 * Состояние экрана чтения
 */
data class ContentState(
    val isLoading: Boolean = false,
    val section: Section? = null,
    val contentItems: List<Content> = emptyList(),
    val error: String? = null
) 