package uz.dckroff.pcap.features.reading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import uz.dckroff.pcap.R
import uz.dckroff.pcap.core.domain.model.Content
import uz.dckroff.pcap.core.domain.model.ContentType
import uz.dckroff.pcap.core.ui.base.BaseFragment
import uz.dckroff.pcap.databinding.FragmentReadingBinding
import uz.dckroff.pcap.ui.components.CodeView
import uz.dckroff.pcap.ui.components.InteractiveContentView
import uz.dckroff.pcap.ui.components.MarkdownTextView
import uz.dckroff.pcap.ui.components.ZoomableImageView
import uz.dckroff.pcap.utils.extensions.showToast
import com.bumptech.glide.Glide
import org.json.JSONObject

/**
 * Фрагмент для отображения контента учебника (чтение)
 */
@AndroidEntryPoint
class ReadingFragment : BaseFragment<FragmentReadingBinding>(), InteractiveContentView.OnInteractionListener {

    private val viewModel: ReadingViewModel by viewModels()
    private val args: ReadingFragmentArgs by navArgs()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentReadingBinding {
        return FragmentReadingBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        observeContent()
        setupBookmarkButton()
        
        // Загрузка контента
        viewModel.loadContent(args.sectionId)
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_add_note -> {
                    navigateToAddNote()
                    true
                }
                R.id.action_share -> {
                    shareContent()
                    true
                }
                else -> false
            }
        }
    }
    
    private fun observeContent() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.contentState.collectLatest { state ->
                    when {
                        state.isLoading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.tvError.visibility = View.GONE
                        }
                        state.error != null -> {
                            binding.progressBar.visibility = View.GONE
                            binding.tvError.visibility = View.VISIBLE
                            binding.tvError.text = state.error
                        }
                        state.section != null -> {
                            binding.progressBar.visibility = View.GONE
                            binding.tvError.visibility = View.GONE
                            
                            // Устанавливаем заголовок
                            binding.toolbar.title = state.section.title
                            binding.tvTitle.text = state.section.title
                            
                            // Очищаем контейнер перед добавлением нового контента
                            binding.contentContainer.removeAllViews()
                            
                            // Добавляем контент
                            state.contentItems.forEach { content ->
                                addContentItem(content)
                            }
                            
                            // Уведомляем ViewModel о том, что контент просмотрен
                            viewModel.markSectionAsRead(args.sectionId)
                        }
                    }
                }
            }
        }
        
        // Наблюдение за состоянием закладки
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isBookmarked.collectLatest { isBookmarked ->
                    updateBookmarkIcon(isBookmarked)
                }
            }
        }
    }
    
    private fun addContentItem(content: Content) {
        val contentView = when (content.contentType) {
            ContentType.TEXT -> createTextView(content)
            ContentType.CODE -> createCodeView(content)
            ContentType.IMAGE -> createImageView(content)
            ContentType.INTERACTIVE -> createInteractiveView(content)
        }
        
        // Добавляем представление в контейнер
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = resources.getDimensionPixelSize(R.dimen.content_margin)
        }
        
        contentView.layoutParams = layoutParams
        binding.contentContainer.addView(contentView)
    }
    
    private fun createTextView(content: Content): View {
        val textView = MarkdownTextView(requireContext())
        textView.setMarkdown(content.contentData)
        return textView
    }
    
    private fun createCodeView(content: Content): View {
        val codeView = CodeView(requireContext())
        
        try {
            // Пытаемся определить язык кода из данных
            val jsonData = JSONObject(content.contentData)
            val code = jsonData.optString("code", content.contentData)
            val language = jsonData.optString("language", "kotlin")
            
            codeView.setText(code, language)
        } catch (e: Exception) {
            // В случае ошибки разбора JSON, используем весь contentData как код
            Timber.e(e, "Ошибка при обработке кода")
            codeView.setText(content.contentData)
        }
        
        return codeView
    }
    
    private fun createImageView(content: Content): View {
        val imageView = ZoomableImageView(requireContext())
        
        try {
            val jsonData = JSONObject(content.contentData)
            val imageUrl = jsonData.optString("url", "")
            val description = jsonData.optString("description", "")
            
            if (imageUrl.isNotEmpty()) {
                // Загрузка изображения
                Glide.with(requireContext())
                    .load(imageUrl)
                    .into(imageView)
                
                // Устанавливаем описание для специальных возможностей
                imageView.contentDescription = description
            } else {
                Timber.e("URL изображения пуст")
                Toast.makeText(requireContext(), "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при обработке изображения")
            Toast.makeText(requireContext(), "Ошибка формата данных изображения", Toast.LENGTH_SHORT).show()
        }
        
        return imageView
    }
    
    private fun createInteractiveView(content: Content): View {
        val interactiveView = InteractiveContentView(requireContext())
        interactiveView.setOnInteractionListener(this)
        
        try {
            val jsonData = JSONObject(content.contentData)
            val contentType = jsonData.optString("type", "diagram")
            val contentData = jsonData.toString()
            
            interactiveView.loadContent(contentType, contentData)
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при обработке интерактивного контента")
            Toast.makeText(requireContext(), "Ошибка формата интерактивного контента", Toast.LENGTH_SHORT).show()
        }
        
        return interactiveView
    }
    
    private fun setupBookmarkButton() {
        binding.fabBookmark.setOnClickListener {
            viewModel.toggleBookmark(args.sectionId)
        }
    }
    
    private fun updateBookmarkIcon(isBookmarked: Boolean) {
        val icon = if (isBookmarked) {
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_bookmark_filled)
        } else {
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_bookmark_border)
        }
        binding.fabBookmark.setImageDrawable(icon)
    }
    
    private fun navigateToAddNote() {
        val action = ReadingFragmentDirections.actionReadingFragmentToEditNoteFragment(
            sectionId = args.sectionId
        )
        findNavController().navigate(action)
    }
    
    private fun shareContent() {
        // Логика для поделиться содержимым
        requireContext().showToast("Функция 'Поделиться' будет доступна в следующем обновлении")
    }

    override fun onInteraction(eventType: String, eventData: String) {
        // Обработка взаимодействия с интерактивным контентом
        Timber.d("Получено взаимодействие: тип=$eventType, данные=$eventData")
        
        // Показываем снекбар с информацией о взаимодействии
        Snackbar.make(
            binding.root,
            "Взаимодействие: $eventType",
            Snackbar.LENGTH_SHORT
        ).show()
    }
} 