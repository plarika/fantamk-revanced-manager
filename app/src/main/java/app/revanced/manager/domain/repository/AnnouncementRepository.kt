package app.revanced.manager.domain.repository

import app.revanced.manager.network.dto.ProjectAnnouncement
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AnnouncementRepository(
    private val projectService: NexoraProjectService
) {
    private val mutex = Mutex()
    private var cachedAnnouncements: List<ProjectAnnouncement>? = null

    suspend fun getAnnouncements(forceRefresh: Boolean = false): List<ProjectAnnouncement>? {
        mutex.withLock {
            if (cachedAnnouncements == null || forceRefresh) {
                cachedAnnouncements = runCatching { projectService.getAnnouncements() }.getOrNull()
            }
            return cachedAnnouncements
        }
    }
}
