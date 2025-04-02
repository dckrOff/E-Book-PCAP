package uz.dckroff.pcap.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quizzes")
data class Quiz(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val title: String,
    
    val description: String,
    
    @ColumnInfo(name = "chapter_id")
    val chapterId: Long? = null,
    
    @ColumnInfo(name = "section_id")
    val sectionId: Long? = null,
    
    @ColumnInfo(name = "difficulty")
    val difficulty: Int = 1, // 1 - Easy, 2 - Medium, 3 - Hard
    
    @ColumnInfo(name = "time_limit")
    val timeLimit: Int = 0, // В минутах, 0 - без ограничения
    
    @ColumnInfo(name = "passing_score")
    val passingScore: Int = 60, // Проходной балл в процентах
    
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,
    
    @ColumnInfo(name = "last_score")
    val lastScore: Int = 0, // Последний результат в процентах
    
    @ColumnInfo(name = "last_attempt_date")
    val lastAttemptDate: Long = 0 // Дата последней попытки
) 