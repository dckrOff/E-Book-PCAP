package uz.dckroff.pcap.features.dashboard

import dagger.hilt.android.lifecycle.HiltViewModel
import uz.dckroff.pcap.core.domain.usecase.GetChaptersUseCase
import uz.dckroff.pcap.core.ui.base.BaseViewModel
import javax.inject.Inject

/**
 * Состояние UI для DashboardFragment
 */
data class DashboardState(
    val isLoading: Boolean = false,
    val recentChapters: List<DashboardChapterItem> = emptyList(),
    val recommendedChapters: List<DashboardChapterItem> = emptyList(),
    val overallProgress: Int = 0
)

/**
 * События UI для DashboardFragment
 */
sealed class DashboardEvent {
    object LoadData : DashboardEvent()
    data class OpenChapter(val chapterId: Long) : DashboardEvent()
    data class OpenSection(val sectionId: Long) : DashboardEvent()
}

/**
 * Эффекты UI для DashboardFragment
 */
sealed class DashboardEffect {
    data class NavigateToChapter(val chapterId: Long) : DashboardEffect()
    data class NavigateToSection(val sectionId: Long) : DashboardEffect()
    data class ShowError(val message: String) : DashboardEffect()
}

/**
 * Модель данных для отображения главы в списке
 */
data class DashboardChapterItem(
    val id: Long,
    val title: String,
    val description: String,
    val progress: Int
)

/**
 * ViewModel для экрана дашборда
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getChaptersUseCase: GetChaptersUseCase
) : BaseViewModel<DashboardState, DashboardEvent, DashboardEffect>() {

    init {
        setState(DashboardState())
        // Загрузить данные при создании ViewModel можно будет позже
    }

    override fun handleEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.LoadData -> {
                // Логика загрузки данных будет добавлена позже
            }
            is DashboardEvent.OpenChapter -> {
                sendEffect(DashboardEffect.NavigateToChapter(event.chapterId))
            }
            is DashboardEvent.OpenSection -> {
                sendEffect(DashboardEffect.NavigateToSection(event.sectionId))
            }
        }
    }

    // Дополнительные методы будут добавлены по мере необходимости
} 