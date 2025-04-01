package uz.dckroff.pcap.core.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.dckroff.pcap.core.domain.model.UserNote

/**
 * Интерфейс репозитория для работы с заметками пользователя
 */
interface NotesRepository {
    /**
     * Получить все заметки
     */
    fun getAllNotes(): Flow<List<UserNote>>
    
    /**
     * Получить заметки для контента
     */
    fun getNotesByContentId(contentId: Long): Flow<List<UserNote>>
    
    /**
     * Получить заметку по ID
     */
    suspend fun getNoteById(id: Long): UserNote?
    
    /**
     * Добавить заметку
     */
    suspend fun addNote(note: UserNote): Long
    
    /**
     * Обновить заметку
     */
    suspend fun updateNote(note: UserNote)
    
    /**
     * Удалить заметку
     */
    suspend fun deleteNote(id: Long)
} 