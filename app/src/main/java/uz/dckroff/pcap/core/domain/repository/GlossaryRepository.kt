package uz.dckroff.pcap.core.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.dckroff.pcap.core.domain.model.GlossaryTerm

/**
 * Интерфейс репозитория для работы с глоссарием
 */
interface GlossaryRepository {
    /**
     * Получить все термины
     */
    fun getAllTerms(): Flow<List<GlossaryTerm>>
    
    /**
     * Получить термин по ID
     */
    suspend fun getTermById(id: Long): GlossaryTerm?
    
    /**
     * Поиск терминов
     */
    fun searchTerms(query: String): Flow<List<GlossaryTerm>>
    
    /**
     * Получить термины, связанные с разделом
     */
    fun getTermsForSection(sectionId: Long): Flow<List<GlossaryTerm>>
} 