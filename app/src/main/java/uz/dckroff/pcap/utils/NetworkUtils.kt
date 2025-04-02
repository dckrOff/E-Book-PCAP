package uz.dckroff.pcap.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Утилита для проверки доступности сети
 */
class NetworkUtils @Inject constructor(
    private val context: Context
) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _networkStatusFlow = MutableStateFlow(false)
    val networkStatusFlow: StateFlow<Boolean> = _networkStatusFlow.asStateFlow()

    init {
        updateNetworkStatus()
        registerNetworkCallback()
    }

    /**
     * Обновляет текущий статус сети
     */
    private fun updateNetworkStatus() {
        _networkStatusFlow.value = isNetworkAvailable()
    }

    /**
     * Регистрирует приемник для отслеживания изменений в соединении
     */
    private fun registerNetworkCallback() {
        val connectivityReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                updateNetworkStatus()
            }
        }
        
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(connectivityReceiver, intentFilter)
    }

    /**
     * Проверяет, доступно ли сетевое соединение
     * @return true, если есть активное соединение с интернетом
     */
    fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Возвращает Flow, который эмитит состояние сетевого соединения
     * @return Flow<Boolean>, где true - соединение есть, false - нет
     */
    fun observeNetworkState(): Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, callback)
        
        // Отправляем текущее состояние сети
        trySend(isNetworkAvailable())
        
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
} 