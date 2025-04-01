package uz.dckroff.pcap.core.domain.model

import java.util.Date

/**
 * Модель прогресса пользователя
 */
data class UserProgress(
    val sectionId: Long,
    val isCompleted: Boolean,
    val completionPercentage: Int,
    val lastAccessDate: Date
) 