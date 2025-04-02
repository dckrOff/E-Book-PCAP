package uz.dckroff.pcap.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import uz.dckroff.pcap.data.model.Note

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY lastModified DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Insert
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: Long)

    @Query("SELECT * FROM notes WHERE sectionId = :sectionId")
    fun getNotesBySection(sectionId: Long): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE chapterId = :chapterId")
    fun getNotesByChapter(chapterId: Long): Flow<List<Note>>
} 