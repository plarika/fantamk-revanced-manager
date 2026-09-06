package app.revanced.manager.domain.repository

import app.revanced.manager.BuildConfig
import app.revanced.manager.domain.manager.PreferencesManager
import app.revanced.manager.network.dto.ReVancedAsset
import app.revanced.manager.network.dto.ReVancedAssetHistory
import app.revanced.manager.network.service.HttpService
import app.revanced.manager.network.utils.getOrThrow
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class NexoraManagerReleaseService(
    private val http: HttpService,
    private val prefs: PreferencesManager,
) {
    suspend fun getLatest(): ReVancedAsset = withContext(Dispatchers.IO) {
        val includePrereleases =
            BuildConfig.VERSION_NAME.contains('-') || prefs.useManagerPrereleases.get()
        val release = fetchReleases().firstOrNull { candidate ->
            !candidate.draft && (includePrereleases || !candidate.prerelease)
        } ?: error("No Nexora Manager release is available")

        release.toAsset()
    }

    suspend fun getHistory(): List<ReVancedAssetHistory> = withContext(Dispatchers.IO) {
        val includePrereleases =
            BuildConfig.VERSION_NAME.contains('-') || prefs.useManagerPrereleases.get()

        fetchReleases()
            .asSequence()
            .filter { !it.draft && (includePrereleases || !it.prerelease) }
            .mapNotNull { release ->
                release.publishedAt?.let { publishedAt ->
                    ReVancedAssetHistory(
                        version = release.tagName,
                        createdAt = parseGitHubTimestamp(publishedAt),
                        description = release.body.orEmpty(),
                    )
                }
            }
            .toList()
    }

    private suspend fun fetchReleases(): List<GitHubRelease> =
        http.request<List<GitHubRelease>> {
            url(RELEASES_ENDPOINT)
            header(HttpHeaders.Accept, GITHUB_JSON_ACCEPT)
            header(HttpHeaders.UserAgent, USER_AGENT)
            header("X-GitHub-Api-Version", GITHUB_API_VERSION)
        }.getOrThrow()

    private fun GitHubRelease.toAsset(): ReVancedAsset {
        val published = publishedAt
            ?: error("Nexora Manager release has no published_at timestamp")
        val apkAssets = assets.filter { asset ->
            asset.name.endsWith(".apk", ignoreCase = true)
        }
        require(apkAssets.size == 1) {
            "Expected exactly one Nexora Manager APK asset, found ${apkAssets.size}"
        }

        val asset = apkAssets.single()
        val sha256 = asset.digest
            ?.removePrefix("sha256:")
            ?.takeIf { it.matches(SHA256_REGEX) }
            ?: error("Nexora Manager release has no valid SHA-256 digest")

        return ReVancedAsset(
            downloadUrl = asset.browserDownloadUrl,
            createdAt = parseGitHubTimestamp(published),
            signatureDownloadUrl = null,
            description = body.orEmpty(),
            version = tagName,
            sha256 = sha256,
        )
    }

    private fun parseGitHubTimestamp(value: String) =
        LocalDateTime.parse(value.removeSuffix("Z"))

    private companion object {
        const val RELEASES_ENDPOINT =
            "https://api.github.com/repos/plarika/fantamk-revanced-manager/releases?per_page=30"
        const val USER_AGENT = "Nexora-Manager"
        const val GITHUB_API_VERSION = "2022-11-28"
        const val GITHUB_JSON_ACCEPT = "application/vnd.github+json"
        val SHA256_REGEX = Regex("^[0-9a-fA-F]{64}$")
    }
}

@Serializable
private data class GitHubRelease(
    @SerialName("tag_name") val tagName: String,
    @SerialName("published_at") val publishedAt: String? = null,
    val body: String? = null,
    val draft: Boolean = false,
    val prerelease: Boolean = false,
    val assets: List<GitHubAsset> = emptyList(),
)

@Serializable
private data class GitHubAsset(
    val name: String,
    @SerialName("browser_download_url") val browserDownloadUrl: String,
    val digest: String? = null,
)
