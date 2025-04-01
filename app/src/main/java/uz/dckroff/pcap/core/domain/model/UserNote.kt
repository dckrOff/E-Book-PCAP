package uz.dckroff.pcap.core.domain.model

import java.util.Date

/**
 * Модель заметки пользователя
 */
data class UserNote(
    val id: Long,
    val contentId: Long,
    val noteText: String,
    val creationDate: Date,
    val lastModifiedDate: Date
) 