package uz.dckroff.pcap.features.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import uz.dckroff.pcap.databinding.FragmentNotesBinding
import uz.dckroff.pcap.features.notes.adapter.NotesAdapter

@AndroidEntryPoint
class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotesViewModel by viewModels()
    private lateinit var adapter: NotesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupToolbar()
        setupListeners()
        observeState()
    }

    private fun setupRecyclerView() {
        adapter = NotesAdapter(
            onNoteClick = { note ->
                viewModel.onEvent(NotesEvent.NoteClick(note))
            },
            onDeleteClick = { note ->
                viewModel.onEvent(NotesEvent.DeleteNote(note))
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@NotesFragment.adapter
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupListeners() {
        binding.addNoteFab.setOnClickListener {
            viewModel.onEvent(NotesEvent.AddNote)
        }

        binding.errorView.retryButton.setOnClickListener {
            viewModel.onEvent(NotesEvent.Retry)
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

    private fun updateUI(state: NotesState) {
        binding.apply {
            recyclerView.visibility = if (state.notes.isNotEmpty()) View.VISIBLE else View.GONE
            emptyView.visibility = if (state.notes.isEmpty() && !state.isLoading) View.VISIBLE else View.GONE
            errorView.visibility = if (state.error != null) View.VISIBLE else View.GONE
            progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

            if (state.error != null) {
                errorView.messageTextView.text = state.error
            }

            adapter.submitList(state.notes)

            // Обработка событий навигации
            state.navigationEvent?.let { event ->
                when (event) {
                    is NotesNavigationEvent.NavigateToEditNote -> {
                        val action = NotesFragmentDirections.actionNotesFragmentToEditNoteFragment(event.noteId)
                        findNavController().navigate(action)
                        // Сбрасываем событие навигации, чтобы избежать повторной навигации
                        viewModel.onEvent(NotesEvent.NavigationHandled)
                    }
                    is NotesNavigationEvent.NavigateToAddNote -> {
                        val action = NotesFragmentDirections.actionNotesFragmentToEditNoteFragment(null)
                        findNavController().navigate(action)
                        // Сбрасываем событие навигации
                        viewModel.onEvent(NotesEvent.NavigationHandled)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 