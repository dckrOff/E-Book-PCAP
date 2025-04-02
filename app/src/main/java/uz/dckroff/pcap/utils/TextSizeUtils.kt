package uz.dckroff.pcap.utils

import android.content.Context
import android.util.TypedValue
import android.widget.TextView
import uz.dckroff.pcap.data.repository.TextSize

/**
 * Утилита для управления размером текста
 */
object TextSizeUtils {

    /**
     * Применяет размер текста к TextView
     */
    fun applyTextSize(textView: TextView, textSize: TextSize) {
        val sizeInSp = when (textSize) {
            TextSize.SMALL -> 14f
            TextSize.MEDIUM -> 16f
            TextSize.LARGE -> 18f
            TextSize.EXTRA_LARGE -> 20f
        }
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeInSp)
    }

    /**
     * Возвращает множитель для размера текста
     */
    fun getTextSizeMultiplier(textSize: TextSize): Float {
        return when (textSize) {
            TextSize.SMALL -> 0.85f
            TextSize.MEDIUM -> 1.0f
            TextSize.LARGE -> 1.15f
            TextSize.EXTRA_LARGE -> 1.3f
        }
    }
} 