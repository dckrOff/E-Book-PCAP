package uz.dckroff.pcap.features.content

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import uz.dckroff.pcap.R
import uz.dckroff.pcap.core.domain.model.ChapterWithSections
import uz.dckroff.pcap.core.ui.base.BaseFragment
import uz.dckroff.pcap.databinding.FragmentContentListBinding

/**
 * Фрагмент для отображения списка содержания учебника
 */
@AndroidEntryPoint
class ContentListFragment : BaseFragment<FragmentContentListBinding>() {

    private val viewModel: ContentListViewModel by viewModels()
    private val contentAdapter by lazy { ContentAdapter(::onContentItemClick) }

    override fun getViewBinding(): FragmentContentListBinding {
        return FragmentContentListBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews()
        setupListeners()
        setupObservers()
        
        // Загружаем содержание при создании фрагмента
        viewModel.handleEvent(ContentListEvent.LoadContent)
    }
    
    private fun initViews() {
        binding.rvContent.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = contentAdapter
        }
    }
    
    private fun setupListeners() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.handleEvent(ContentListEvent.LoadContent)
        }
        
        binding.errorView.btnRetry.setOnClickListener {
            viewModel.handleEvent(ContentListEvent.LoadContent)
        }
    }
    
    private fun setupObservers() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            updateUI(state)
        }
        
        viewModel.effect.observe(viewLifecycleOwner) { effect ->
            handleEffect(effect)
        }
    }
    
    private fun updateUI(state: ContentListState) {
        // Отображаем/скрываем индикатор загрузки
        binding.swipeRefreshLayout.isRefreshing = state.isLoading && !binding.rvContent.isVisible
        binding.progressBar.isVisible = state.isLoading && !binding.swipeRefreshLayout.isRefreshing
        
        // Обрабатываем список содержания
        if (state.content.isNotEmpty()) {
            binding.rvContent.isVisible = true
            binding.errorView.root.isVisible = false
            binding.emptyView.isVisible = false
            
            contentAdapter.submitList(state.content)
        } else if (state.error != null) {
            binding.rvContent.isVisible = false
            binding.errorView.root.isVisible = true
            binding.emptyView.isVisible = false
            
            binding.errorView.tvErrorMessage.text = state.error
        } else if (!state.isLoading) {
            binding.rvContent.isVisible = false
            binding.errorView.root.isVisible = false
            binding.emptyView.isVisible = true
        }
    }
    
    private fun handleEffect(effect: ContentListEffect) {
        when (effect) {
            is ContentListEffect.NavigateToContent -> {
                val action = ContentListFragmentDirections.actionContentListFragmentToReadingFragment(effect.contentId)
                findNavController().navigate(action)
            }
        }
    }
    
    private fun onContentItemClick(sectionId: Long, contentId: Long) {
        viewModel.handleEvent(ContentListEvent.ContentItemClicked(sectionId, contentId))
    }
} 