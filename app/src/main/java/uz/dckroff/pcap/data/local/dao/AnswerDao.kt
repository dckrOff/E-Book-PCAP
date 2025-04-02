package uz.dckroff.pcap.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import uz.dckroff.pcap.data.model.Answer

@Dao
interface AnswerDao {
    @Query("SELECT * FROM answers WHERE question_id = :questionId ORDER BY id ASC")
    fun getAnswersByQuestion(questionId: Long): Flow<List<Answer>>
    
    @Query("SELECT * FROM answers WHERE question_id = :questionId AND is_correct = 1 ORDER BY id ASC")
    fun getCorrectAnswersByQuestion(questionId: Long): Flow<List<Answer>>
    
    @Query("SELECT * FROM answers WHERE id = :answerId LIMIT 1")
    suspend fun getAnswerById(answerId: Long): Answer?
    
    @Insert
    suspend fun insertAnswer(answer: Answer): Long
    
    @Insert
    suspend fun insertAnswers(answers: List<Answer>): List<Long>
    
    @Update
    suspend fun updateAnswer(answer: Answer)
    
    @Delete
    suspend fun deleteAnswer(answer: Answer)
    
    @Query("DELETE FROM answers WHERE id = :answerId")
    suspend fun deleteAnswerById(answerId: Long)
    
    @Query("DELETE FROM answers WHERE question_id = :questionId")
    suspend fun deleteAnswersForQuestion(questionId: Long)
    
    @Query("SELECT * FROM answers WHERE id IN (:answerIds)")
    fun getAnswersByIds(answerIds: List<Long>): Flow<List<Answer>>
} 