package uz.dckroff.pcap.features.settings

import android.content.pm.PackageManager
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uz.dckroff.pcap.R
import uz.dckroff.pcap.data.repository.TextSize
import uz.dckroff.pcap.data.repository.ThemeMode
import uz.dckroff.pcap.databinding.FragmentSettingsBinding
import uz.dckroff.pcap.features.settings.domain.model.TextSize as DomainTextSize
import uz.dckroff.pcap.features.settings.domain.model.ThemeMode as DomainThemeMode

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Настройка темы
        binding.themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val themeMode = when (checkedId) {
                binding.lightThemeRadioButton.id -> DomainThemeMode.LIGHT
                binding.darkThemeRadioButton.id -> DomainThemeMode.DARK
                else -> DomainThemeMode.SYSTEM
            }
            viewModel.onEvent(SettingsEvent.SetTheme(themeMode))
        }

        // Настройка размера текста
        binding.textSizeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val textSize = when (checkedId) {
                binding.smallTextRadioButton.id -> DomainTextSize.SMALL
                binding.largeTextRadioButton.id -> DomainTextSize.LARGE
                binding.extraLargeTextRadioButton.id -> DomainTextSize.EXTRA_LARGE
                else -> DomainTextSize.MEDIUM
            }
            viewModel.onEvent(SettingsEvent.SetTextSize(textSize))
        }

        // Настройка автосохранения
        binding.autoSaveSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onEvent(SettingsEvent.SetAutoSave(isChecked))
        }

        // Настройка уведомлений
        binding.notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onEvent(SettingsEvent.SetNotifications(isChecked))
        }

        // Установка версии приложения
        try {
            val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            val versionName = packageInfo.versionName
            binding.versionTextView.text = getString(R.string.version, versionName)
        } catch (e: PackageManager.NameNotFoundException) {
            binding.versionTextView.text = getString(R.string.version, "—")
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest { state ->
                    updateUI(state)
                }
            }
        }
    }

    private fun updateUI(state: SettingsState) {
        val settings = state.settings
        
        // Установка темы
        val radioButtonId = when (settings.themeMode) {
            DomainThemeMode.LIGHT -> binding.lightThemeRadioButton.id
            DomainThemeMode.DARK -> binding.darkThemeRadioButton.id
            DomainThemeMode.SYSTEM -> binding.systemThemeRadioButton.id
            else -> binding.systemThemeRadioButton.id
        }
        binding.themeRadioGroup.check(radioButtonId)
        
        // Установка размера текста
        val textSizeRadioButtonId = when (settings.textSize) {
            DomainTextSize.SMALL -> binding.smallTextRadioButton.id
            DomainTextSize.LARGE -> binding.largeTextRadioButton.id
            DomainTextSize.EXTRA_LARGE -> binding.extraLargeTextRadioButton.id
            DomainTextSize.MEDIUM -> binding.mediumTextRadioButton.id
            else -> binding.mediumTextRadioButton.id
        }
        binding.textSizeRadioGroup.check(textSizeRadioButtonId)
        
        // Установка автосохранения
        binding.autoSaveSwitch.isChecked = settings.autoSaveEnabled
        
        // Установка уведомлений
        binding.notificationsSwitch.isChecked = settings.notificationsEnabled
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 