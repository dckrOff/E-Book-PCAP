package uz.dckroff.pcap.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uz.dckroff.pcap.R
import uz.dckroff.pcap.utils.NetworkUtils
import uz.dckroff.pcap.utils.extensions.setVisibility

/**
 * Компонент для отображения индикатора оффлайн-режима.
 * Автоматически отображается, когда устройство находится в оффлайн-режиме.
 */
class OfflineIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.offline_indicator, this, true)
    }

    /**
     * Устанавливает LifecycleOwner и NetworkUtils для автоматического отслеживания состояния сети.
     */
    fun setup(lifecycleOwner: LifecycleOwner, networkUtils: NetworkUtils) {
        lifecycleOwner.lifecycleScope.launch {
            networkUtils.networkStatusFlow.collectLatest { isNetworkAvailable ->
                setVisibility(!isNetworkAvailable)
            }
        }
    }

    /**
     * Принудительно устанавливает видимость индикатора.
     */
    fun setOfflineMode(isOffline: Boolean) {
        setVisibility(isOffline)
    }
} 