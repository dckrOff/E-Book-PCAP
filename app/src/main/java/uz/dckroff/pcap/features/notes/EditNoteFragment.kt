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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import uz.dckroff.pcap.databinding.FragmentEditNoteBinding

@AndroidEntryPoint
class EditNoteFragment : Fragment() {

    private var _binding: FragmentEditNoteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditNoteViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupListeners()
        observeState()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                android.R.id.home -> {
                    findNavController().navigateUp()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupListeners() {
        binding.titleEditText.setOnTextChangedListener { text ->
            viewModel.onEvent(EditNoteEvent.TitleChanged(text))
        }

        binding.contentEditText.setOnTextChangedListener { text ->
            viewModel.onEvent(EditNoteEvent.ContentChanged(text))
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                android.R.id.home -> {
                    findNavController().navigateUp()
                    true
                }
                R.id.action_save -> {
                    viewModel.onEvent(EditNoteEvent.SaveNote)
                    true
                }
                else -> false
            }
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

    private fun updateUI(state: EditNoteState) {
        binding.apply {
            titleEditText.setText(state.title)
            contentEditText.setText(state.content)

            titleLayout.error = state.titleError
            contentLayout.error = state.contentError

            if (state.error != null) {
                // TODO: Показать ошибку
            }

            if (state.isSaved) {
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 