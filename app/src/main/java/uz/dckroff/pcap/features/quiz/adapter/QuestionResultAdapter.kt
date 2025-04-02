package uz.dckroff.pcap.features.quiz.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import uz.dckroff.pcap.R
import uz.dckroff.pcap.databinding.ItemQuestionResultBinding
import uz.dckroff.pcap.features.quiz.QuestionResult

class QuestionResultAdapter : ListAdapter<QuestionResult, QuestionResultAdapter.QuestionResultViewHolder>(QuestionResultDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionResultViewHolder {
        val binding = ItemQuestionResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuestionResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestionResultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class QuestionResultViewHolder(
        private val binding: ItemQuestionResultBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(questionResult: QuestionResult) {
            val context = binding.root.context

            // Устанавливаем номер вопроса
            binding.questionNumberTextView.text = context.getString(
                R.string.question_number_simple,
                questionResult.questionNumber
            )

            // Текст вопроса
            binding.questionTextView.text = questionResult.questionText

            // Правильный ответ
            binding.correctAnswerTextView.text = questionResult.correctAnswer

            // Ответ пользователя
            binding.yourAnswerTextView.text = questionResult.userAnswer
            
            // Устанавливаем цвет ответа пользователя в зависимости от правильности
            val textColor = if (questionResult.isCorrect) {
                R.color.text_color_easy
            } else {
                R.color.text_color_hard
            }
            binding.yourAnswerTextView.setTextColor(ContextCompat.getColor(context, textColor))

            // Устанавливаем иконку результата
            val iconRes = if (questionResult.isCorrect) {
                R.drawable.ic_correct
            } else {
                R.drawable.ic_incorrect
            }
            binding.resultIconImageView.setImageResource(iconRes)
            binding.resultIconImageView.setColorFilter(
                ContextCompat.getColor(
                    context,
                    if (questionResult.isCorrect) R.color.text_color_easy else R.color.text_color_hard
                )
            )

            // Отображаем объяснение, если оно есть
            if (questionResult.explanation.isNotEmpty()) {
                binding.explanationTextView.text = questionResult.explanation
                binding.explanationTextView.visibility = android.view.View.VISIBLE
                binding.explanationLabelTextView.visibility = android.view.View.VISIBLE
            } else {
                binding.explanationTextView.visibility = android.view.View.GONE
                binding.explanationLabelTextView.visibility = android.view.View.GONE
            }
        }
    }

    private class QuestionResultDiffCallback : DiffUtil.ItemCallback<QuestionResult>() {
        override fun areItemsTheSame(oldItem: QuestionResult, newItem: QuestionResult): Boolean {
            return oldItem.questionId == newItem.questionId
        }

        override fun areContentsTheSame(oldItem: QuestionResult, newItem: QuestionResult): Boolean {
            return oldItem == newItem
        }
    }
} 