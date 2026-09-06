package app.revanced.manager.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.os.ParcelUuid
import androidx.annotation.StringRes
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import app.revanced.manager.R
import app.revanced.manager.data.platform.Filesystem
import app.revanced.manager.data.platform.NetworkInfo
import app.revanced.manager.domain.repository.ChangelogSource
import app.revanced.manager.domain.repository.ChangelogsRepository
import app.revanced.manager.domain.repository.ManagerUpdateRepository
import app.revanced.manager.network.api.ReVancedAPI
import app.revanced.manager.network.dto.ReVancedAsset
import app.revanced.manager.network.dto.ReVancedAssetHistory
import app.revanced.manager.network.service.HttpService
import app.revanced.manager.util.saveableVar
import app.revanced.manager.util.toast
import app.revanced.manager.util.uiSafe
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import ru.solrudev.ackpine.installer.InstallFailure
import ru.solrudev.ackpine.installer.PackageInstaller
import ru.solrudev.ackpine.installer.createSession
import ru.solrudev.ackpine.installer.getSession
import ru.solrudev.ackpine.session.Session
import ru.solrudev.ackpine.session.await
import ru.solrudev.ackpine.session.parameters.Confirmation

class UpdateViewModel(
    private val api: ReVancedAPI,
    private val source: ChangelogSource,
    private val downloadOnScreenEntry: Boolean,
    private val app: Application,
    private val http: HttpService,
    private val networkInfo: NetworkInfo,
    private val fs: Filesystem,
    private val ackpineInstaller: PackageInstaller,
    private val managerUpdateRepository: ManagerUpdateRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    var downloadedSize by mutableLongStateOf(0L)
        private set
    var totalSize by mutableLongStateOf(0L)
        private set

    val downloadProgress by derivedStateOf {
        if (downloadedSize == 0L || totalSize == 0L) return@derivedStateOf 0f
        downloadedSize.toFloat() / totalSize.toFloat()
    }

    var showInternetCheckDialog by mutableStateOf(false)
    var state by mutableStateOf(State.CAN_DOWNLOAD)
        private set

    var backPressedOnce by mutableStateOf(false)
        private set

    var installError by mutableStateOf("")
        private set

    var releaseInfo: ReVancedAsset? by mutableStateOf(null)
        private set

    val changelogs: Flow<PagingData<ReVancedAssetHistory>> = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { ChangelogsRepository(api, managerUpdateRepository, source) }
    ).flow.cachedIn(viewModelScope)

    private val location = fs.uiTempDir.resolve("updater.apk")
    private val temporaryLocation = fs.uiTempDir.resolve("updater.apk.download")
    private var installerSessionId: ParcelUuid? by savedStateHandle.saveableVar()

    init {
        installerSessionId?.uuid?.let { id ->
            viewModelScope.launch {
                uiSafe(app, R.string.install_app_fail, "Failed to install") {
                    state = State.INSTALLING

                    val session = withContext(Dispatchers.IO) {
                        ackpineInstaller.getSession(id)
                    }

                    if (session == null) {
                        installerSessionId = null
                        state = State.CAN_INSTALL
                        return@uiSafe
                    }

                    val result = withContext(Dispatchers.IO) {
                        session.await()
                    }

                    handleInstallResult(result)
                }
            }
        }

        viewModelScope.launch {
            uiSafe(app, R.string.download_manager_failed, "Failed to download Nexora Manager") {
                releaseInfo = managerUpdateRepository.getUpdateOrNull()
                    ?: throw Exception("No update available")

                if (downloadOnScreenEntry) {
                    downloadUpdate()
                } else if (withContext(Dispatchers.IO) {
                        hasVerifiedDownloadedUpdate(releaseInfo!!)
                    }) {
                    state = State.CAN_INSTALL
                } else {
                    state = State.CAN_DOWNLOAD
                }
            }
        }
    }

    fun downloadUpdate(ignoreInternetCheck: Boolean = false) = viewModelScope.launch {
        uiSafe(app, R.string.failed_to_download_update, "Failed to download update") {
            val release = releaseInfo!!

            if (!ignoreInternetCheck && !networkInfo.isUnmetered()) {
                showInternetCheckDialog = true
                return@uiSafe
            }

            state = State.DOWNLOADING

            downloadVerifiedUpdate(release)
            installUpdate()
        }
    }

    private fun hasVerifiedDownloadedUpdate(release: ReVancedAsset): Boolean {
        val expectedSha256 = release.sha256
            ?.takeIf { SHA256_REGEX.matches(it) }
        if (expectedSha256 == null || !location.exists()) {
            location.delete()
            return false
        }

        val matches = location.sha256().equals(expectedSha256, ignoreCase = true)
        if (!matches) location.delete()
        return matches
    }

    private suspend fun downloadVerifiedUpdate(release: ReVancedAsset) = withContext(Dispatchers.IO) {
        val expectedSha256 = release.sha256
            ?.takeIf { SHA256_REGEX.matches(it) }
            ?: error("Nexora Manager update has no valid SHA-256 digest")

        try {
            temporaryLocation.delete()
            http.download(temporaryLocation) {
                url(release.downloadUrl)
                onDownload { bytesSentTotal, contentLength ->
                    downloadedSize = bytesSentTotal
                    contentLength?.let { totalSize = it }
                }
            }

            val actualSha256 = temporaryLocation.sha256()
            check(actualSha256.equals(expectedSha256, ignoreCase = true)) {
                "Downloaded Nexora Manager update failed SHA-256 verification"
            }

            try {
                Files.move(
                    temporaryLocation.toPath(),
                    location.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE,
                )
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(
                    temporaryLocation.toPath(),
                    location.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                )
            }
        } finally {
            temporaryLocation.delete()
        }
    }

    fun installUpdate() = viewModelScope.launch {
        uiSafe(app, R.string.install_app_fail, "Failed to install") {
            val release = releaseInfo ?: error("No Nexora Manager update metadata is available")
            check(withContext(Dispatchers.IO) { hasVerifiedDownloadedUpdate(release) }) {
                "Nexora Manager update failed SHA-256 verification before install"
            }

            state = State.INSTALLING

            val session = withContext(Dispatchers.IO) {
                ackpineInstaller.createSession(Uri.fromFile(location)) {
                    confirmation = Confirmation.IMMEDIATE
                }
            }

            installerSessionId = ParcelUuid(session.id)

            val result = try {
                withContext(Dispatchers.IO) {
                    session.await()
                }
            } finally {
                installerSessionId = null
            }

            handleInstallResult(result)
        }
    }

    private fun handleInstallResult(result: Session.State<InstallFailure>) {
        when (result) {
            is Session.State.Failed<InstallFailure> -> {
                when (val failure = result.failure) {
                    is InstallFailure.Aborted -> state = State.CAN_INSTALL
                    else -> {
                        val msg = failure.message.orEmpty()
                        app.toast(app.getString(R.string.install_app_fail, msg))
                        installError = msg
                        state = State.FAILED
                    }
                }
            }

            Session.State.Succeeded -> {
                app.toast(app.getString(R.string.install_app_success))
                state = State.SUCCESS
            }

            else -> {
                state = State.INSTALLING
            }
        }
    }

    fun cancelUpdate() {
        location.delete()
        temporaryLocation.delete()
    }

    fun onBackPressed() {
        if (!backPressedOnce) {
            backPressedOnce = true
            app.toast(R.string.press_back_again_to_cancel_update)
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (installerSessionId == null) {
            cancelUpdate()
        }
    }

    enum class State(@param:StringRes val title: Int) {
        CAN_DOWNLOAD(R.string.update_available),
        DOWNLOADING(R.string.downloading_manager_update),
        CAN_INSTALL(R.string.ready_to_install_update),
        INSTALLING(R.string.installing_manager_update),
        FAILED(R.string.install_update_manager_failed),
        SUCCESS(R.string.update_completed)
    }
}

private val SHA256_REGEX = Regex("^[0-9a-fA-F]{64}$")

private fun java.io.File.sha256(): String {
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
