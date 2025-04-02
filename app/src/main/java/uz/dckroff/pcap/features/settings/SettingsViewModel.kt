package uz.dckroff.pcap.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.dckroff.pcap.features.settings.domain.SettingsRepository
import uz.dckroff.pcap.features.settings.domain.model.Settings
import uz.dckroff.pcap.features.settings.domain.model.TextSize
import uz.dckroff.pcap.features.settings.domain.model.ThemeMode
import uz.dckroff.pcap.utils.ThemeUtils
import javax.inject.Inject

/**
 * ViewModel для экрана настроек
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.getSettingsFlow().collect { settings ->
                _state.update { it.copy(settings = settings) }
            }
        }
    }

    /**
     * Обработка событий экрана настроек
     */
    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.SetTheme -> {
                viewModelScope.launch {
                    settingsRepository.setThemeMode(event.themeMode)
                    ThemeUtils.applyTheme(event.themeMode)
                }
            }
            is SettingsEvent.SetTextSize -> {
                viewModelScope.launch {
                    settingsRepository.setTextSize(event.textSize)
                }
            }
            is SettingsEvent.SetAutoSave -> {
                viewModelScope.launch {
                    settingsRepository.setAutoSaveEnabled(event.enabled)
                }
            }
            is SettingsEvent.SetNotifications -> {
                viewModelScope.launch {
                    settingsRepository.setNotificationsEnabled(event.enabled)
                }
            }
        }
    }
}

/**
 * Состояние экрана настроек
 */
data class SettingsState(
    val settings: Settings = Settings()
)

/**
 * События экрана настроек
 */
sealed class SettingsEvent {
    data class SetTheme(val themeMode: ThemeMode) : SettingsEvent()
    data class SetTextSize(val textSize: TextSize) : SettingsEvent()
    data class SetAutoSave(val enabled: Boolean) : SettingsEvent()
    data class SetNotifications(val enabled: Boolean) : SettingsEvent()
} 