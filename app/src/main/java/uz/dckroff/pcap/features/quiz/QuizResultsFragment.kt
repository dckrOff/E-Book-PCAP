package uz.dckroff.pcap.features.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uz.dckroff.pcap.R
import uz.dckroff.pcap.databinding.FragmentQuizResultsBinding
import uz.dckroff.pcap.features.quiz.adapter.QuestionResultAdapter

@AndroidEntryPoint
class QuizResultsFragment : Fragment() {

    private var _binding: FragmentQuizResultsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QuizResultsViewModel by viewModels()
    private lateinit var adapter: QuestionResultAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        adapter = QuestionResultAdapter()
        binding.questionsRecyclerView.adapter = adapter

        binding.retakeQuizButton.setOnClickListener {
            viewModel.onEvent(QuizResultsEvent.RetakeQuiz)
        }

        binding.errorView.retryButton.setOnClickListener {
            viewModel.onEvent(QuizResultsEvent.RetryLoading)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest { state ->
                    updateUiState(state)
                    handleNavigation(state.navigateToQuizSession)
                }
            }
        }
    }

    private fun updateUiState(state: QuizResultsState) {
        binding.progressBar.isVisible = state.isLoading
        binding.errorView.root.isVisible = state.error != null

        if (state.error != null) {
            binding.errorView.errorMessageTextView.text = state.error
            return
        }

        if (state.quiz != null) {
            binding.quizTitleTextView.text = state.quiz.title
            binding.attemptDateTextView.text = getString(R.string.attempt_date, state.attemptDate)
            binding.scoreTextView.text = "${state.score}%"
            binding.correctAnswersTextView.text = getString(
                R.string.correct_answers_count,
                state.correctAnswers,
                state.totalQuestions
            )
            binding.timeSpentTextView.text = getString(R.string.time_spent, state.timeSpent)
        }

        adapter.submitList(state.questionResults)
    }

    private fun handleNavigation(quizId: Long?) {
        if (quizId != null) {
            val action = QuizResultsFragmentDirections.actionQuizResultsFragmentToQuizSessionFragment(quizId)
            findNavController().navigate(action)
            viewModel.onEvent(QuizResultsEvent.NavigationHandled)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 