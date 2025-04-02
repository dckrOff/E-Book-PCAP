package uz.dckroff.pcap.features.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.dckroff.pcap.data.model.Note
import uz.dckroff.pcap.data.repository.NoteRepository
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NotesState())
    val state: StateFlow<NotesState> = _state.asStateFlow()

    init {
        loadNotes()
    }

    fun onEvent(event: NotesEvent) {
        when (event) {
            is NotesEvent.LoadNotes -> loadNotes()
            is NotesEvent.DeleteNote -> deleteNote(event.note)
            is NotesEvent.NoteClick -> navigateToNote(event.note)
            is NotesEvent.AddNote -> navigateToAddNote()
            is NotesEvent.Retry -> loadNotes()
        }
    }

    private fun loadNotes() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                noteRepository.getAllNotes().collect { notes ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        notes = notes,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Неизвестная ошибка"
                )
            }
        }
    }

    private fun deleteNote(note: Note) {
        viewModelScope.launch {
            try {
                noteRepository.deleteNote(note)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Ошибка при удалении заметки"
                )
            }
        }
    }

    private fun navigateToNote(note: Note) {
        // TODO: Реализовать навигацию к редактированию заметки
    }

    private fun navigateToAddNote() {
        // TODO: Реализовать навигацию к созданию новой заметки
    }
}

data class NotesState(
    val isLoading: Boolean = false,
    val notes: List<Note> = emptyList(),
    val error: String? = null
)

sealed class NotesEvent {
    object LoadNotes : NotesEvent()
    object AddNote : NotesEvent()
    object Retry : NotesEvent()
    data class DeleteNote(val note: Note) : NotesEvent()
    data class NoteClick(val note: Note) : NotesEvent()
} 