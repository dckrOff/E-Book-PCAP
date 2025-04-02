package uz.dckroff.pcap.features.quiz

import uz.dckroff.pcap.data.model.Quiz

data class QuizListState(
    val quizzes: List<Quiz> = emptyList(),
    val filteredQuizzes: List<Quiz> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val completionFilter: Boolean? = null,
    val difficultyFilter: Int? = null,
    val navigationEvent: QuizNavigationEvent? = null
)

sealed class QuizNavigationEvent {
    data class NavigateToQuiz(val quizId: Long) : QuizNavigationEvent()
    data class NavigateToResults(val quizId: Long) : QuizNavigationEvent()
} 