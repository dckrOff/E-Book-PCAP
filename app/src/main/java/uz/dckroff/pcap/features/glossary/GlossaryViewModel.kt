package uz.dckroff.pcap.features.glossary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uz.dckroff.pcap.data.model.Term
import uz.dckroff.pcap.data.repository.TermRepository
import javax.inject.Inject

@HiltViewModel
class GlossaryViewModel @Inject constructor(
    private val termRepository: TermRepository
) : ViewModel() {

    private val _state = MutableStateFlow(GlossaryState())
    val state: StateFlow<GlossaryState> = _state.asStateFlow()

    private var currentSearchQuery: String = ""
    private var selectedCategory: String? = null

    init {
        loadCategories()
        loadTerms()
    }

    fun onEvent(event: GlossaryEvent) {
        when (event) {
            is GlossaryEvent.SearchQuery -> {
                currentSearchQuery = event.query
                searchTerms()
            }
            is GlossaryEvent.SelectCategory -> {
                selectedCategory = event.category
                searchTerms()
            }
            is GlossaryEvent.ClearCategory -> {
                selectedCategory = null
                searchTerms()
            }
            is GlossaryEvent.Retry -> {
                loadCategories()
                loadTerms()
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                termRepository.getAllCategories().collect { categories ->
                    _state.value = _state.value.copy(
                        categories = categories
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Ошибка при загрузке категорий"
                )
            }
        }
    }

    private fun loadTerms() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                termRepository.getAllTerms().collect { terms ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        allTerms = terms,
                        filteredTerms = filterTerms(terms, currentSearchQuery, selectedCategory)
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Ошибка при загрузке терминов"
                )
            }
        }
    }

    private fun searchTerms() {
        val terms = _state.value.allTerms
        _state.value = _state.value.copy(
            filteredTerms = filterTerms(terms, currentSearchQuery, selectedCategory)
        )
    }

    private fun filterTerms(
        terms: List<Term>,
        query: String,
        category: String?
    ): List<Term> {
        val trimmedQuery = query.trim().lowercase()
        
        return terms.filter { term ->
            val matchesQuery = trimmedQuery.isEmpty() ||
                    term.term.lowercase().contains(trimmedQuery) ||
                    term.definition.lowercase().contains(trimmedQuery)
            
            val matchesCategory = category == null || term.category == category
            
            matchesQuery && matchesCategory
        }
    }
}

data class GlossaryState(
    val isLoading: Boolean = false,
    val allTerms: List<Term> = emptyList(),
    val filteredTerms: List<Term> = emptyList(),
    val categories: List<String> = emptyList(),
    val error: String? = null
)

sealed class GlossaryEvent {
    data class SearchQuery(val query: String) : GlossaryEvent()
    data class SelectCategory(val category: String) : GlossaryEvent()
    object ClearCategory : GlossaryEvent()
    object Retry : GlossaryEvent()
} 