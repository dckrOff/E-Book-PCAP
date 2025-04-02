package uz.dckroff.pcap.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = Quiz::class,
            parentColumns = ["id"],
            childColumns = ["quiz_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Question(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "quiz_id")
    val quizId: Long,
    
    val text: String,
    
    @ColumnInfo(name = "question_type")
    val type: Int, // 1 - Single choice, 2 - Multiple choice, 3 - True/False, 4 - Text input
    
    val explanation: String = "", // Объяснение правильного ответа
    
    @ColumnInfo(name = "points")
    val points: Int = 1 // Количество баллов за вопрос
) 