package uz.dckroff.pcap.features.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import uz.dckroff.pcap.R
import uz.dckroff.pcap.databinding.FragmentQuizListBinding
import uz.dckroff.pcap.features.quiz.adapter.QuizAdapter

@AndroidEntryPoint
class QuizListFragment : Fragment() {

    private var _binding: FragmentQuizListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QuizListViewModel by viewModels()
    private lateinit var adapter: QuizAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupToolbar()
        setupFilters()
        observeState()
    }

    private fun setupRecyclerView() {
        adapter = QuizAdapter(
            onQuizClick = { quiz ->
                // Обработка клика по тесту (для детальной информации)
            },
            onStartClick = { quiz ->
                viewModel.onEvent(QuizListEvent.NavigateToQuiz(quiz.id))
            },
            onResultsClick = { quiz ->
                viewModel.onEvent(QuizListEvent.NavigateToResults(quiz.id))
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@QuizListFragment.adapter
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupFilters() {
        // Настройка фильтра по завершению
        binding.statusChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                when (checkedIds[0]) {
                    R.id.allStatusChip -> viewModel.onEvent(QuizListEvent.FilterByCompletion(CompletionStatus.ALL))
                    R.id.completedChip -> viewModel.onEvent(QuizListEvent.FilterByCompletion(CompletionStatus.COMPLETED))
                    R.id.pendingChip -> viewModel.onEvent(QuizListEvent.FilterByCompletion(CompletionStatus.PENDING))
                }
            }
        }
        
        // Настройка фильтра по сложности
        binding.difficultyChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                when (checkedIds[0]) {
                    R.id.allDifficultyChip -> viewModel.onEvent(QuizListEvent.FilterByDifficulty("all"))
                    R.id.easyChip -> viewModel.onEvent(QuizListEvent.FilterByDifficulty("easy"))
                    R.id.mediumChip -> viewModel.onEvent(QuizListEvent.FilterByDifficulty("medium"))
                    R.id.hardChip -> viewModel.onEvent(QuizListEvent.FilterByDifficulty("hard"))
                }
            }
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

    private fun updateUI(state: QuizListState) {
        binding.progressBar.isVisible = state.isLoading
        binding.errorView.root.isVisible = state.error != null
        binding.emptyView.root.isVisible = !state.isLoading && state.error == null && state.filteredQuizzes.isEmpty()
        binding.recyclerView.isVisible = !state.isLoading && state.error == null && state.filteredQuizzes.isNotEmpty()
        
        if (state.error != null) {
            binding.errorView.errorMessageTextView.text = state.error
        }
        
        adapter.submitList(state.filteredQuizzes)
        
        // Обработка событий навигации
        handleNavigationEvents(state.navigationEvent)
    }

    private fun handleNavigationEvents(event: NavigationEvent?) {
        when (event) {
            is NavigationEvent.NavigateToQuizSession -> {
                val action = QuizListFragmentDirections.actionQuizListFragmentToQuizSessionFragment(event.quizId)
                findNavController().navigate(action)
                viewModel.onEvent(QuizListEvent.NavigationHandled)
            }
            is NavigationEvent.NavigateToQuizResults -> {
                val action = QuizListFragmentDirections.actionQuizListFragmentToQuizResultsFragment(
                    event.quizId,
                    event.score,
                    event.timestamp
                )
                findNavController().navigate(action)
                viewModel.onEvent(QuizListEvent.NavigationHandled)
            }
            null -> { /* Нет события */ }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 