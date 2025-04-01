package uz.dckroff.pcap.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import uz.dckroff.pcap.core.domain.model.Chapter
import uz.dckroff.pcap.core.domain.repository.ContentRepository
import javax.inject.Inject

/**
 * UseCase для получения списка глав учебника
 */
class GetChaptersUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    operator fun invoke(): Flow<List<Chapter>> {
        return contentRepository.getChapters()
    }
} 