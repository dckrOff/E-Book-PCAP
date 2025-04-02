package uz.dckroff.pcap.ui.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import uz.dckroff.pcap.R
import uz.dckroff.pcap.databinding.FragmentOfflineSettingsBinding
import uz.dckroff.pcap.utils.NetworkUtils
import uz.dckroff.pcap.utils.Utils
import uz.dckroff.pcap.utils.extensions.setVisibility
import uz.dckroff.pcap.utils.extensions.showConfirmationDialog
import javax.inject.Inject

@AndroidEntryPoint
class OfflineSettingsFragment : Fragment() {

    private var _binding: FragmentOfflineSettingsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: OfflineSettingsViewModel by viewModels()
    
    @Inject
    lateinit var networkUtils: NetworkUtils
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOfflineSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupUi()
        observeViewModel()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }
    
    private fun setupUi() {
        // Настраиваем обновление статуса сети
        updateNetworkStatus()
        
        // Настраиваем кнопку синхронизации
        binding.buttonSync.setOnClickListener {
            if (networkUtils.isNetworkAvailable()) {
                viewModel.syncData()
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.offline_mode_enabled,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        
        // Переключатель автоматической синхронизации
        binding.switchAutoSync.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAutoSync(isChecked)
        }
        
        // Переключатель скачивания всего контента
        binding.switchDownloadAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !networkUtils.isNetworkAvailable()) {
                binding.switchDownloadAll.isChecked = false
                Toast.makeText(
                    requireContext(),
                    R.string.offline_mode_enabled,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnCheckedChangeListener
            }
            
            viewModel.setDownloadAllContent(isChecked)
        }
        
        // Кнопка очистки кэша
        binding.buttonClearCache.setOnClickListener {
            requireContext().showConfirmationDialog(
                title = getString(R.string.cache_management),
                message = getString(R.string.confirm_clear_cache),
                positiveAction = { viewModel.clearCache() }
            )
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Наблюдаем за состоянием синхронизации
                launch {
                    viewModel.syncState.collectLatest { state ->
                        handleSyncState(state)
                    }
                }
                
                // Наблюдаем за настройками
                launch {
                    viewModel.settings.collectLatest { settings ->
                        updateSettingsUi(settings)
                    }
                }
                
                // Наблюдаем за размером кэша
                launch {
                    viewModel.cacheSize.collectLatest { size ->
                        binding.textCacheSize.text = getString(R.string.cache_size, size)
                    }
                }
                
                // Наблюдаем за временем последней синхронизации
                launch {
                    viewModel.lastSyncTime.collectLatest { time ->
                        binding.textLastSynced.text = if (time.isNotEmpty()) {
                            getString(R.string.last_synced, time)
                        } else {
                            getString(R.string.never_synced)
                        }
                    }
                }
                
                // Наблюдаем за статусом сети
                launch {
                    networkUtils.networkStatusFlow.collectLatest {
                        updateNetworkStatus()
                    }
                }
            }
        }
    }
    
    private fun handleSyncState(state: SyncState) {
        when (state) {
            is SyncState.Idle -> {
                binding.progressSync.setVisibility(false)
                binding.buttonSync.isEnabled = true
            }
            is SyncState.Syncing -> {
                binding.progressSync.setVisibility(true)
                binding.buttonSync.isEnabled = false
                Toast.makeText(requireContext(), R.string.sync_in_progress, Toast.LENGTH_SHORT).show()
            }
            is SyncState.Success -> {
                binding.progressSync.setVisibility(false)
                binding.buttonSync.isEnabled = true
                Toast.makeText(requireContext(), R.string.sync_completed, Toast.LENGTH_SHORT).show()
                viewModel.updateLastSyncTime()
            }
            is SyncState.Error -> {
                binding.progressSync.setVisibility(false)
                binding.buttonSync.isEnabled = true
                Toast.makeText(requireContext(), R.string.sync_failed, Toast.LENGTH_SHORT).show()
                Timber.e(state.error)
            }
        }
    }
    
    private fun updateSettingsUi(settings: OfflineSettings) {
        binding.switchAutoSync.isChecked = settings.autoSync
        binding.switchDownloadAll.isChecked = settings.downloadAllContent
    }
    
    private fun updateNetworkStatus() {
        val isNetworkAvailable = networkUtils.isNetworkAvailable()
        binding.textNetworkStatus.text = getString(
            if (isNetworkAvailable) R.string.network_status_online else R.string.network_status_offline
        )
        binding.textNetworkStatus.setTextColor(
            resources.getColor(
                if (isNetworkAvailable) R.color.green_500 else R.color.orange_500,
                null
            )
        )
        binding.buttonSync.isEnabled = isNetworkAvailable
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 