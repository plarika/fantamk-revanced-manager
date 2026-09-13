package app.revanced.manager.domain.repository

import app.revanced.manager.network.dto.ProjectAnnouncement
import app.revanced.manager.network.dto.ProjectContributor
import app.revanced.manager.network.dto.ProjectRepository
import app.revanced.manager.network.service.HttpService
import app.revanced.manager.network.utils.getOrThrow
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

class NexoraProjectService(
    private val http: HttpService,
) {
    suspend fun getContributors(): List<ProjectRepository> = withContext(Dispatchers.IO) {
        val contributors = request<List<GitHubContributor>>(CONTRIBUTORS_ENDPOINT)
            .filter { it.type == "User" }
            .map { ProjectContributor(it.login, it.avatarUrl) }

        listOf(ProjectRepository(PROJECT_NAME, REPOSITORY_URL, contributors))
    }

    suspend fun getAnnouncements(): List<ProjectAnnouncement> = withContext(Dispatchers.IO) {
        request<List<ProjectGitHubRelease>>(RELEASES_ENDPOINT)
            .asSequence()
            .filterNot(ProjectGitHubRelease::draft)
            .mapNotNull { release ->
                val publishedAt = release.publishedAt ?: return@mapNotNull null
                ProjectAnnouncement(
                    id = release.id,
                    author = release.author?.login ?: PROJECT_NAME,
                    title = release.name?.takeIf(String::isNotBlank)
                        ?: "$PROJECT_NAME ${release.tagName}",
                    content = release.body.orEmpty().toAnnouncementHtml(),
                    tags = buildList {
                        add("nexora")
                        add("release")
                        if (release.prerelease) add("prerelease")
                    },
                    createdAt = Instant.parse(publishedAt),
                    archivedAt = null,
                    level = 0,
                )
            }
            .toList()
    }

    private suspend inline fun <reified T> request(endpoint: String): T =
        http.request<T> {
            url(endpoint)
            header(HttpHeaders.Accept, GITHUB_JSON_ACCEPT)
            header(HttpHeaders.UserAgent, USER_AGENT)
            header("X-GitHub-Api-Version", GITHUB_API_VERSION)
        }.getOrThrow()

    private fun String.toAnnouncementHtml(): String =
        replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("\n", "<br>")

    private companion object {
        const val PROJECT_NAME = "Nexora Manager"
        const val REPOSITORY_URL = "https://github.com/plarika/nexora-manager"
        const val CONTRIBUTORS_ENDPOINT =
            "https://api.github.com/repos/plarika/nexora-manager/contributors?per_page=100&anon=0"
        const val RELEASES_ENDPOINT =
            "https://api.github.com/repos/plarika/nexora-manager/releases?per_page=30"
        const val USER_AGENT = "Nexora-Manager"
        const val GITHUB_API_VERSION = "2022-11-28"
        const val GITHUB_JSON_ACCEPT = "application/vnd.github+json"
    }
}

@Serializable
private data class GitHubContributor(
    val login: String,
    @SerialName("avatar_url") val avatarUrl: String,
    val type: String,
)

@Serializable
private data class ProjectGitHubRelease(
    val id: Long,
    @SerialName("tag_name") val tagName: String,
    val name: String? = null,
    val body: String? = null,
    @SerialName("published_at") val publishedAt: String? = null,
    val draft: Boolean = false,
    val prerelease: Boolean = false,
    val author: GitHubAuthor? = null,
)

@Serializable
private data class GitHubAuthor(val login: String)
