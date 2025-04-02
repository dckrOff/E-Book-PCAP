package uz.dckroff.pcap.di

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import uz.dckroff.pcap.core.domain.repository.UserProgressRepository
import uz.dckroff.pcap.data.cache.CacheManager
import uz.dckroff.pcap.data.cache.impl.SharedPrefsCacheManager
import uz.dckroff.pcap.data.repository.UserProgressRepositoryImpl
import uz.dckroff.pcap.database.dao.ChapterDao
import uz.dckroff.pcap.database.dao.SectionDao
import uz.dckroff.pcap.features.settings.data.SettingsRepositoryImpl
import uz.dckroff.pcap.features.settings.domain.SettingsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("pcap_preferences", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(sharedPreferences: SharedPreferences): SettingsRepository {
        return SettingsRepositoryImpl(sharedPreferences)
    }
    
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create()
    }
    
    @Provides
    @Singleton
    fun provideCacheManager(
        @ApplicationContext context: Context,
        gson: Gson
    ): CacheManager {
        return SharedPrefsCacheManager(context, gson)
    }
    
    @Provides
    @Singleton
    fun provideUserProgressRepository(
        cacheManager: CacheManager,
        sectionDao: SectionDao,
        chapterDao: ChapterDao
    ): UserProgressRepository {
        return UserProgressRepositoryImpl(cacheManager, sectionDao, chapterDao)
    }
} 