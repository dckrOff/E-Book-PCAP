package uz.dckroff.pcap.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import uz.dckroff.pcap.data.model.UserAnswer

@Dao
interface UserAnswerDao {
    @Query("SELECT * FROM user_answers WHERE quiz_id = :quizId ORDER BY id ASC")
    fun getUserAnswersByQuiz(quizId: Long): Flow<List<UserAnswer>>
    
    @Query("SELECT * FROM user_answers WHERE question_id = :questionId ORDER BY attempt_date DESC")
    fun getUserAnswersByQuestion(questionId: Long): Flow<List<UserAnswer>>
    
    @Query("SELECT * FROM user_answers WHERE quiz_id = :quizId AND attempt_date = (SELECT MAX(attempt_date) FROM user_answers WHERE quiz_id = :quizId)")
    fun getLatestUserAnswersForQuiz(quizId: Long): Flow<List<UserAnswer>>
    
    @Query("SELECT COUNT(*) FROM user_answers WHERE quiz_id = :quizId AND is_correct = 1 AND attempt_date = :attemptDate")
    suspend fun getCorrectAnswersCountForAttempt(quizId: Long, attemptDate: Long): Int
    
    @Query("SELECT COUNT(*) FROM user_answers WHERE quiz_id = :quizId AND attempt_date = :attemptDate")
    suspend fun getTotalAnswersCountForAttempt(quizId: Long, attemptDate: Long): Int
    
    @Query("SELECT * FROM user_answers WHERE quiz_id = :quizId AND attempt_date = :attemptDate")
    fun getUserAnswersByQuizAndAttempt(quizId: Long, attemptDate: Long): List<UserAnswer>
    
    @Insert
    suspend fun insertUserAnswer(userAnswer: UserAnswer): Long
    
    @Insert
    suspend fun insertUserAnswers(userAnswers: List<UserAnswer>): List<Long>
    
    @Update
    suspend fun updateUserAnswer(userAnswer: UserAnswer)
    
    @Delete
    suspend fun deleteUserAnswer(userAnswer: UserAnswer)
    
    @Query("DELETE FROM user_answers WHERE quiz_id = :quizId")
    suspend fun deleteUserAnswersForQuiz(quizId: Long)
    
    @Query("DELETE FROM user_answers WHERE quiz_id = :quizId AND attempt_date = :attemptDate")
    suspend fun deleteUserAnswersForAttempt(quizId: Long, attemptDate: Long)
} 