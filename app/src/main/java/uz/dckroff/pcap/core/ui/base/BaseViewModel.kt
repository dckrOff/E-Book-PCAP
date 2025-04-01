package uz.dckroff.pcap.core.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Базовый ViewModel, от которого будут наследоваться все ViewModels в приложении
 */
abstract class BaseViewModel<State : Any, Event : Any, Effect : Any> : ViewModel() {

    /**
     * Текущее состояние UI
     */
    private val _state = MutableStateFlow<State?>(null)
    val state: StateFlow<State?> = _state

    /**
     * Поток UI эффектов (навигация, сообщения и т.д.)
     */
    private val _effect = MutableSharedFlow<Effect>()
    val effect: SharedFlow<Effect> = _effect

    /**
     * Обновить состояние UI
     */
    protected fun setState(newState: State) {
        _state.value = newState
    }

    /**
     * Отправить эффект
     */
    protected fun sendEffect(newEffect: Effect) {
        viewModelScope.launch {
            _effect.emit(newEffect)
        }
    }

    /**
     * Обработать событие от UI
     */
    abstract fun handleEvent(event: Event)
} 