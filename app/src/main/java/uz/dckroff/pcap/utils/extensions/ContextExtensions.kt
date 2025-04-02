package uz.dckroff.pcap.utils.extensions

import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import uz.dckroff.pcap.R

/**
 * Показывает диалог подтверждения с заданными заголовком, сообщением
 * и действиями при положительном и отрицательном ответе
 */
fun Context.showConfirmationDialog(
    title: String,
    message: String,
    positiveAction: () -> Unit,
    negativeAction: (() -> Unit)? = null
) {
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(R.string.yes) { dialog, _ ->
            dialog.dismiss()
            positiveAction()
        }
        .setNegativeButton(R.string.no) { dialog, _ ->
            dialog.dismiss()
            negativeAction?.invoke()
        }
        .show()
}

/**
 * Показывает Toast сообщение
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Вспомогательный метод для установки видимости View
 */
fun View.setVisibility(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
} 