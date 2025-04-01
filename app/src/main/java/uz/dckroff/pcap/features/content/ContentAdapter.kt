package uz.dckroff.pcap.features.content

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import uz.dckroff.pcap.core.domain.model.ChapterWithSections
import uz.dckroff.pcap.core.domain.model.Section
import uz.dckroff.pcap.databinding.ItemChapterBinding
import uz.dckroff.pcap.databinding.ItemSectionBinding

/**
 * Адаптер для отображения структуры содержания учебника
 */
class ContentAdapter(
    private val onSectionClick: (sectionId: Long, contentId: Long) -> Unit
) : ListAdapter<ChapterWithSections, ContentAdapter.ChapterViewHolder>(ChapterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val binding = ItemChapterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChapterViewHolder(
        private val binding: ItemChapterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val sectionAdapter = SectionAdapter(onSectionClick)
        private var isExpanded = false

        init {
            binding.rvSections.apply {
                layoutManager = LinearLayoutManager(binding.root.context)
                adapter = sectionAdapter
            }

            binding.chapterContainer.setOnClickListener {
                isExpanded = !isExpanded
                toggleSections()
            }
        }

        fun bind(chapter: ChapterWithSections) {
            binding.tvChapterTitle.text = chapter.title
            sectionAdapter.submitList(chapter.sections)
            
            // Обновляем индикатор состояния (стрелка вверх/вниз)
            updateExpandIndicator()
            
            // Если глава должна быть развернута по умолчанию
            // isExpanded = true
            // toggleSections()
        }

        private fun toggleSections() {
            binding.rvSections.visibility = if (isExpanded) View.VISIBLE else View.GONE
            updateExpandIndicator()
        }
        
        private fun updateExpandIndicator() {
            // Здесь можно обновить индикатор (стрелка вверх/вниз)
            // binding.ivExpandIndicator.rotation = if (isExpanded) 180f else 0f
        }
    }

    /**
     * Адаптер для отображения разделов главы
     */
    class SectionAdapter(
        private val onSectionClick: (sectionId: Long, contentId: Long) -> Unit
    ) : ListAdapter<Section, SectionAdapter.SectionViewHolder>(SectionDiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
            val binding = ItemSectionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return SectionViewHolder(binding)
        }

        override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        inner class SectionViewHolder(
            private val binding: ItemSectionBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            init {
                binding.root.setOnClickListener {
                    val section = getItem(bindingAdapterPosition)
                    onSectionClick(section.id, section.contentId)
                }
            }

            fun bind(section: Section) {
                binding.tvSectionTitle.text = section.title
            }
        }

        /**
         * DiffCallback для сравнения разделов
         */
        class SectionDiffCallback : DiffUtil.ItemCallback<Section>() {
            override fun areItemsTheSame(oldItem: Section, newItem: Section): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Section, newItem: Section): Boolean {
                return oldItem == newItem
            }
        }
    }

    /**
     * DiffCallback для сравнения глав
     */
    class ChapterDiffCallback : DiffUtil.ItemCallback<ChapterWithSections>() {
        override fun areItemsTheSame(oldItem: ChapterWithSections, newItem: ChapterWithSections): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChapterWithSections, newItem: ChapterWithSections): Boolean {
            return oldItem == newItem
        }
    }
} 