package uz.dckroff.pcap.core.domain.model

/**
 * Модель главы учебника
 */
data class Chapter(
    val id: Long,
    val title: String,
    val description: String,
    val orderIndex: Int
) 