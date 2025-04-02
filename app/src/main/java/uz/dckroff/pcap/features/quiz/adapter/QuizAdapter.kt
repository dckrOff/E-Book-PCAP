package uz.dckroff.pcap.features.quiz.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import uz.dckroff.pcap.R
import uz.dckroff.pcap.data.models.Quiz
import uz.dckroff.pcap.databinding.ItemQuizBinding

class QuizAdapter(
    private val onQuizClicked: (Quiz) -> Unit = {},
    private val onStartClicked: (Long) -> Unit = {},
    private val onResultsClicked: (Long) -> Unit = {}
) : ListAdapter<Quiz, QuizAdapter.QuizViewHolder>(QuizDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val binding = ItemQuizBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuizViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class QuizViewHolder(
        private val binding: ItemQuizBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onQuizClicked(getItem(position))
                }
            }

            binding.startButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onStartClicked(getItem(position).id)
                }
            }

            binding.resultsButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onResultsClicked(getItem(position).id)
                }
            }
        }

        fun bind(quiz: Quiz) {
            binding.quizTitleTextView.text = quiz.title
            binding.quizDescriptionTextView.text = quiz.description

            // Настройка отображения сложности
            val difficultyText = when (quiz.difficulty.lowercase()) {
                "easy" -> binding.root.context.getString(R.string.easy)
                "medium" -> binding.root.context.getString(R.string.medium)
                "hard" -> binding.root.context.getString(R.string.hard)
                else -> quiz.difficulty
            }
            
            binding.difficultyTextView.text = difficultyText
            
            // Установка цвета текста сложности
            val textColor = when (quiz.difficulty.lowercase()) {
                "easy" -> R.color.text_color_easy
                "medium" -> R.color.text_color_medium
                "hard" -> R.color.text_color_hard
                else -> R.color.text_color_medium
            }
            binding.difficultyTextView.setTextColor(binding.root.context.getColor(textColor))

            // Настройка информации о вопросах
            binding.questionsCountTextView.text = binding.root.context.getString(
                R.string.questions_count,
                quiz.questionsCount
            )

            // Отображение времени
            if (quiz.timeLimit > 0) {
                binding.timeLimitTextView.text = binding.root.context.getString(
                    R.string.time_limit,
                    quiz.timeLimit
                )
            } else {
                binding.timeLimitTextView.text = binding.root.context.getString(R.string.no_time_limit)
            }

            // Отображение счета или статуса "не пройден"
            if (quiz.isCompleted) {
                binding.scoreTextView.text = binding.root.context.getString(
                    R.string.score,
                    quiz.score
                )
                binding.startButton.visibility = View.GONE
                binding.resultsButton.visibility = View.VISIBLE
            } else {
                binding.scoreTextView.text = binding.root.context.getString(R.string.not_completed)
                binding.startButton.visibility = View.VISIBLE
                binding.resultsButton.visibility = View.GONE
            }
        }
    }

    private class QuizDiffCallback : DiffUtil.ItemCallback<Quiz>() {
        override fun areItemsTheSame(oldItem: Quiz, newItem: Quiz): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Quiz, newItem: Quiz): Boolean {
            return oldItem == newItem
        }
    }
} 