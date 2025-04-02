package uz.dckroff.pcap.features.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uz.dckroff.pcap.data.model.Question
import uz.dckroff.pcap.data.model.Quiz
import uz.dckroff.pcap.data.model.UserAnswer
import uz.dckroff.pcap.data.repository.QuizRepository
import uz.dckroff.pcap.utils.formatTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class QuizResultsViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(QuizResultsState())
    val state: StateFlow<QuizResultsState> = _state.asStateFlow()

    private val quizId: Long = savedStateHandle.get<Long>("quizId") ?: -1L
    private val score: Int = savedStateHandle.get<Int>("score") ?: 0
    private val timestamp: Long = savedStateHandle.get<Long>("timestamp") ?: 0L

    init {
        if (quizId != -1L) {
            loadQuizResults()
        } else {
            _state.value = _state.value.copy(
                error = "Недопустимый ID теста"
            )
        }
    }

    fun onEvent(event: QuizResultsEvent) {
        when (event) {
            is QuizResultsEvent.RetakeQuiz -> {
                _state.value = _state.value.copy(
                    navigateToQuizSession = quizId
                )
            }
            QuizResultsEvent.RetryLoading -> {
                loadQuizResults()
            }
            QuizResultsEvent.NavigationHandled -> {
                _state.value = _state.value.copy(
                    navigateToQuizSession = null
                )
            }
        }
    }

    private fun loadQuizResults() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null
            )

            try {
                // Загрузка данных о тесте
                val quiz = quizRepository.getQuizById(quizId)
                if (quiz == null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Тест не найден"
                    )
                    return@launch
                }

                // Форматируем дату прохождения
                val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
                val attemptDate = dateFormat.format(Date(timestamp))

                // Загружаем вопросы теста
                quizRepository.getQuestionsByQuiz(quizId).collectLatest { questions ->
                    if (questions.isEmpty()) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "Вопросы не найдены"
                        )
                        return@collectLatest
                    }

                    // Загружаем ответы пользователя
                    val userAnswers = quizRepository.getUserAnswersByQuizAndAttempt(quizId, timestamp)
                    val questionResults = createQuestionResults(questions, userAnswers)

                    // Вычисляем статистику
                    val correctAnswers = userAnswers.count { it.isCorrect }
                    val totalTime = calculateTotalTime(quiz, timestamp)

                    _state.value = _state.value.copy(
                        isLoading = false,
                        quiz = quiz,
                        attemptDate = attemptDate,
                        score = score,
                        correctAnswers = correctAnswers,
                        totalQuestions = questions.size,
                        timeSpent = totalTime,
                        questionResults = questionResults
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Ошибка при загрузке результатов теста"
                )
            }
        }
    }

    private fun createQuestionResults(
        questions: List<Question>,
        userAnswers: List<UserAnswer>
    ): List<QuestionResult> {
        val results = mutableListOf<QuestionResult>()

        for (question in questions) {
            val userAnswer = userAnswers.find { it.questionId == question.id }
            if (userAnswer != null) {
                viewModelScope.launch {
                    // Получаем правильные ответы
                    quizRepository.getCorrectAnswersByQuestion(question.id).collectLatest { correctAnswers ->
                        val correctAnswerText = correctAnswers.joinToString(", ") { it.text }
                        
                        // Получаем выбранные пользователем ответы
                        val selectedAnswers = mutableListOf<String>()
                        if (userAnswer.selectedAnswerIds.isNotEmpty()) {
                            val answerIds = userAnswer.selectedAnswerIds.split(",").mapNotNull { it.toLongOrNull() }
                            quizRepository.getAnswersByIds(answerIds).collectLatest { answers ->
                                selectedAnswers.addAll(answers.map { it.text })
                                
                                val result = QuestionResult(
                                    questionId = question.id,
                                    questionNumber = questions.indexOf(question) + 1,
                                    questionText = question.text,
                                    correctAnswer = correctAnswerText,
                                    userAnswer = selectedAnswers.joinToString(", "),
                                    isCorrect = userAnswer.isCorrect,
                                    explanation = question.explanation ?: ""
                                )
                                
                                // Добавляем результат, если он еще не существует
                                if (!results.any { it.questionId == question.id }) {
                                    results.add(result)
                                    
                                    // Обновляем состояние
                                    _state.value = _state.value.copy(
                                        questionResults = results.toList()
                                    )
                                }
                            }
                        } else {
                            // Для текстовых ответов
                            val result = QuestionResult(
                                questionId = question.id,
                                questionNumber = questions.indexOf(question) + 1,
                                questionText = question.text,
                                correctAnswer = correctAnswerText,
                                userAnswer = userAnswer.textAnswer,
                                isCorrect = userAnswer.isCorrect,
                                explanation = question.explanation ?: ""
                            )
                            
                            // Добавляем результат, если он еще не существует
                            if (!results.any { it.questionId == question.id }) {
                                results.add(result)
                                
                                // Обновляем состояние
                                _state.value = _state.value.copy(
                                    questionResults = results.toList()
                                )
                            }
                        }
                    }
                }
            }
        }

        return results
    }

    private fun calculateTotalTime(quiz: Quiz, endTime: Long): String {
        val startTime = quiz.lastAttemptStartTime ?: (endTime - TimeUnit.MINUTES.toMillis(quiz.timeLimit.toLong()))
        val timeSpent = endTime - startTime
        return formatTime(timeSpent)
    }
}

data class QuizResultsState(
    val isLoading: Boolean = false,
    val quiz: Quiz? = null,
    val attemptDate: String = "",
    val score: Int = 0,
    val correctAnswers: Int = 0,
    val totalQuestions: Int = 0,
    val timeSpent: String = "",
    val questionResults: List<QuestionResult> = emptyList(),
    val error: String? = null,
    val navigateToQuizSession: Long? = null
)

data class QuestionResult(
    val questionId: Long,
    val questionNumber: Int,
    val questionText: String,
    val correctAnswer: String,
    val userAnswer: String,
    val isCorrect: Boolean,
    val explanation: String
)

sealed class QuizResultsEvent {
    object RetakeQuiz : QuizResultsEvent()
    object RetryLoading : QuizResultsEvent()
    object NavigationHandled : QuizResultsEvent()
} 