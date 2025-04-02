package uz.dckroff.pcap.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import uz.dckroff.pcap.data.model.Term

@Dao
interface TermDao {
    @Query("SELECT * FROM terms ORDER BY term ASC")
    fun getAllTerms(): Flow<List<Term>>
    
    @Query("SELECT * FROM terms WHERE category = :category ORDER BY term ASC")
    fun getTermsByCategory(category: String): Flow<List<Term>>
    
    @Query("SELECT * FROM terms WHERE section_id = :sectionId ORDER BY term ASC")
    fun getTermsBySection(sectionId: Long): Flow<List<Term>>
    
    @Query("SELECT * FROM terms WHERE chapter_id = :chapterId ORDER BY term ASC")
    fun getTermsByChapter(chapterId: Long): Flow<List<Term>>
    
    @Query("SELECT * FROM terms WHERE term LIKE '%' || :query || '%' OR definition LIKE '%' || :query || '%' ORDER BY term ASC")
    fun searchTerms(query: String): Flow<List<Term>>
    
    @Query("SELECT DISTINCT category FROM terms WHERE category IS NOT NULL ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>
    
    @Insert
    suspend fun insertTerm(term: Term): Long
    
    @Update
    suspend fun updateTerm(term: Term)
    
    @Delete
    suspend fun deleteTerm(term: Term)
    
    @Query("DELETE FROM terms WHERE id = :termId")
    suspend fun deleteTermById(termId: Long)
    
    @Query("SELECT * FROM terms WHERE id = :termId")
    suspend fun getTermById(termId: Long): Term?
} 