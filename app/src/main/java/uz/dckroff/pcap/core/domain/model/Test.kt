package uz.dckroff.pcap.core.domain.model

/**
 * Типы вопросов теста
 */
enum class QuestionType {
    SINGLE_CHOICE,      // Выбор одного варианта
    MULTIPLE_CHOICE,    // Выбор нескольких вариантов
    TRUE_FALSE,         // Да/Нет
    TEXT_INPUT          // Ввод текста
}

/**
 * Модель теста
 */
data class Test(
    val id: Long,
    val sectionId: Long,
    val title: String,
    val description: String
)

/**
 * Модель вопроса теста
 */
data class TestQuestion(
    val id: Long,
    val testId: Long,
    val questionText: String,
    val questionType: QuestionType,
    val optionsData: String,  // JSON формат с вариантами ответов
    val correctAnswer: String // Правильный ответ или JSON для нескольких правильных ответов
) 