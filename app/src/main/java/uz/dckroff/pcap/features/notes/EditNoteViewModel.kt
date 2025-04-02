package uz.dckroff.pcap.features.notes

import androidx.lifecycle.SavedStateHandle
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
class EditNoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(EditNoteState())
    val state: StateFlow<EditNoteState> = _state.asStateFlow()

    private val noteId: Long? = savedStateHandle.get<Long>("noteId")

    init {
        if (noteId != null) {
            loadNote(noteId)
        }
    }

    fun onEvent(event: EditNoteEvent) {
        when (event) {
            is EditNoteEvent.TitleChanged -> {
                _state.value = _state.value.copy(
                    title = event.title,
                    titleError = null
                )
            }
            is EditNoteEvent.ContentChanged -> {
                _state.value = _state.value.copy(
                    content = event.content,
                    contentError = null
                )
            }
            EditNoteEvent.SaveNote -> saveNote()
        }
    }

    private fun loadNote(noteId: Long) {
        viewModelScope.launch {
            try {
                noteRepository.getNoteById(noteId)?.let { note ->
                    _state.value = _state.value.copy(
                        title = note.title,
                        content = note.content,
                        noteId = note.id,
                        sectionId = note.sectionId,
                        chapterId = note.chapterId
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Ошибка при загрузке заметки"
                )
            }
        }
    }

    private fun saveNote() {
        val title = _state.value.title.trim()
        val content = _state.value.content.trim()

        if (title.isEmpty()) {
            _state.value = _state.value.copy(
                titleError = "Введите заголовок заметки"
            )
            return
        }

        if (content.isEmpty()) {
            _state.value = _state.value.copy(
                contentError = "Введите содержание заметки"
            )
            return
        }

        viewModelScope.launch {
            try {
                val note = Note(
                    id = _state.value.noteId ?: 0,
                    title = title,
                    content = content,
                    sectionId = _state.value.sectionId,
                    chapterId = _state.value.chapterId
                )

                if (note.id == 0L) {
                    noteRepository.addNote(note)
                } else {
                    noteRepository.updateNote(note)
                }

                _state.value = _state.value.copy(
                    isSaved = true
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Ошибка при сохранении заметки"
                )
            }
        }
    }
}

data class EditNoteState(
    val title: String = "",
    val content: String = "",
    val noteId: Long? = null,
    val sectionId: Long? = null,
    val chapterId: Long? = null,
    val titleError: String? = null,
    val contentError: String? = null,
    val error: String? = null,
    val isSaved: Boolean = false
)

sealed class EditNoteEvent {
    data class TitleChanged(val title: String) : EditNoteEvent()
    data class ContentChanged(val content: String) : EditNoteEvent()
    object SaveNote : EditNoteEvent()
} 