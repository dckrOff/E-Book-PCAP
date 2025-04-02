/**
 * Подсчитывает размер директории в байтах
 */
fun getDirectorySize(directory: File): Long {
    var size: Long = 0
    
    try {
        if (directory.exists()) {
            for (file in directory.listFiles() ?: emptyArray()) {
                size += if (file.isDirectory) {
                    getDirectorySize(file)
                } else {
                    file.length()
                }
            }
        }
    } catch (e: Exception) {
        Timber.e(e, "Error calculating directory size")
    }
    
    return size
}

/**
 * Форматирует размер файла из байтов в человекочитаемый формат
 */
fun formatFileSize(size: Long): String {
    val kb = 1024.0
    val mb = kb * 1024
    val gb = mb * 1024
    
    return when {
        size < kb -> "$size B"
        size < mb -> String.format("%.2f KB", size / kb)
        size < gb -> String.format("%.2f MB", size / mb)
        else -> String.format("%.2f GB", size / gb)
    }
} 