package uz.dckroff.pcap.features.settings.domain.model

/**
 * Класс, представляющий настройки приложения
 */
data class Settings(
    val themeMode: ThemeMode? = ThemeMode.SYSTEM,
    val textSize: TextSize? = TextSize.MEDIUM,
    val autoSaveEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true
)

/**
 * Перечисление для режимов темы
 */
enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

/**
 * Перечисление для размеров текста
 */
enum class TextSize {
    SMALL, MEDIUM, LARGE, EXTRA_LARGE
} 