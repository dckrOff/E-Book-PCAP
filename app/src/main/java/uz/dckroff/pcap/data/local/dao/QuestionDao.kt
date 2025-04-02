package uz.dckroff.pcap.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import uz.dckroff.pcap.data.model.Question

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE quiz_id = :quizId ORDER BY id ASC")
    fun getQuestionsByQuiz(quizId: Long): Flow<List<Question>>
    
    @Query("SELECT * FROM questions WHERE id = :questionId LIMIT 1")
    suspend fun getQuestionById(questionId: Long): Question?
    
    @Query("SELECT COUNT(*) FROM questions WHERE quiz_id = :quizId")
    suspend fun getQuestionCountForQuiz(quizId: Long): Int
    
    @Query("SELECT * FROM questions WHERE quiz_id = :quizId AND question_type = :type ORDER BY id ASC")
    fun getQuestionsByType(quizId: Long, type: Int): Flow<List<Question>>
    
    @Insert
    suspend fun insertQuestion(question: Question): Long
    
    @Update
    suspend fun updateQuestion(question: Question)
    
    @Delete
    suspend fun deleteQuestion(question: Question)
    
    @Query("DELETE FROM questions WHERE id = :questionId")
    suspend fun deleteQuestionById(questionId: Long)
    
    @Query("DELETE FROM questions WHERE quiz_id = :quizId")
    suspend fun deleteQuestionsForQuiz(quizId: Long)
} 