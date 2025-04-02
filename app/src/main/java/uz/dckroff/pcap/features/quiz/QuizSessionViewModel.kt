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
import uz.dckroff.pcap.data.model.Answer
import uz.dckroff.pcap.data.model.Question
import uz.dckroff.pcap.data.model.Quiz
import uz.dckroff.pcap.data.model.UserAnswer
import uz.dckroff.pcap.data.repository.QuizRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class QuizSessionViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(QuizSessionState())
    val state: StateFlow<QuizSessionState> = _state.asStateFlow()
    
    private val quizId: Long = savedStateHandle.get<Long>("quizId") ?: -1L
    private var questions: List<Question> = emptyList()
    private var currentQuestionAnswers: List<Answer> = emptyList()
    private val userAnswers = mutableMapOf<Long, UserAnswer>()
    private var quizStartTime: Long = 0
    
    init {
        if (quizId != -1L) {
            loadQuiz()
        } else {
            _state.value = _state.value.copy(
                error = "Недопустимый ID теста"
            )
        }
    }
    
    fun onEvent(event: QuizSessionEvent) {
        when (event) {
            is QuizSessionEvent.AnswerSelected -> {
                handleAnswerSelection(event.questionId, event.answerId, event.isSelected)
            }
            is QuizSessionEvent.TextAnswerChanged -> {
                handleTextAnswer(event.questionId, event.answer)
            }
            is QuizSessionEvent.NextQuestion -> {
                navigateToNextQuestion()
            }
            is QuizSessionEvent.PreviousQuestion -> {
                navigateToPreviousQuestion()
            }
            is QuizSessionEvent.NavigateToQuestion -> {
                navigateToQuestion(event.index)
            }
            is QuizSessionEvent.FinishQuiz -> {
                finishQuiz()
            }
            QuizSessionEvent.NavigationHandled -> {
                _state.value = _state.value.copy(
                    navigationEvent = null
                )
            }
        }
    }
    
    private fun loadQuiz() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                // Загрузка данных теста
                val quiz = quizRepository.getQuizById(quizId)
                
                if (quiz == null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Тест не найден"
                    )
                    return@launch
                }
                
                // Запуск таймера
                quizStartTime = System.currentTimeMillis()
                
                // Загрузка вопросов
                quizRepository.getQuestionsByQuiz(quizId).collectLatest { loadedQuestions ->
                    questions = loadedQuestions
                    
                    if (questions.isEmpty()) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "Вопросы не найдены"
                        )
                        return@collectLatest
                    }
                    
                    // Загрузка ответов для первого вопроса
                    loadAnswersForQuestion(questions.firstOrNull()?.id ?: -1)
                    
                    _state.value = _state.value.copy(
                        isLoading = false,
                        quiz = quiz,
                        questions = loadedQuestions,
                        currentQuestionIndex = 0,
                        totalQuestions = loadedQuestions.size,
                        currentQuestion = loadedQuestions.firstOrNull(),
                        timeLimit = quiz.timeLimit,
                        startTime = quizStartTime
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Ошибка при загрузке теста"
                )
            }
        }
    }
    
    private fun loadAnswersForQuestion(questionId: Long) {
        if (questionId == -1L) return
        
        viewModelScope.launch {
            try {
                quizRepository.getAnswersByQuestion(questionId).collectLatest { answers ->
                    currentQuestionAnswers = answers
                    _state.value = _state.value.copy(
                        currentAnswers = answers
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Ошибка при загрузке вариантов ответа"
                )
            }
        }
    }
    
    private fun handleAnswerSelection(questionId: Long, answerId: Long, isSelected: Boolean) {
        val currentQuestion = _state.value.currentQuestion ?: return
        
        // Обрабатываем в зависимости от типа вопроса
        when (currentQuestion.type) {
            1 -> { // Single choice
                // Если выбран вариант, сохраняем только его, иначе удаляем выбор
                if (isSelected) {
                    userAnswers[questionId] = UserAnswer(
                        questionId = questionId,
                        quizId = quizId,
                        selectedAnswerIds = answerId.toString()
                    )
                } else {
                    userAnswers.remove(questionId)
                }
            }
            2 -> { // Multiple choice
                val existingAnswer = userAnswers[questionId]
                if (existingAnswer != null) {
                    val selectedIds = existingAnswer.selectedAnswerIds
                        .split(",")
                        .filter { it.isNotEmpty() }
                        .map { it.toLong() }
                        .toMutableList()
                    
                    if (isSelected) {
                        if (!selectedIds.contains(answerId)) {
                            selectedIds.add(answerId)
                        }
                    } else {
                        selectedIds.remove(answerId)
                    }
                    
                    if (selectedIds.isNotEmpty()) {
                        userAnswers[questionId] = existingAnswer.copy(
                            selectedAnswerIds = selectedIds.joinToString(",")
                        )
                    } else {
                        userAnswers.remove(questionId)
                    }
                } else if (isSelected) {
                    userAnswers[questionId] = UserAnswer(
                        questionId = questionId,
                        quizId = quizId,
                        selectedAnswerIds = answerId.toString()
                    )
                }
            }
            3 -> { // True/False
                if (isSelected) {
                    userAnswers[questionId] = UserAnswer(
                        questionId = questionId,
                        quizId = quizId,
                        selectedAnswerIds = answerId.toString()
                    )
                } else {
                    userAnswers.remove(questionId)
                }
            }
        }
        
        updateQuestionStatus()
    }
    
    private fun handleTextAnswer(questionId: Long, answer: String) {
        val currentQuestion = _state.value.currentQuestion ?: return
        
        if (currentQuestion.type == 4) { // Text input
            if (answer.isNotEmpty()) {
                userAnswers[questionId] = UserAnswer(
                    questionId = questionId,
                    quizId = quizId,
                    selectedAnswerIds = "",
                    textAnswer = answer
                )
            } else {
                userAnswers.remove(questionId)
            }
            
            updateQuestionStatus()
        }
    }
    
    private fun updateQuestionStatus() {
        val answeredQuestions = userAnswers.keys.size
        val progress = if (questions.isNotEmpty()) {
            (answeredQuestions * 100) / questions.size
        } else {
            0
        }
        
        val currentQuestionId = _state.value.currentQuestion?.id ?: -1
        val isCurrentQuestionAnswered = userAnswers.containsKey(currentQuestionId)
        
        _state.value = _state.value.copy(
            answeredQuestions = answeredQuestions,
            progress = progress,
            isCurrentQuestionAnswered = isCurrentQuestionAnswered,
            userAnswers = userAnswers.toMap()
        )
    }
    
    private fun navigateToNextQuestion() {
        val currentIndex = _state.value.currentQuestionIndex
        val nextIndex = currentIndex + 1
        
        if (nextIndex < questions.size) {
            navigateToQuestion(nextIndex)
        }
    }
    
    private fun navigateToPreviousQuestion() {
        val currentIndex = _state.value.currentQuestionIndex
        val prevIndex = currentIndex - 1
        
        if (prevIndex >= 0) {
            navigateToQuestion(prevIndex)
        }
    }
    
    private fun navigateToQuestion(index: Int) {
        if (index in questions.indices) {
            val question = questions[index]
            loadAnswersForQuestion(question.id)
            
            _state.value = _state.value.copy(
                currentQuestionIndex = index,
                currentQuestion = question,
                isCurrentQuestionAnswered = userAnswers.containsKey(question.id)
            )
        }
    }
    
    private fun finishQuiz() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true)
            
            try {
                val attemptTime = System.currentTimeMillis()
                val quiz = _state.value.quiz ?: return@launch
                
                // Проверяем правильность ответов и сохраняем результаты
                var correctAnswers = 0
                val userAnswersList = mutableListOf<UserAnswer>()
                
                for ((questionId, userAnswer) in userAnswers) {
                    val question = questions.find { it.id == questionId } ?: continue
                    
                    when (question.type) {
                        1, 2, 3 -> { // Single choice, Multiple choice, True/False
                            val correctAnswerIds = quizRepository.getCorrectAnswersByQuestion(questionId)
                                .collectLatest { answers ->
                                    val correctIds = answers.map { it.id }.joinToString(",")
                                    val isCorrect = correctIds == userAnswer.selectedAnswerIds
                                    
                                    if (isCorrect) {
                                        correctAnswers++
                                    }
                                    
                                    userAnswersList.add(userAnswer.copy(
                                        attemptDate = attemptTime,
                                        isCorrect = isCorrect
                                    ))
                                }
                        }
                        4 -> { // Text input
                            // Для текстовых ответов требуется сравнение с правильным ответом
                            // (в этой реализации просто пример)
                            val isCorrect = false // Здесь должна быть логика проверки
                            
                            if (isCorrect) {
                                correctAnswers++
                            }
                            
                            userAnswersList.add(userAnswer.copy(
                                attemptDate = attemptTime,
                                isCorrect = isCorrect
                            ))
                        }
                    }
                }
                
                // Сохраняем ответы пользователя
                quizRepository.addUserAnswers(userAnswersList)
                
                // Вычисляем результат
                val score = if (questions.isNotEmpty()) {
                    (correctAnswers * 100) / questions.size
                } else {
                    0
                }
                
                // Обновляем статус прохождения теста
                val isCompleted = score >= quiz.passingScore
                quizRepository.updateQuizCompletionWithStartTime(
                    quizId = quizId,
                    isCompleted = isCompleted,
                    score = score,
                    attemptDate = attemptTime,
                    startTime = quizStartTime
                )
                
                // Переходим к экрану результатов
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    navigationEvent = QuizSessionNavigationEvent.NavigateToResults(quizId, score, attemptTime)
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    error = "Ошибка при сохранении результатов: ${e.message}"
                )
            }
        }
    }
    
    fun getRemainingTime(): Long {
        val quiz = _state.value.quiz ?: return 0
        
        if (quiz.timeLimit <= 0) {
            return 0 // Неограниченное время
        }
        
        val elapsedTime = System.currentTimeMillis() - quizStartTime
        val totalTime = TimeUnit.MINUTES.toMillis(quiz.timeLimit.toLong())
        
        return (totalTime - elapsedTime).coerceAtLeast(0)
    }
    
    fun getSelectedAnswerIds(questionId: Long): List<Long> {
        val userAnswer = userAnswers[questionId] ?: return emptyList()
        
        return userAnswer.selectedAnswerIds
            .split(",")
            .filter { it.isNotEmpty() }
            .map { it.toLong() }
    }
    
    fun getTextAnswer(questionId: Long): String {
        val userAnswer = userAnswers[questionId] ?: return ""
        return userAnswer.textAnswer
    }
}

