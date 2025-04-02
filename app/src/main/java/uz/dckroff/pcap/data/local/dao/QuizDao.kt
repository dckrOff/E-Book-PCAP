package uz.dckroff.pcap.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import uz.dckroff.pcap.data.model.Quiz

@Dao
interface QuizDao {
    @Query("SELECT * FROM quizzes ORDER BY id ASC")
    fun getAllQuizzes(): Flow<List<Quiz>>
    
    @Query("SELECT * FROM quizzes WHERE chapter_id = :chapterId ORDER BY id ASC")
    fun getQuizzesByChapter(chapterId: Long): Flow<List<Quiz>>
    
    @Query("SELECT * FROM quizzes WHERE section_id = :sectionId ORDER BY id ASC")
    fun getQuizzesBySection(sectionId: Long): Flow<List<Quiz>>
    
    @Query("SELECT * FROM quizzes WHERE is_completed = :isCompleted ORDER BY id ASC")
    fun getQuizzesByCompletion(isCompleted: Boolean): Flow<List<Quiz>>
    
    @Query("SELECT * FROM quizzes WHERE difficulty = :difficulty ORDER BY id ASC")
    fun getQuizzesByDifficulty(difficulty: Int): Flow<List<Quiz>>
    
    @Query("SELECT * FROM quizzes WHERE id = :quizId LIMIT 1")
    suspend fun getQuizById(quizId: Long): Quiz?
    
    @Insert
    suspend fun insertQuiz(quiz: Quiz): Long
    
    @Update
    suspend fun updateQuiz(quiz: Quiz)
    
    @Delete
    suspend fun deleteQuiz(quiz: Quiz)
    
    @Query("DELETE FROM quizzes WHERE id = :quizId")
    suspend fun deleteQuizById(quizId: Long)
    
    @Query("UPDATE quizzes SET is_completed = :isCompleted, last_score = :score, last_attempt_date = :attemptDate WHERE id = :quizId")
    suspend fun updateQuizCompletion(quizId: Long, isCompleted: Boolean, score: Int, attemptDate: Long)
} 