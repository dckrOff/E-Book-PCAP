package uz.dckroff.pcap.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import uz.dckroff.pcap.core.domain.model.Content
import uz.dckroff.pcap.core.domain.repository.ContentRepository
import javax.inject.Inject

/**
 * UseCase для поиска по контенту
 */
class SearchContentUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    operator fun invoke(query: String): Flow<List<Content>> {
        return contentRepository.searchContent(query)
    }
} 