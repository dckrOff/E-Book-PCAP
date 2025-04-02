package uz.dckroff.pcap.features.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uz.dckroff.pcap.data.model.Quiz
import uz.dckroff.pcap.data.repository.QuizRepository
import javax.inject.Inject

@HiltViewModel
class QuizListViewModel @Inject constructor(
    private val quizRepository: QuizRepository
) : ViewModel() {

    enum class CompletionStatus {
        ALL, COMPLETED, PENDING
    }

    private val _state = MutableStateFlow(QuizListState())
    val state: StateFlow<QuizListState> = _state.asStateFlow()

    private var filterByCompletion: Boolean? = null
    private var filterByDifficulty: Int? = null

    init {
        loadQuizzes()
    }

    fun onEvent(event: QuizListEvent) {
        when (event) {
            is QuizListEvent.FilterByCompletion -> {
                _state.value = _state.value.copy(completionFilter = event.status)
                applyFilters()
            }
            is QuizListEvent.FilterByDifficulty -> {
                _state.value = _state.value.copy(difficultyFilter = event.difficulty)
                applyFilters()
            }
            is QuizListEvent.RetryLoading -> {
                loadQuizzes()
            }
            is QuizListEvent.StartQuiz -> {
                _state.value = _state.value.copy(
                    navigationEvent = NavigationEvent.NavigateToQuizSession(event.quizId)
                )
            }
            is QuizListEvent.ViewResults -> {
                val quiz = _state.value.quizzes.find { it.id == event.quizId }
                if (quiz != null && quiz.isCompleted) {
                    _state.value = _state.value.copy(
                        navigationEvent = NavigationEvent.NavigateToQuizResults(
                            event.quizId,
                            quiz.score,
                            quiz.lastAttemptDate ?: System.currentTimeMillis()
                        )
                    )
                }
            }
            is QuizListEvent.NavigationHandled -> {
                _state.value = _state.value.copy(navigationEvent = null)
            }
        }
    }

    private fun loadQuizzes() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null
            )
            
            try {
                val quizzes = quizRepository.getAllQuizzes()
                _state.value = _state.value.copy(
                    isLoading = false,
                    quizzes = quizzes,
                    filteredQuizzes = quizzes
                )
                
                // Применяем текущие фильтры, если они установлены
                if (_state.value.completionFilter != CompletionStatus.ALL || 
                    _state.value.difficultyFilter.isNotEmpty()) {
                    applyFilters()
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Не удалось загрузить тесты"
                )
            }
        }
    }

    private fun applyFilters() {
        val allQuizzes = _state.value.quizzes
        val filteredQuizzes = filterQuizzes(
            allQuizzes,
            _state.value.completionFilter,
            _state.value.difficultyFilter
        )
        _state.value = _state.value.copy(filteredQuizzes = filteredQuizzes)
    }

    private fun filterQuizzes(
        quizzes: List<Quiz>,
        completionStatus: CompletionStatus,
        difficulty: String
    ): List<Quiz> {
        var filtered = quizzes
        
        // Фильтрация по статусу выполнения
        filtered = when (completionStatus) {
            CompletionStatus.ALL -> filtered
            CompletionStatus.COMPLETED -> filtered.filter { it.isCompleted }
            CompletionStatus.PENDING -> filtered.filter { !it.isCompleted }
        }
        
        // Фильтрация по сложности
        if (difficulty.isNotEmpty() && difficulty != "all") {
            filtered = filtered.filter { it.difficulty.equals(difficulty, ignoreCase = true) }
        }
        
        return filtered
    }
}

data class QuizListState(
    val quizzes: List<Quiz> = emptyList(),
    val filteredQuizzes: List<Quiz> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val completionFilter: CompletionStatus = CompletionStatus.ALL,
    val difficultyFilter: String = "",
    val navigationEvent: NavigationEvent? = null
)

sealed class QuizListEvent {
    data class FilterByCompletion(val status: CompletionStatus) : QuizListEvent()
    data class FilterByDifficulty(val difficulty: String) : QuizListEvent()
    object RetryLoading : QuizListEvent()
    data class StartQuiz(val quizId: Long) : QuizListEvent()
    data class ViewResults(val quizId: Long) : QuizListEvent()
    object NavigationHandled : QuizListEvent()
}

sealed class NavigationEvent {
    data class NavigateToQuizSession(val quizId: Long) : NavigationEvent()
    data class NavigateToQuizResults(val quizId: Long, val score: Int, val timestamp: Long) : NavigationEvent()
} 