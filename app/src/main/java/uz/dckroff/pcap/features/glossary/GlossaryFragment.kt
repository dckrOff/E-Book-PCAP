package uz.dckroff.pcap.features.glossary

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import uz.dckroff.pcap.R
import uz.dckroff.pcap.databinding.FragmentGlossaryBinding
import uz.dckroff.pcap.features.glossary.adapter.TermsAdapter

@AndroidEntryPoint
class GlossaryFragment : Fragment() {

    private var _binding: FragmentGlossaryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GlossaryViewModel by viewModels()
    private lateinit var adapter: TermsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGlossaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupToolbar()
        setupSearchView()
        setupChips()
        observeState()
    }

    private fun setupRecyclerView() {
        adapter = TermsAdapter { term ->
            // TODO: Реализовать навигацию к разделу учебника
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@GlossaryFragment.adapter
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupSearchView() {
        binding.searchEditText.apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    viewModel.onEvent(GlossaryEvent.SearchQuery(s.toString()))
                }
            })

            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    viewModel.onEvent(GlossaryEvent.SearchQuery(text.toString()))
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun setupChips() {
        binding.allCategoriesChip.setOnClickListener {
            viewModel.onEvent(GlossaryEvent.ClearCategory)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    updateUI(state)
                }
            }
        }
    }

    private fun updateUI(state: GlossaryState) {
        // Обновляем категории
        updateCategories(state.categories)

        // Обновляем список терминов
        adapter.submitList(state.filteredTerms)

        // Обновляем видимость элементов
        binding.apply {
            progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
            
            val isEmpty = !state.isLoading && state.filteredTerms.isEmpty()
            emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
            
            if (isEmpty) {
                emptyView.messageTextView.text = getString(R.string.no_terms)
            }
            
            recyclerView.visibility = if (!state.isLoading && state.filteredTerms.isNotEmpty()) 
                View.VISIBLE else View.GONE
            
            errorView.visibility = if (state.error != null && !state.isLoading) 
                View.VISIBLE else View.GONE
            
            if (state.error != null) {
                errorView.messageTextView.text = state.error
            }
        }
    }

    private fun updateCategories(categories: List<String>) {
        if (categories.isEmpty() || _binding == null) return

        // Удалить все чипы кроме первого (All Categories)
        val chipGroup = binding.categoryChipGroup
        if (chipGroup.childCount > 1) {
            chipGroup.removeViews(1, chipGroup.childCount - 1)
        }

        // Добавить новые чипы для категорий
        categories.forEach { category ->
            val chip = layoutInflater.inflate(
                R.layout.item_category_chip,
                chipGroup,
                false
            ) as Chip
            chip.text = category
            chip.setOnClickListener {
                viewModel.onEvent(GlossaryEvent.SelectCategory(category))
            }
            chipGroup.addView(chip)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 