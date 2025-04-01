package uz.dckroff.pcap.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import uz.dckroff.pcap.core.domain.model.Chapter

/**
 * Сущность Room для главы учебника
 */
@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey
    val id: Long,
    val title: String,
    val description: String,
    val orderIndex: Int
) {
    /**
     * Преобразовать в доменную модель
     */
    fun toDomainModel(): Chapter {
        return Chapter(
            id = id,
            title = title,
            description = description,
            orderIndex = orderIndex
        )
    }

    companion object {
        /**
         * Преобразовать из доменной модели
         */
        fun fromDomainModel(chapter: Chapter): ChapterEntity {
            return ChapterEntity(
                id = chapter.id,
                title = chapter.title,
                description = chapter.description,
                orderIndex = chapter.orderIndex
            )
        }
    }
} 