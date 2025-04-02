package uz.dckroff.pcap.features.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import uz.dckroff.pcap.data.model.Bookmark
import uz.dckroff.pcap.data.repository.BookmarkRepository
import uz.dckroff.pcap.navigation.NavigationManager
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val navigationManager: NavigationManager
) : ViewModel() {

    private val _state = MutableStateFlow(BookmarksState())
    val state: StateFlow<BookmarksState> = _state.asStateFlow()

    init {
        loadBookmarks()
    }

    private fun loadBookmarks() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            bookmarkRepository.getAllBookmarks()
                .catch { error ->
                    _state.value = _state.value.copy(
                        error = error.message ?: "Неизвестная ошибка",
                        isLoading = false
                    )
                }
                .collect { bookmarks ->
                    _state.value = _state.value.copy(
                        bookmarks = bookmarks,
                        isLoading = false
                    )
                }
        }
    }

    fun onEvent(event: BookmarksEvent) {
        when (event) {
            is BookmarksEvent.DeleteBookmark -> deleteBookmark(event.bookmark)
            is BookmarksEvent.BookmarkClick -> navigateToContent(event.bookmark)
            BookmarksEvent.Retry -> loadBookmarks()
        }
    }

    private fun deleteBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            try {
                bookmarkRepository.deleteBookmark(bookmark)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Ошибка при удалении закладки"
                )
            }
        }
    }

    private fun navigateToContent(bookmark: Bookmark) {
        navigationManager.navigateToContent(bookmark.sectionId)
    }
}

data class BookmarksState(
    val bookmarks: List<Bookmark> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed class BookmarksEvent {
    data class DeleteBookmark(val bookmark: Bookmark) : BookmarksEvent()
    data class BookmarkClick(val bookmark: Bookmark) : BookmarksEvent()
    object Retry : BookmarksEvent()
} 