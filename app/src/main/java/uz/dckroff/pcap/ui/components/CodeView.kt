package uz.dckroff.pcap.ui.components

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import io.github.kbiakov.codeview.highlight.CodeHighlighter
import io.github.kbiakov.codeview.highlight.ColorTheme
import io.github.kbiakov.codeview.highlight.SyntaxColors
import uz.dckroff.pcap.R
import uz.dckroff.pcap.databinding.ViewCodeBinding
import timber.log.Timber

/**
 * Компонент для отображения кода с подсветкой синтаксиса
 */
class CodeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewCodeBinding

    private val highlighter = CodeHighlighter(ColorTheme.DEFAULT)
    private var codeLanguage: String = "kotlin" // По умолчанию Kotlin

    init {
        binding = ViewCodeBinding.inflate(LayoutInflater.from(context), this, true)
        orientation = VERTICAL
        
        // Настройка внешнего вида
        with(binding.tvCode) {
            setTextIsSelectable(true)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTypeface(Typeface.MONOSPACE)
        }

        // Установка фона для кода
        binding.codeContainer.setBackgroundColor(
            ContextCompat.getColor(context, R.color.code_background)
        )
    }

    /**
     * Устанавливает текст кода и выполняет подсветку синтаксиса
     */
    fun setText(codeText: String, language: String = "kotlin") {
        this.codeLanguage = language.lowercase()
        
        try {
            // Подсветка синтаксиса
            val highlightedCode = highlighter.highlight(codeLanguage, codeText)
            binding.tvCode.text = highlightedCode
            
            // Установка языка
            binding.tvLanguage.text = getLanguageDisplay(language)
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при подсветке синтаксиса")
            // В случае ошибки просто устанавливаем текст без подсветки
            binding.tvCode.text = codeText
        }
    }

    /**
     * Получает отображаемое имя языка программирования
     */
    private fun getLanguageDisplay(lang: String): String {
        return when (lang.lowercase()) {
            "kotlin" -> "Kotlin"
            "java" -> "Java"
            "xml" -> "XML"
            "json" -> "JSON"
            "c" -> "C"
            "cpp", "c++" -> "C++"
            "cs", "csharp" -> "C#"
            "js", "javascript" -> "JavaScript"
            "py", "python" -> "Python"
            "html" -> "HTML"
            "css" -> "CSS"
            "bash", "shell" -> "Bash"
            "sql" -> "SQL"
            else -> lang.uppercase()
        }
    }

    /**
     * Устанавливает тему для подсветки синтаксиса
     */
    fun setTheme(isDarkMode: Boolean) {
        val colorTheme = if (isDarkMode) ColorTheme.DARK else ColorTheme.DEFAULT
        highlighter.setColors(SyntaxColors.fromTheme(colorTheme))
        
        // Обновить текст с новыми цветами
        val currentText = binding.tvCode.text.toString()
        setText(currentText, codeLanguage)
    }
} 