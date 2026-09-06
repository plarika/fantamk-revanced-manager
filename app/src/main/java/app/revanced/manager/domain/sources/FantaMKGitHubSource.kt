package app.revanced.manager.domain.sources

import app.revanced.manager.network.dto.ReVancedAsset
import app.revanced.manager.network.utils.getOrThrow
import app.revanced.manager.patcher.patch.PatchBundle
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

typealias FantaMKPatchBundle = FantaMKGitHubSource<PatchBundle>

class FantaMKGitHubSource<T>(
    name: String,
    uid: Int,
    versionHash: String?,
    releasedAt: LocalDateTime?,
    error: Throwable?,
    file: File,
    autoUpdate: Boolean,
    loader: Loader<T>
) : RemoteSource<T>(name, uid, versionHash, releasedAt, error, file, ENDPOINT, autoUpdate, loader) {
    override suspend fun getLatestInfo() = withContext(Dispatchers.IO) {
        val release = http.request<GitHubRelease> {
            url(ENDPOINT)
            header(HttpHeaders.Accept, GITHUB_JSON_ACCEPT)
            header(HttpHeaders.UserAgent, USER_AGENT)
            header("X-GitHub-Api-Version", GITHUB_API_VERSION)
        }.getOrThrow()

        val candidates = release.assets.filter { asset ->
            asset.name.startsWith("patches-") && asset.name.endsWith(".rvp")
        }
        require(candidates.size == 1) {
            "Expected exactly one FantaMK .rvp asset, found ${candidates.size}"
        }

        val asset = candidates.single()
        val publishedAt = release.publishedAt
            ?: error("FantaMK GitHub release has no published_at timestamp")
        val sha256 = asset.digest
            ?.removePrefix("sha256:")
            ?.takeIf { it.matches(SHA256_REGEX) }
            ?: error("FantaMK GitHub release has no valid SHA-256 digest")

        ReVancedAsset(
            downloadUrl = asset.browserDownloadUrl,
            createdAt = LocalDateTime.parse(publishedAt.removeSuffix("Z")),
            description = release.body.orEmpty(),
            version = release.tagName,
            signatureDownloadUrl = null,
            sha256 = sha256,
        )
    }

    override fun copy(
        error: Throwable?,
        name: String,
        autoUpdate: Boolean,
        versionHash: String?,
        releasedAt: LocalDateTime?
    ) = FantaMKGitHubSource(
        name,
        uid,
        versionHash,
        releasedAt,
        error,
        file,
        autoUpdate,
        loader
    )

    companion object {
        const val ENDPOINT = "https://api.github.com/repos/plarika/revanced-patches-private/releases/latest"
        const val DISPLAY_NAME = "FantaMK ReVanced Patches"
        private const val USER_AGENT = "FantaMK-ReVanced-Manager"
        private const val GITHUB_API_VERSION = "2022-11-28"
        private const val GITHUB_JSON_ACCEPT = "application/vnd.github+json"
        private val SHA256_REGEX = Regex("^[0-9a-fA-F]{64}$")
    }
}

@Serializable
private data class GitHubRelease(
    @SerialName("tag_name") val tagName: String,
    @SerialName("published_at") val publishedAt: String? = null,
    val body: String? = null,
    val assets: List<GitHubAsset> = emptyList()
)

@Serializable
private data class GitHubAsset(
    val name: String,
    @SerialName("browser_download_url") val browserDownloadUrl: String,
    val digest: String? = null,
)
