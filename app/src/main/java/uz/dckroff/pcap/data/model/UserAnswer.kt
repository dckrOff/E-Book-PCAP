package uz.dckroff.pcap.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_answers",
    foreignKeys = [
        ForeignKey(
            entity = Question::class,
            parentColumns = ["id"],
            childColumns = ["question_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Quiz::class,
            parentColumns = ["id"],
            childColumns = ["quiz_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserAnswer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "quiz_id")
    val quizId: Long,
    
    @ColumnInfo(name = "question_id")
    val questionId: Long,
    
    @ColumnInfo(name = "selected_answer_ids")
    val selectedAnswerIds: String, // Для хранения нескольких ID в формате "1,2,3"
    
    @ColumnInfo(name = "text_answer")
    val textAnswer: String = "", // Для вопросов с текстовым ответом
    
    @ColumnInfo(name = "is_correct")
    val isCorrect: Boolean = false,
    
    @ColumnInfo(name = "attempt_date")
    val attemptDate: Long = System.currentTimeMillis()
) 