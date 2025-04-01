package uz.dckroff.pcap.core.domain.model

/**
 * Модель раздела учебника
 */
data class Section(
    val id: Long,
    val chapterId: Long,
    val title: String,
    val orderIndex: Int
) 