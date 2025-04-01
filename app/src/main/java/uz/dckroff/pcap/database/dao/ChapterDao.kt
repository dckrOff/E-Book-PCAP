package uz.dckroff.pcap.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import uz.dckroff.pcap.database.entity.ChapterEntity

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters ORDER BY orderIndex ASC")
    fun getAllChapters(): Flow<List<ChapterEntity>>
    
    @Query("SELECT * FROM chapters WHERE id = :id")
    suspend fun getChapterById(id: Long): ChapterEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity): Long
}