package uz.dckroff.pcap.data.repository

import kotlinx.coroutines.flow.Flow
import uz.dckroff.pcap.data.local.dao.AnswerDao
import uz.dckroff.pcap.data.local.dao.QuestionDao
import uz.dckroff.pcap.data.local.dao.QuizDao
import uz.dckroff.pcap.data.local.dao.UserAnswerDao
import uz.dckroff.pcap.data.model.Answer
import uz.dckroff.pcap.data.model.Question
import uz.dckroff.pcap.data.model.Quiz
import uz.dckroff.pcap.data.model.UserAnswer
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class QuizRepository @Inject constructor(
    private val quizDao: QuizDao,
    private val questionDao: QuestionDao,
    private val answerDao: AnswerDao,
    private val userAnswerDao: UserAnswerDao
) {
    // Quiz operations
    fun getAllQuizzes(): Flow<List<Quiz>> = quizDao.getAllQuizzes()
    
    fun getQuizzesByChapter(chapterId: Long): Flow<List<Quiz>> = 
        quizDao.getQuizzesByChapter(chapterId)
    
    fun getQuizzesBySection(sectionId: Long): Flow<List<Quiz>> = 
        quizDao.getQuizzesBySection(sectionId)
    
    fun getQuizzesByCompletion(isCompleted: Boolean): Flow<List<Quiz>> = 
        quizDao.getQuizzesByCompletion(isCompleted)
    
    fun getQuizzesByDifficulty(difficulty: Int): Flow<List<Quiz>> = 
        quizDao.getQuizzesByDifficulty(difficulty)
    
    suspend fun getQuizById(quizId: Long): Quiz? = 
        quizDao.getQuizById(quizId)
    
    suspend fun addQuiz(quiz: Quiz): Long = 
        quizDao.insertQuiz(quiz)
    
    suspend fun updateQuiz(quiz: Quiz) = 
        quizDao.updateQuiz(quiz)
    
    suspend fun deleteQuiz(quiz: Quiz) = 
        quizDao.deleteQuiz(quiz)
    
    suspend fun deleteQuizById(quizId: Long) = 
        quizDao.deleteQuizById(quizId)
    
    suspend fun updateQuizCompletion(quizId: Long, isCompleted: Boolean, score: Int, attemptDate: Long) = 
        quizDao.updateQuizCompletion(quizId, isCompleted, score, attemptDate)
    
    fun updateQuizCompletionWithStartTime(quizId: Long, isCompleted: Boolean, score: Int, attemptDate: Long, startTime: Long) {
        quizDao.updateQuizCompletionWithStartTime(quizId, isCompleted, score, attemptDate, startTime)
    }
    
    // Question operations
    fun getQuestionsByQuiz(quizId: Long): Flow<List<Question>> = 
        questionDao.getQuestionsByQuiz(quizId)
    
    suspend fun getQuestionById(questionId: Long): Question? = 
        questionDao.getQuestionById(questionId)
    
    suspend fun getQuestionCountForQuiz(quizId: Long): Int = 
        questionDao.getQuestionCountForQuiz(quizId)
    
    fun getQuestionsByType(quizId: Long, type: Int): Flow<List<Question>> = 
        questionDao.getQuestionsByType(quizId, type)
    
    suspend fun addQuestion(question: Question): Long = 
        questionDao.insertQuestion(question)
    
    suspend fun updateQuestion(question: Question) = 
        questionDao.updateQuestion(question)
    
    suspend fun deleteQuestion(question: Question) = 
        questionDao.deleteQuestion(question)
    
    suspend fun deleteQuestionById(questionId: Long) = 
        questionDao.deleteQuestionById(questionId)
    
    suspend fun deleteQuestionsForQuiz(quizId: Long) = 
        questionDao.deleteQuestionsForQuiz(quizId)
    
    // Answer operations
    fun getAnswersByQuestion(questionId: Long): Flow<List<Answer>> = 
        answerDao.getAnswersByQuestion(questionId)
    
    fun getCorrectAnswersByQuestion(questionId: Long): Flow<List<Answer>> = 
        answerDao.getCorrectAnswersByQuestion(questionId)
    
    suspend fun getAnswerById(answerId: Long): Answer? = 
        answerDao.getAnswerById(answerId)
    
    suspend fun addAnswer(answer: Answer): Long = 
        answerDao.insertAnswer(answer)
    
    suspend fun addAnswers(answers: List<Answer>): List<Long> = 
        answerDao.insertAnswers(answers)
    
    suspend fun updateAnswer(answer: Answer) = 
        answerDao.updateAnswer(answer)
    
    suspend fun deleteAnswer(answer: Answer) = 
        answerDao.deleteAnswer(answer)
    
    suspend fun deleteAnswerById(answerId: Long) = 
        answerDao.deleteAnswerById(answerId)
    
    suspend fun deleteAnswersForQuestion(questionId: Long) = 
        answerDao.deleteAnswersForQuestion(questionId)
    
    // UserAnswer operations
    fun getUserAnswersByQuiz(quizId: Long): Flow<List<UserAnswer>> = 
        userAnswerDao.getUserAnswersByQuiz(quizId)
    
    fun getUserAnswersByQuestion(questionId: Long): Flow<List<UserAnswer>> = 
        userAnswerDao.getUserAnswersByQuestion(questionId)
    
    fun getLatestUserAnswersForQuiz(quizId: Long): Flow<List<UserAnswer>> = 
        userAnswerDao.getLatestUserAnswersForQuiz(quizId)
    
    suspend fun getCorrectAnswersCountForAttempt(quizId: Long, attemptDate: Long): Int = 
        userAnswerDao.getCorrectAnswersCountForAttempt(quizId, attemptDate)
    
    suspend fun getTotalAnswersCountForAttempt(quizId: Long, attemptDate: Long): Int = 
        userAnswerDao.getTotalAnswersCountForAttempt(quizId, attemptDate)
    
    suspend fun addUserAnswer(userAnswer: UserAnswer): Long = 
        userAnswerDao.insertUserAnswer(userAnswer)
    
    suspend fun addUserAnswers(userAnswers: List<UserAnswer>): List<Long> = 
        userAnswerDao.insertUserAnswers(userAnswers)
    
    suspend fun updateUserAnswer(userAnswer: UserAnswer) = 
        userAnswerDao.updateUserAnswer(userAnswer)
    
    suspend fun deleteUserAnswer(userAnswer: UserAnswer) = 
        userAnswerDao.deleteUserAnswer(userAnswer)
    
    suspend fun deleteUserAnswersForQuiz(quizId: Long) = 
        userAnswerDao.deleteUserAnswersForQuiz(quizId)
    
    suspend fun deleteUserAnswersForAttempt(quizId: Long, attemptDate: Long) = 
        userAnswerDao.deleteUserAnswersForAttempt(quizId, attemptDate)
    
    // Compute quiz score
    suspend fun calculateQuizScore(quizId: Long, attemptDate: Long): Int {
        val correctCount = getCorrectAnswersCountForAttempt(quizId, attemptDate)
        val totalCount = getTotalAnswersCountForAttempt(quizId, attemptDate)
        
        return if (totalCount > 0) {
            (correctCount * 100) / totalCount
        } else {
            0
        }
    }

    // Методы для работы с ответами пользователя
    fun getUserAnswersByQuiz(quizId: Long): Flow<List<UserAnswer>> {
        return userAnswerDao.getUserAnswersByQuiz(quizId)
    }

    fun getUserAnswersByQuestion(questionId: Long): Flow<List<UserAnswer>> {
        return userAnswerDao.getUserAnswersByQuestion(questionId)
    }

    fun getUserAnswersByQuizAndAttempt(quizId: Long, attemptDate: Long): List<UserAnswer> {
        return userAnswerDao.getUserAnswersByQuizAndAttempt(quizId, attemptDate)
    }

    fun getCorrectAnswersByQuestion(questionId: Long): Flow<List<Answer>> {
        return answerDao.getCorrectAnswersByQuestion(questionId)
    }

    fun getAnswersByIds(answerIds: List<Long>): Flow<List<Answer>> {
        return answerDao.getAnswersByIds(answerIds)
    }

    fun addUserAnswer(userAnswer: UserAnswer) {
        userAnswerDao.insertUserAnswer(userAnswer)
    }

    fun addUserAnswers(userAnswers: List<UserAnswer>) {
        userAnswers.forEach { userAnswer ->
            userAnswerDao.insertUserAnswer(userAnswer)
        }
    }

    fun updateQuizCompletion(quizId: Long, isCompleted: Boolean, score: Int, attemptDate: Long) {
        quizDao.updateQuizCompletion(quizId, isCompleted, score, attemptDate)
    }

    /**
     * Синхронизирует тесты с сервером
     * В случае офлайн-режима работает с локальными данными
     */
    suspend fun syncQuizzes() {
        try {
            // Получаем данные с сервера, если есть соединение
            if (networkUtils.isNetworkAvailable()) {
                val quizzes = remoteDataSource.getQuizzes()
                
                // Сохраняем тесты в базу данных
                quizDao.insertQuizzes(quizzes)
                
                // Для каждого теста получаем вопросы
                quizzes.forEach { quiz ->
                    val questions = remoteDataSource.getQuestions(quiz.id)
                    
                    // Сохраняем вопросы в базу данных
                    questionDao.insertQuestions(questions)
                    
                    // Для каждого вопроса получаем варианты ответов
                    questions.forEach { question ->
                        val answers = remoteDataSource.getAnswers(question.id)
                        
                        // Сохраняем ответы в базу данных
                        answerDao.insertAnswers(answers)
                    }
                }
                
                // Сохраняем данные в кэш
                cacheManager.saveData(CACHE_KEY_QUIZZES_SYNCED, true)
                cacheManager.saveData(CACHE_KEY_QUIZZES_LAST_SYNC, System.currentTimeMillis())
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при синхронизации тестов")
            // Если произошла ошибка, используем локальные данные
        }
    }
    
    companion object {
        private const val CACHE_KEY_QUIZZES_SYNCED = "quizzes_synced"
        private const val CACHE_KEY_QUIZZES_LAST_SYNC = "quizzes_last_sync"
    }
} 