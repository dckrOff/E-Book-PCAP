package uz.dckroff.pcap.features.bookmarks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import uz.dckroff.pcap.databinding.FragmentBookmarksBinding
import uz.dckroff.pcap.features.bookmarks.adapter.BookmarksAdapter

@AndroidEntryPoint
class BookmarksFragment : Fragment() {

    private var _binding: FragmentBookmarksBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookmarksViewModel by viewModels()
    private lateinit var adapter: BookmarksAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookmarksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupToolbar()
        observeState()
    }

    private fun setupRecyclerView() {
        adapter = BookmarksAdapter { event ->
            viewModel.onEvent(event)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@BookmarksFragment.adapter
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    updateUI(state)
                }
            }
        }
    }

    private fun updateUI(state: BookmarksState) {
        binding.recyclerView.visibility = if (state.bookmarks.isEmpty()) View.GONE else View.VISIBLE
        binding.emptyView.root.visibility = if (state.bookmarks.isEmpty()) View.VISIBLE else View.GONE
        binding.errorView.root.visibility = if (state.error != null) View.VISIBLE else View.GONE

        state.error?.let { error ->
            binding.errorView.messageTextView.text = error
            binding.errorView.retryButton.setOnClickListener {
                viewModel.onEvent(BookmarksEvent.Retry)
            }
        }

        adapter.submitList(state.bookmarks)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 