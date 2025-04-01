package uz.dckroff.pcap.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import uz.dckroff.pcap.database.entity.ContentEntity

@Dao
interface ContentDao {
    @Query("SELECT * FROM content WHERE sectionId = :sectionId ORDER BY orderIndex ASC")
    fun getContentBySectionId(sectionId: Long): Flow<List<ContentEntity>>
    
    @Query("SELECT * FROM content WHERE id = :id")
    suspend fun getContentById(id: Long): ContentEntity?
    
    @Query("SELECT * FROM content WHERE title LIKE '%' || :query || '%' OR contentData LIKE '%' || :query || '%'")
    fun searchContent(query: String): Flow<List<ContentEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContent(content: List<ContentEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContent(content: ContentEntity): Long
} 