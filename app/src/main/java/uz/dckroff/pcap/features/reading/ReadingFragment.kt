package uz.dckroff.pcap.features.reading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import uz.dckroff.pcap.R
import uz.dckroff.pcap.core.domain.model.ContentType
import uz.dckroff.pcap.core.ui.base.BaseFragment
import uz.dckroff.pcap.databinding.FragmentReadingBinding

/**
 * Фрагмент для отображения контента учебника (чтение)
 */
@AndroidEntryPoint
class ReadingFragment : BaseFragment<FragmentReadingBinding>() {

    private val viewModel: ReadingViewModel by viewModels()
    private val args: ReadingFragmentArgs by navArgs()

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentReadingBinding {
        return FragmentReadingBinding.inflate(inflater, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // Включаем поддержку меню
    }

    override fun initViews() {
        // Инициализация UI компонентов
        binding.btnNextSection.setOnClickListener {
            viewModel.handleEvent(ReadingEvent.NextSection)
        }
        
        binding.btnPreviousSection.setOnClickListener {
            viewModel.handleEvent(ReadingEvent.PreviousSection)
        }
    }

    override fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.handleEvent(ReadingEvent.RefreshContent)
        }

        binding.btnRetry.setOnClickListener {
            binding.errorLayout.visibility = View.GONE
            binding.contentLayout.visibility = View.VISIBLE
            viewModel.handleEvent(ReadingEvent.RefreshContent)
        }
    }

    override fun setupObservers() {
        // Наблюдение за состоянием UI
        lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                state?.let {
                    showLoading(it.isLoading)
                    
                    // Отображаем ошибку, если есть
                    if (it.error != null) {
                        showError(it.error)
                        return@collectLatest
                    }
                    
                    // Отображаем контент
                    it.content?.let { content ->
                        binding.tvContentTitle.text = content.title
                        
                        // В зависимости от типа контента отображаем его по-разному
                        when (content.contentType) {
                            ContentType.TEXT -> {
                                binding.tvContentText.visibility = View.VISIBLE
                                binding.webView.visibility = View.GONE
                                binding.codeView.visibility = View.GONE
                                binding.ivImage.visibility = View.GONE
                                
                                // Отображаем текст с поддержкой HTML форматирования
                                binding.tvContentText.text = HtmlCompat.fromHtml(
                                    content.contentData,
                                    HtmlCompat.FROM_HTML_MODE_COMPACT
                                )
                            }
                            ContentType.CODE -> {
                                binding.tvContentText.visibility = View.GONE
                                binding.webView.visibility = View.GONE
                                binding.codeView.visibility = View.VISIBLE
                                binding.ivImage.visibility = View.GONE
                                
                                // Отображаем код с подсветкой синтаксиса (упрощенно)
                                binding.codeView.text = content.contentData
                            }
                            ContentType.INTERACTIVE -> {
                                binding.tvContentText.visibility = View.GONE
                                binding.webView.visibility = View.VISIBLE
                                binding.codeView.visibility = View.GONE
                                binding.ivImage.visibility = View.GONE
                                
                                // Загружаем интерактивный контент в WebView
                                binding.webView.loadData(
                                    content.contentData,
                                    "text/html",
                                    "UTF-8"
                                )
                            }
                            ContentType.IMAGE -> {
                                binding.tvContentText.visibility = View.GONE
                                binding.webView.visibility = View.GONE
                                binding.codeView.visibility = View.GONE
                                binding.ivImage.visibility = View.VISIBLE
                                
                                // В реальном приложении здесь будет загрузка изображения
                                // Для примера просто показываем заглушку
                                binding.ivImage.setImageResource(R.drawable.ic_launcher_foreground)
                            }
                        }
                    }
                    
                    // Настраиваем кнопки навигации
                    binding.btnPreviousSection.isEnabled = it.hasPreviousSection
                    binding.btnNextSection.isEnabled = it.hasNextSection
                }
            }
        }

        // Наблюдение за эффектами UI (например, навигация)
        lifecycleScope.launch {
            viewModel.effect.collectLatest { effect ->
                when (effect) {
                    is ReadingEffect.NavigateToContent -> {
                        Timber.d("Переход к контенту: ${effect.contentId}")
                        val action = ReadingFragmentDirections.actionReadingFragmentSelf(effect.contentId)
                        findNavController().navigate(action)
                    }
                    is ReadingEffect.AddedBookmark -> {
                        // Показываем сообщение об успешном добавлении закладки
                        Timber.d("Добавлена закладка для контента: ${effect.contentId}")
                    }
                    is ReadingEffect.ShowNote -> {
                        // Показываем диалог для добавления/редактирования заметки
                        Timber.d("Показываем заметку для контента: ${effect.contentId}")
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Загружаем контент по ID из аргументов
        viewModel.handleEvent(ReadingEvent.LoadContent(args.contentId))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_reading, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_bookmark -> {
                viewModel.handleEvent(ReadingEvent.AddBookmark)
                true
            }
            R.id.action_add_note -> {
                viewModel.handleEvent(ReadingEvent.AddNote)
                true
            }
            R.id.action_font_size -> {
                // Здесь можно реализовать изменение размера шрифта
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.swipeRefresh.isRefreshing = isLoading
    }

    private fun showError(errorMessage: String) {
        binding.errorLayout.visibility = View.VISIBLE
        binding.contentLayout.visibility = View.GONE
        binding.tvErrorMessage.text = errorMessage
    }
} 