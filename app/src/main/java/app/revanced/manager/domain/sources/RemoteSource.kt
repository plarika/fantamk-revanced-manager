package app.revanced.manager.domain.sources

import app.revanced.manager.data.redux.ActionContext
import app.revanced.manager.network.api.ReVancedAPI
import app.revanced.manager.network.dto.ReVancedAsset
import app.revanced.manager.network.service.HttpService
import app.revanced.manager.network.utils.APIResponse
import app.revanced.manager.network.utils.getOrThrow
import app.revanced.manager.patcher.patch.PatchBundle
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import kotlinx.datetime.LocalDateTime

typealias RemotePatchBundle = RemoteSource<PatchBundle>
typealias JsonPatchBundle = JsonSource<PatchBundle>
typealias APIPatchBundle = APISource<PatchBundle>

sealed class RemoteSource<T>(
    name: String,
    uid: Int,
    protected val versionHash: String?,
    val releasedAt: LocalDateTime?,
    error: Throwable?,
    file: File,
    val endpoint: String,
    val autoUpdate: Boolean,
    loader: Loader<T>
) : Source<T>(name, uid, error, file, loader), KoinComponent {
    data class UpdateResult(val versionHash: String, val releasedAt: LocalDateTime)

    protected val http: HttpService by inject()

    protected abstract suspend fun getLatestInfo(): ReVancedAsset
    abstract fun copy(
        error: Throwable? = this.error,
        name: String = this.name,
        autoUpdate: Boolean = this.autoUpdate,
        versionHash: String? = this.versionHash,
        releasedAt: LocalDateTime? = this.releasedAt
    ): RemoteSource<T>

    override fun copy(error: Throwable?, name: String): RemoteSource<T> =
        copy(error, name, this.autoUpdate, this.versionHash, this.releasedAt)

    protected open fun HttpRequestBuilder.configureDownload(info: ReVancedAsset) {
        url(info.downloadUrl)
    }

    private suspend fun download(info: ReVancedAsset) = withContext(Dispatchers.IO) {
        val temporary = File(file.parentFile, "${file.name}.download")
        try {
            temporary.outputStream().use {
                http.streamTo(it) { configureDownload(info) }
            }

            info.sha256?.let { expected ->
                check(temporary.sha256().equals(expected, ignoreCase = true)) {
                    "Downloaded patch bundle failed SHA-256 verification"
                }
            }

            file.setWritable(true, true)
            try {
                try {
                    Files.move(
                        temporary.toPath(), file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                    )
                } catch (_: AtomicMoveNotSupportedException) {
                    Files.move(
                        temporary.toPath(), file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                    )
                }
            } finally {
                file.setReadOnly()
            }
        } finally {
            temporary.delete()
        }

        UpdateResult(info.version, info.createdAt)
    }

    /**
     * Downloads the latest version regardless if there is a new update available.
     */
    suspend fun ActionContext.downloadLatest() = download(getLatestInfo())
    suspend fun ActionContext.getUpdateInfo() =
        getLatestInfo().takeUnless { hasInstalled() && it.version == versionHash }

    suspend fun ActionContext.update(): UpdateResult? = withContext(Dispatchers.IO) {
        getUpdateInfo()?.let { download(it) }
    }

    companion object {
        const val updateFailMsg = "Failed to update"
    }
}

class JsonSource<T>(
    name: String,
    uid: Int,
    versionHash: String?,
    releasedAt: LocalDateTime?,
    error: Throwable?,
    file: File,
    endpoint: String,
    autoUpdate: Boolean,
    loader: Loader<T>
) : RemoteSource<T>(name, uid, versionHash, releasedAt, error, file, endpoint, autoUpdate, loader) {
    override suspend fun getLatestInfo() = withContext(Dispatchers.IO) {
        http.request<ReVancedAsset> {
            url(endpoint)
        }.getOrThrow()
    }

    override fun copy(
        error: Throwable?,
        name: String,
        autoUpdate: Boolean,
        versionHash: String?,
        releasedAt: LocalDateTime?
    ) = JsonSource(
        name,
        uid,
        versionHash,
        releasedAt,
        error,
        file,
        endpoint,
        autoUpdate,
        loader
    )
}


private fun File.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    inputStream().buffered().use { input ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            digest.update(buffer, 0, count)
        }
    }
    return digest.digest().joinToString("") { byte ->
        "%02x".format(byte.toInt() and 0xff)
    }
}
class APISource<T>(
    name: String,
    uid: Int,
    versionHash: String?,
    releasedAt: LocalDateTime?,
    error: Throwable?,
    file: File,
    endpoint: String,
    autoUpdate: Boolean,
    loader: Loader<T>,
    private val getUpdate: suspend ReVancedAPI.() -> APIResponse<ReVancedAsset>
) : RemoteSource<T>(name, uid, versionHash, releasedAt, error, file, endpoint, autoUpdate, loader) {
    private val api: ReVancedAPI by inject()

    override suspend fun getLatestInfo() = api.getUpdate().getOrThrow()
    override fun copy(
        error: Throwable?,
        name: String,
        autoUpdate: Boolean,
        versionHash: String?,
        releasedAt: LocalDateTime?
    ) = APISource(
        name,
        uid,
        versionHash,
        releasedAt,
        error,
        file,
        endpoint,
        autoUpdate,
        loader,
        getUpdate
    )
}