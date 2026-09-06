package app.revanced.manager.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.revanced.manager.domain.manager.FantaMKCredentialStore
import app.revanced.manager.domain.repository.PatchBundleRepository
import app.revanced.manager.domain.sources.FantaMKGitHubSource
import app.revanced.manager.domain.sources.RemoteSource
import app.revanced.manager.patcher.patch.PatchBundle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FantaMKSettingsViewModel(
    private val credentials: FantaMKCredentialStore,
    private val patchBundleRepository: PatchBundleRepository,
) : ViewModel() {
    var isConfigured by mutableStateOf(credentials.hasToken())
        private set
    var isBusy by mutableStateOf(false)
        private set
    var status by mutableStateOf<String?>(null)
        private set
    var installedVersion by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch { refreshInstalledVersion() }
    }

    fun saveToken(token: String) = viewModelScope.launch {
        if (token.isBlank()) {
            status = "Token cannot be empty"
            return@launch
        }

        val previousToken = credentials.getToken()
        isBusy = true
        status = null

        try {
            credentials.saveToken(token)
            patchBundleRepository.reload()
            val existing = findSource()
            if (existing == null) {
                patchBundleRepository.createRemote(FantaMKGitHubSource.ENDPOINT, autoUpdate = true)
                // createRemote persists the source first. Reload so it becomes an active
                // source before forcing the first authenticated download.
                patchBundleRepository.reload()
            }

            val activeSource = findSource()
                ?: error("FantaMK source was not created")
            patchBundleRepository.run {
                activeSource.setAutoUpdate(true)
                update(activeSource, force = true)
            }

            validateLoadedSource()
            isConfigured = true
            status = "FantaMK source connected"
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            restoreCredential(previousToken)
            status = "Connection failed: ${e.message ?: e::class.simpleName}"
        } finally {
            refreshInstalledVersion()
            isBusy = false
        }
    }

    fun refresh() = viewModelScope.launch {
        val source = findSource()
        if (source == null) {
            status = "FantaMK source is not configured"
            return@launch
        }

        isBusy = true
        status = null
        try {
            patchBundleRepository.update(source, force = true)
            validateLoadedSource()
            status = "FantaMK patches updated"
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            status = "Update failed: ${e.message ?: e::class.simpleName}"
        } finally {
            refreshInstalledVersion()
            isBusy = false
        }
    }

    fun disconnect() = viewModelScope.launch {
        isBusy = true
        status = null
        try {
            findSource()?.let { patchBundleRepository.remove(it) }
            credentials.clearToken()
            isConfigured = false
            installedVersion = null
            status = "FantaMK private source disconnected"
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            status = "Disconnect failed: ${e.message ?: e::class.simpleName}"
        } finally {
            isBusy = false
        }
    }

    private suspend fun validateLoadedSource() {
        patchBundleRepository.reload()
        val source = findSource() ?: error("FantaMK source was not created")
        source.error?.let { throw it }
        checkNotNull(source.loaded) { "FantaMK patch bundle was not downloaded" }
    }

    private suspend fun refreshInstalledVersion() {
        installedVersion = findSource()?.loaded?.manifestAttributes?.version
    }

    private suspend fun restoreCredential(previousToken: String?) {
        if (previousToken.isNullOrBlank()) {
            runCatching { findSource()?.let { patchBundleRepository.remove(it) } }
            credentials.clearToken()
            isConfigured = false
        } else {
            credentials.saveToken(previousToken)
            isConfigured = true
        }
    }

    private suspend fun findSource(): RemoteSource<PatchBundle>? =
        patchBundleRepository.sources.first()
            .filterIsInstance<RemoteSource<PatchBundle>>()
            .firstOrNull { it.endpoint == FantaMKGitHubSource.ENDPOINT }
}
