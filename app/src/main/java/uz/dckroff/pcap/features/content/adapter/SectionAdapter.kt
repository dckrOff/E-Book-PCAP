package uz.dckroff.pcap.features.content.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import uz.dckroff.pcap.databinding.ItemSectionBinding
import uz.dckroff.pcap.features.content.SectionItem

/**
 * Адаптер для отображения списка разделов главы
 */
class SectionAdapter(
    private val onSectionClick: (Long, Long) -> Unit
) : ListAdapter<SectionItem, SectionAdapter.SectionViewHolder>(SectionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val binding = ItemSectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SectionViewHolder(private val binding: ItemSectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(section: SectionItem) {
            // Настройка заголовка раздела
            binding.tvSectionTitle.text = section.title
            
            // Настройка обработчика нажатия на раздел
            binding.root.setOnClickListener {
                // Проверяем, есть ли ID для первого контента
                section.firstContentId?.let { contentId ->
                    onSectionClick(section.id, contentId)
                }
            }
        }
    }

    /**
     * DiffCallback для оптимизации обновлений списка
     */
    class SectionDiffCallback : DiffUtil.ItemCallback<SectionItem>() {
        override fun areItemsTheSame(oldItem: SectionItem, newItem: SectionItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SectionItem, newItem: SectionItem): Boolean {
            return oldItem == newItem
        }
    }
} 