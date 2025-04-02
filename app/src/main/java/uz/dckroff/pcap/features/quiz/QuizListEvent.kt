package uz.dckroff.pcap.features.quiz

sealed class QuizListEvent {
    data class FilterByCompletion(val isCompleted: Boolean) : QuizListEvent()
    data class FilterByDifficulty(val difficulty: Int) : QuizListEvent()
    object ClearCompletionFilter : QuizListEvent()
    object ClearDifficultyFilter : QuizListEvent()
    object Retry : QuizListEvent()
    data class NavigateToQuiz(val quizId: Long) : QuizListEvent()
    data class NavigateToResults(val quizId: Long) : QuizListEvent()
    object NavigationHandled : QuizListEvent()
} 