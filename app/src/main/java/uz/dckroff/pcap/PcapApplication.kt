package uz.dckroff.pcap

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class PcapApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Инициализация Timber для логирования
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
} 