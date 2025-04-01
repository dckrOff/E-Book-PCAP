package uz.dckroff.pcap.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import uz.dckroff.pcap.database.dao.ChapterDao
import uz.dckroff.pcap.database.dao.ContentDao
import uz.dckroff.pcap.database.dao.SectionDao
import uz.dckroff.pcap.database.entity.ChapterEntity
import uz.dckroff.pcap.database.entity.ContentEntity
import uz.dckroff.pcap.database.entity.SectionEntity
import uz.dckroff.pcap.database.util.DateConverters

/**
 * Основной класс базы данных Room
 */
@Database(
    entities = [
        ChapterEntity::class,
        SectionEntity::class,
        ContentEntity::class
        // Другие сущности будут добавлены позже
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun chapterDao(): ChapterDao
    abstract fun sectionDao(): SectionDao
    abstract fun contentDao(): ContentDao
    // Другие DAO будут добавлены позже

    companion object {
        private const val DATABASE_NAME = "pcap_textbook.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 