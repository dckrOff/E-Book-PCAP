package uz.dckroff.pcap.core.domain.model

/**
 * Модель термина глоссария
 */
data class GlossaryTerm(
    val id: Long,
    val term: String,
    val definition: String,
    val relatedSectionId: Long?
) 