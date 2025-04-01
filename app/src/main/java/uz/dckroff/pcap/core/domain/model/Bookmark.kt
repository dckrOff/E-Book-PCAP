package uz.dckroff.pcap.core.domain.model

import java.util.Date

/**
 * Модель закладки
 */
data class Bookmark(
    val id: Long,
    val contentId: Long,
    val creationDate: Date,
    val notes: String?
) 