package uz.dckroff.pcap.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import uz.dckroff.pcap.core.domain.model.Content
import uz.dckroff.pcap.core.domain.model.ContentType

/**
 * Сущность Room для контента учебника
 */
@Entity(
    tableName = "content",
    foreignKeys = [
        ForeignKey(
            entity = SectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sectionId")]
)
data class ContentEntity(
    @PrimaryKey
    val id: Long,
    val sectionId: Long,
    val title: String,
    val contentType: String,  // Храним как строку
    val contentData: String,
    val orderIndex: Int
) {
    /**
     * Преобразовать в доменную модель
     */
    fun toDomainModel(): Content {
        return Content(
            id = id,
            sectionId = sectionId,
            title = title,
            contentType = ContentType.valueOf(contentType),
            contentData = contentData,
            orderIndex = orderIndex
        )
    }

    companion object {
        /**
         * Преобразовать из доменной модели
         */
        fun fromDomainModel(content: Content): ContentEntity {
            return ContentEntity(
                id = content.id,
                sectionId = content.sectionId,
                title = content.title,
                contentType = content.contentType.name,
                contentData = content.contentData,
                orderIndex = content.orderIndex
            )
        }
    }
} 