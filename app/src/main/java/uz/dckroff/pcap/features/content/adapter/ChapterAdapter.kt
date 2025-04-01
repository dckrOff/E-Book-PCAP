package uz.dckroff.pcap.features.content.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import uz.dckroff.pcap.databinding.ItemChapterBinding
import uz.dckroff.pcap.features.content.ChapterItem

/**
 * Адаптер для отображения списка глав учебника
 */
class ChapterAdapter(
    private val onChapterClick: (Long) -> Unit,
    private val onSectionClick: (Long, Long) -> Unit
) : ListAdapter<ChapterItem, ChapterAdapter.ChapterViewHolder>(ChapterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val binding = ItemChapterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChapterViewHolder(private val binding: ItemChapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var sectionAdapter: SectionAdapter? = null

        fun bind(chapter: ChapterItem) {
            // Настройка заголовка главы
            binding.tvChapterTitle.text = chapter.title
            
            // Настройка индикатора развернутости
            binding.ivExpandIcon.rotation = if (chapter.isExpanded) 180f else 0f
            
            // Настройка видимости списка разделов
            binding.rvSections.visibility = if (chapter.isExpanded) View.VISIBLE else View.GONE
            
            // Настройка обработчика нажатия на главу
            binding.chapterHeader.setOnClickListener {
                onChapterClick(chapter.id)
            }

            // Настройка адаптера для разделов, если глава развернута
            if (chapter.isExpanded) {
                if (sectionAdapter == null) {
                    sectionAdapter = SectionAdapter { sectionId, contentId ->
                        onSectionClick(sectionId, contentId)
                    }
                    binding.rvSections.apply {
                        layoutManager = LinearLayoutManager(context)
                        adapter = sectionAdapter
                    }
                }
                sectionAdapter?.submitList(chapter.sections)
            }
        }
    }

    /**
     * DiffCallback для оптимизации обновлений списка
     */
    class ChapterDiffCallback : DiffUtil.ItemCallback<ChapterItem>() {
        override fun areItemsTheSame(oldItem: ChapterItem, newItem: ChapterItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChapterItem, newItem: ChapterItem): Boolean {
            return oldItem == newItem
        }
    }
} 