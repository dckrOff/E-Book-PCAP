package uz.dckroff.pcap.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import uz.dckroff.pcap.core.domain.model.Section

/**
 * Сущность Room для раздела учебника
 */
@Entity(
    tableName = "sections",
    foreignKeys = [
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("chapterId")]
)
data class SectionEntity(
    @PrimaryKey
    val id: Long,
    val chapterId: Long,
    val title: String,
    val orderIndex: Int
) {
    /**
     * Преобразовать в доменную модель
     */
    fun toDomainModel(): Section {
        return Section(
            id = id,
            chapterId = chapterId,
            title = title,
            orderIndex = orderIndex
        )
    }

    companion object {
        /**
         * Преобразовать из доменной модели
         */
        fun fromDomainModel(section: Section): SectionEntity {
            return SectionEntity(
                id = section.id,
                chapterId = section.chapterId,
                title = section.title,
                orderIndex = section.orderIndex
            )
        }
    }
} 