package uz.dckroff.pcap.data.repository

import uz.dckroff.pcap.data.local.dao.NoteDao
import uz.dckroff.pcap.data.model.Note
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) {
    fun getAllNotes() = noteDao.getAllNotes()

    suspend fun addNote(note: Note): Long = noteDao.insertNote(note)

    suspend fun updateNote(note: Note) = noteDao.updateNote(note)

    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)

    suspend fun deleteNoteById(noteId: Long) = noteDao.deleteNoteById(noteId)

    fun getNotesBySection(sectionId: Long) = noteDao.getNotesBySection(sectionId)

    fun getNotesByChapter(chapterId: Long) = noteDao.getNotesByChapter(chapterId)
} 