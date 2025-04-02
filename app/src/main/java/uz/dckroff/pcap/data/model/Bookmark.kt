package uz.dckroff.pcap.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sectionId: Long,
    val chapterId: Long,
    val title: String,
    val chapterTitle: String,
    val timestamp: Long = System.currentTimeMillis()
) 