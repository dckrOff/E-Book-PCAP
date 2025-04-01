package uz.dckroff.pcap.core.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * Базовый фрагмент, от которого будут наследоваться все фрагменты в приложении
 */
abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    /**
     * Метод для создания ViewBinding
     */
    abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    /**
     * Инициализация UI компонентов
     */
    abstract fun initViews()

    /**
     * Настройка обработчиков событий
     */
    abstract fun setupListeners()

    /**
     * Настройка наблюдателей за данными
     */
    abstract fun setupObservers()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = getViewBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setupListeners()
        setupObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 