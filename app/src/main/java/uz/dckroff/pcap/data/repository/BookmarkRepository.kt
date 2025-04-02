package uz.dckroff.pcap.data.repository

import uz.dckroff.pcap.data.local.dao.BookmarkDao
import uz.dckroff.pcap.data.model.Bookmark
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepository @Inject constructor(
    private val bookmarkDao: BookmarkDao
) {
    fun getAllBookmarks() = bookmarkDao.getAllBookmarks()

    suspend fun addBookmark(bookmark: Bookmark) = bookmarkDao.insertBookmark(bookmark)

    suspend fun deleteBookmark(bookmark: Bookmark) = bookmarkDao.deleteBookmark(bookmark)

    suspend fun deleteBookmarkById(bookmarkId: Long) = bookmarkDao.deleteBookmarkById(bookmarkId)

    suspend fun isBookmarked(sectionId: Long) = bookmarkDao.isBookmarked(sectionId)
} 