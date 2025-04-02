package uz.dckroff.pcap.features.glossary.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import uz.dckroff.pcap.data.model.Term
import uz.dckroff.pcap.databinding.ItemTermBinding

class TermsAdapter(
    private val onTermClick: (Term) -> Unit
) : ListAdapter<Term, TermsAdapter.TermViewHolder>(TermDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TermViewHolder {
        val binding = ItemTermBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TermViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TermViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TermViewHolder(
        private val binding: ItemTermBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onTermClick(getItem(position))
                }
            }
        }

        fun bind(term: Term) {
            binding.apply {
                termTextView.text = term.term
                definitionTextView.text = term.definition
                
                if (term.category != null) {
                    categoryChip.text = term.category
                    categoryChip.visibility = View.VISIBLE
                } else {
                    categoryChip.visibility = View.GONE
                }
                
                val hasLocation = term.sectionId != null || term.chapterId != null
                if (hasLocation) {
                    locationTextView.visibility = View.VISIBLE
                    val locationText = buildLocationText(term)
                    locationTextView.text = locationText
                } else {
                    locationTextView.visibility = View.GONE
                }
            }
        }
        
        private fun buildLocationText(term: Term): String {
            val chapterText = term.chapterId?.let { "Глава $it" } ?: ""
            val sectionText = term.sectionId?.let { "Раздел $it" } ?: ""
            
            return when {
                chapterText.isNotEmpty() && sectionText.isNotEmpty() -> "$chapterText, $sectionText"
                chapterText.isNotEmpty() -> chapterText
                sectionText.isNotEmpty() -> sectionText
                else -> ""
            }
        }
    }

    private class TermDiffCallback : DiffUtil.ItemCallback<Term>() {
        override fun areItemsTheSame(oldItem: Term, newItem: Term): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Term, newItem: Term): Boolean {
            return oldItem == newItem
        }
    }
} 