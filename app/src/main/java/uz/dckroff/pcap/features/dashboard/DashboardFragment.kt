package uz.dckroff.pcap.features.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import uz.dckroff.pcap.core.ui.base.BaseFragment
import uz.dckroff.pcap.databinding.FragmentDashboardBinding

/**
 * Фрагмент для отображения главного экрана (дашборда) приложения
 */
@AndroidEntryPoint
class DashboardFragment : BaseFragment<FragmentDashboardBinding>() {

    private val viewModel: DashboardViewModel by viewModels()

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDashboardBinding {
        return FragmentDashboardBinding.inflate(inflater, container, false)
    }

    override fun initViews() {
        // Инициализация UI компонентов будет добавлена позже
    }

    override fun setupListeners() {
        // Настройка обработчиков событий будет добавлена позже
    }

    override fun setupObservers() {
        // Здесь будут наблюдатели за данными из ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Временно, для демонстрации
        binding.tvWelcome.text = "Добро пожаловать в учебник ПАКП!"
    }
} 