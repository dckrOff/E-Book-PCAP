package uz.dckroff.pcap.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import uz.dckroff.pcap.core.domain.model.Content
import uz.dckroff.pcap.core.domain.repository.ContentRepository
import javax.inject.Inject

/**
 * UseCase для получения контента раздела
 */
class GetContentBySectionUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    operator fun invoke(sectionId: Long): Flow<List<Content>> {
        return contentRepository.getContentBySectionId(sectionId)
    }
} 