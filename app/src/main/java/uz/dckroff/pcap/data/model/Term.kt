package uz.dckroff.pcap.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "terms")
data class Term(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val term: String,
    
    val definition: String,
    
    @ColumnInfo(name = "section_id")
    val sectionId: Long? = null,
    
    @ColumnInfo(name = "chapter_id")
    val chapterId: Long? = null,
    
    val category: String? = null
) 