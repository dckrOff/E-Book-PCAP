package uz.dckroff.pcap.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import uz.dckroff.pcap.core.domain.model.Section
import uz.dckroff.pcap.core.domain.repository.ContentRepository
import javax.inject.Inject

/**
 * UseCase для получения разделов главы
 */
class GetSectionsByChapterUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    operator fun invoke(chapterId: Long): Flow<List<Section>> {
        return contentRepository.getSectionsByChapterId(chapterId)
    }
} 