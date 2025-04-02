package uz.dckroff.pcap.features.quiz

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
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
import uz.dckroff.pcap.data.models.Answer
import uz.dckroff.pcap.data.models.Question
import uz.dckroff.pcap.databinding.FragmentQuizSessionBinding
import uz.dckroff.pcap.features.quiz.QuizSessionViewModel.*
import uz.dckroff.pcap.utils.formatTime
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class QuizSessionFragment : Fragment() {

    private var _binding: FragmentQuizSessionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QuizSessionViewModel by viewModels()
    private var timer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
        setupBackPressHandler()
    }

    private fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            showConfirmExitDialog()
        }

        binding.previousButton.setOnClickListener {
            viewModel.onEvent(QuizSessionEvent.NavigateToPreviousQuestion)
        }

        binding.nextButton.setOnClickListener {
            viewModel.onEvent(QuizSessionEvent.NavigateToNextQuestion)
        }

        binding.finishButton.setOnClickListener {
            showConfirmFinishDialog()
        }

        binding.textAnswerEditText.doAfterTextChanged { text ->
            viewModel.onEvent(QuizSessionEvent.TextAnswerChanged(text.toString()))
        }

        binding.errorView.retryButton.setOnClickListener {
            viewModel.onEvent(QuizSessionEvent.RetryQuizLoading)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest { state ->
                    updateUiState(state)
                    handleNavigationEvents(state.navigationEvent)
                }
            }
        }
    }

    private fun updateUiState(state: QuizSessionState) {
        binding.progressBar.isVisible = state.isLoading
        binding.errorView.root.isVisible = state.error != null

        if (state.error != null) {
            binding.errorView.errorMessageTextView.text = state.error
            return
        }

        val quiz = state.quiz
        val currentQuestion = state.currentQuestion
        
        if (quiz != null) {
            binding.quizTitleTextView.text = quiz.title
            
            // Настройка прогресса
            val answeredCount = state.userAnswers.size
            val totalQuestions = state.questions.size
            binding.progressIndicator.max = totalQuestions
            binding.progressIndicator.progress = answeredCount
            binding.progressTextView.text = getString(
                R.string.answered_questions_count,
                answeredCount,
                totalQuestions
            )
            
            // Настройка таймера, если время ограничено
            if (timer == null && quiz.timeLimit > 0) {
                setupTimer(quiz.timeLimit)
            } else if (quiz.timeLimit <= 0) {
                binding.timerLayout.isVisible = false
            }
        }
        
        if (currentQuestion != null) {
            setupQuestion(currentQuestion, state)
            updateNavigationButtons(state)
        }
    }

    private fun setupQuestion(question: Question, state: QuizSessionState) {
        binding.questionNumberTextView.text = getString(
            R.string.question_number,
            state.currentQuestionIndex + 1,
            state.questions.size
        )
        binding.questionTextView.text = question.text
        
        // Очистить все группы ответов и показать только нужную
        binding.singleChoiceGroup.removeAllViews()
        binding.multipleChoiceGroup.removeAllViews()
        binding.singleChoiceGroup.isVisible = false
        binding.multipleChoiceGroup.isVisible = false
        binding.trueFalseGroup.isVisible = false
        binding.textAnswerLayout.isVisible = false
        
        val userAnswer = state.userAnswers[question.id]
        
        when (question.type) {
            Question.TYPE_SINGLE_CHOICE -> setupSingleChoiceQuestion(question, state.answers, userAnswer)
            Question.TYPE_MULTIPLE_CHOICE -> setupMultipleChoiceQuestion(question, state.answers, userAnswer)
            Question.TYPE_TRUE_FALSE -> setupTrueFalseQuestion(question, userAnswer)
            Question.TYPE_TEXT -> setupTextQuestion(question, userAnswer)
        }
    }
    
    private fun setupSingleChoiceQuestion(question: Question, allAnswers: Map<Long, List<Answer>>, userAnswer: String?) {
        binding.singleChoiceGroup.isVisible = true
        val answersForQuestion = allAnswers[question.id] ?: return
        
        val selectedAnswerId = userAnswer?.toLongOrNull()
        
        answersForQuestion.forEach { answer ->
            val radioButton = RadioButton(requireContext())
            radioButton.id = View.generateViewId()
            radioButton.text = answer.text
            radioButton.isChecked = answer.id == selectedAnswerId
            radioButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    viewModel.onEvent(QuizSessionEvent.AnswerSelected(answer.id.toString()))
                }
            }
            binding.singleChoiceGroup.addView(radioButton)
        }
    }
    
    private fun setupMultipleChoiceQuestion(question: Question, allAnswers: Map<Long, List<Answer>>, userAnswer: String?) {
        binding.multipleChoiceGroup.isVisible = true
        val answersForQuestion = allAnswers[question.id] ?: return
        
        val selectedAnswerIds = userAnswer?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList()
        
        answersForQuestion.forEach { answer ->
            val checkBox = CheckBox(requireContext())
            checkBox.id = View.generateViewId()
            checkBox.text = answer.text
            checkBox.isChecked = selectedAnswerIds.contains(answer.id)
            checkBox.setOnCheckedChangeListener { _, _ ->
                val selectedIds = mutableListOf<Long>()
                for (i in 0 until binding.multipleChoiceGroup.childCount) {
                    val child = binding.multipleChoiceGroup[i] as CheckBox
                    if (child.isChecked) {
                        answersForQuestion.getOrNull(i)?.id?.let { selectedIds.add(it) }
                    }
                }
                viewModel.onEvent(QuizSessionEvent.AnswerSelected(selectedIds.joinToString(",")))
            }
            binding.multipleChoiceGroup.addView(checkBox)
        }
    }
    
    private fun setupTrueFalseQuestion(question: Question, userAnswer: String?) {
        binding.trueFalseGroup.isVisible = true
        
        binding.trueRadioButton.isChecked = userAnswer == "true"
        binding.falseRadioButton.isChecked = userAnswer == "false"
        
        binding.trueRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.onEvent(QuizSessionEvent.AnswerSelected("true"))
            }
        }
        
        binding.falseRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.onEvent(QuizSessionEvent.AnswerSelected("false"))
            }
        }
    }
    
    private fun setupTextQuestion(question: Question, userAnswer: String?) {
        binding.textAnswerLayout.isVisible = true
        binding.textAnswerEditText.setText(userAnswer ?: "")
    }
    
    private fun updateNavigationButtons(state: QuizSessionState) {
        binding.previousButton.isEnabled = state.currentQuestionIndex > 0
        
        val isLastQuestion = state.currentQuestionIndex == state.questions.size - 1
        binding.nextButton.isVisible = !isLastQuestion
        binding.finishButton.isVisible = isLastQuestion
    }
    
    private fun setupTimer(timeLimitMinutes: Int) {
        val timeLimitMillis = TimeUnit.MINUTES.toMillis(timeLimitMinutes.toLong())
        
        timer = object : CountDownTimer(timeLimitMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.timerTextView.text = getString(
                    R.string.remaining_time,
                    formatTime(millisUntilFinished)
                )
            }
            
            override fun onFinish() {
                viewModel.onEvent(QuizSessionEvent.FinishQuiz)
            }
        }.start()
    }
    
    private fun handleNavigationEvents(event: NavigationEvent?) {
        when (event) {
            is NavigationEvent.NavigateToResults -> {
                val action = QuizSessionFragmentDirections.actionQuizSessionFragmentToQuizResultsFragment(
                    event.quizId,
                    event.score,
                    event.timestamp
                )
                findNavController().navigate(action)
                viewModel.onEvent(QuizSessionEvent.NavigationHandled)
            }
            is NavigationEvent.NavigateBack -> {
                findNavController().popBackStack()
                viewModel.onEvent(QuizSessionEvent.NavigationHandled)
            }
            null -> { /* Нет события */ }
        }
    }
    
    private fun showConfirmFinishDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.finish_test)
            .setMessage(R.string.confirm_finish_quiz)
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.onEvent(QuizSessionEvent.FinishQuiz)
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }
    
    private fun showConfirmExitDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quiz)
            .setMessage(R.string.confirm_finish_quiz)
            .setPositiveButton(R.string.yes) { _, _ ->
                findNavController().popBackStack()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }
    
    private fun setupBackPressHandler() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showConfirmExitDialog()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        timer = null
        _binding = null
    }
} 