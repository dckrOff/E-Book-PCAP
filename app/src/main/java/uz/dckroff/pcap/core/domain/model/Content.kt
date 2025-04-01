package uz.dckroff.pcap.core.domain.model

/**
 * Типы контента
 */
enum class ContentType {
    TEXT,
    CODE,
    INTERACTIVE,
    IMAGE
}

/**
 * Модель контента учебника
 */
data class Content(
    val id: Long,
    val sectionId: Long,
    val title: String,
    val contentType: ContentType,
    val contentData: String,
    val orderIndex: Int
) 