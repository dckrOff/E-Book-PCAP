package uz.dckroff.pcap.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import uz.dckroff.pcap.database.entity.SectionEntity

@Dao
interface SectionDao {
    @Query("SELECT * FROM sections WHERE chapterId = :chapterId ORDER BY orderIndex ASC")
    fun getSectionsByChapterId(chapterId: Long): Flow<List<SectionEntity>>
    
    @Query("SELECT * FROM sections WHERE id = :id")
    suspend fun getSectionById(id: Long): SectionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(sections: List<SectionEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: SectionEntity): Long
} 