data class QuizSessionState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val quiz: Quiz? = null,
    val questions: List<Question> = emptyList(),
    val currentQuestion: Question? = null,
    val currentAnswers: List<Answer> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val totalQuestions: Int = 0,
    val answeredQuestions: Int = 0,
    val progress: Int = 0,
    val isCurrentQuestionAnswered: Boolean = false,
    val timeLimit: Int = 0,
    val startTime: Long = 0,
    val userAnswers: Map<Long, UserAnswer> = emptyMap(),
    val error: String? = null,
    val navigationEvent: QuizSessionNavigationEvent? = null
)

sealed class QuizSessionEvent {
    data class AnswerSelected(val questionId: Long, val answerId: Long, val isSelected: Boolean) : QuizSessionEvent()
    data class TextAnswerChanged(val questionId: Long, val answer: String) : QuizSessionEvent()
    object NextQuestion : QuizSessionEvent()
    object PreviousQuestion : QuizSessionEvent()
    data class NavigateToQuestion(val index: Int) : QuizSessionEvent()
    object FinishQuiz : QuizSessionEvent()
    object NavigationHandled : QuizSessionEvent()
}

sealed class QuizSessionNavigationEvent {
    data class NavigateToResults(val quizId: Long, val score: Int, val timestamp: Long) : QuizSessionNavigationEvent()
} 