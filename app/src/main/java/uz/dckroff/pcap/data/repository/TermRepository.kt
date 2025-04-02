package uz.dckroff.pcap.data.repository

import kotlinx.coroutines.flow.Flow
import uz.dckroff.pcap.data.local.dao.TermDao
import uz.dckroff.pcap.data.model.Term
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TermRepository @Inject constructor(
    private val termDao: TermDao
) {
    fun getAllTerms(): Flow<List<Term>> = termDao.getAllTerms()
    
    fun getTermsByCategory(category: String): Flow<List<Term>> = 
        termDao.getTermsByCategory(category)
    
    fun getTermsBySection(sectionId: Long): Flow<List<Term>> = 
        termDao.getTermsBySection(sectionId)
    
    fun getTermsByChapter(chapterId: Long): Flow<List<Term>> = 
        termDao.getTermsByChapter(chapterId)
    
    fun searchTerms(query: String): Flow<List<Term>> = 
        termDao.searchTerms(query)
    
    fun getAllCategories(): Flow<List<String>> = 
        termDao.getAllCategories()
    
    suspend fun addTerm(term: Term): Long = 
        termDao.insertTerm(term)
    
    suspend fun updateTerm(term: Term) = 
        termDao.updateTerm(term)
    
    suspend fun deleteTerm(term: Term) = 
        termDao.deleteTerm(term)
    
    suspend fun deleteTermById(termId: Long) = 
        termDao.deleteTermById(termId)
    
    suspend fun getTermById(termId: Long): Term? = 
        termDao.getTermById(termId)
} 