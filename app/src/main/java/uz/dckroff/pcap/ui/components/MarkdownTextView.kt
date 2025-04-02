package uz.dckroff.pcap.ui.components

import android.content.Context
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.glide.GlideImagesPlugin
import timber.log.Timber

/**
 * Специализированный TextView для отображения Markdown текста
 * с поддержкой различных расширений Markdown (таблицы, изображения, чек-листы)
 */
class MarkdownTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val markwon: Markwon by lazy {
        Markwon.builder(context)
            .usePlugin(TablePlugin.create(context))
            .usePlugin(TaskListPlugin.create(context))
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(HtmlPlugin.create())
            .usePlugin(GlideImagesPlugin.create(context))
            .build()
    }

    init {
        // Включаем обработку ссылок
        movementMethod = LinkMovementMethod.getInstance()
    }

    /**
     * Устанавливает Markdown текст
     * @param markdown текст в формате Markdown
     */
    fun setMarkdown(markdown: String) {
        try {
            // Преобразуем Markdown в Spanned
            val spanned = markwon.toMarkdown(markdown)
            // Устанавливаем текст
            markwon.setParsedMarkdown(this, spanned)
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при обработке Markdown")
            // В случае ошибки просто устанавливаем сырой текст
            text = markdown
        }
    }
} 