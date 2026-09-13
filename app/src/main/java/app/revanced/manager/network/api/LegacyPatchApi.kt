package app.revanced.manager.network.api

import android.util.Log
import app.revanced.manager.domain.manager.PreferencesManager
import app.revanced.manager.domain.manager.base.Preference
import app.revanced.manager.network.dto.RemoteAsset
import app.revanced.manager.network.dto.RemoteAssetHistory
import app.revanced.manager.network.service.HttpService
import app.revanced.manager.network.utils.APIResponse
import io.ktor.client.request.url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LegacyPatchApi(
    private val client: HttpService,
    private val prefs: PreferencesManager,
) {
    private suspend fun apiUrl() = prefs.legacyApiUrl.get()
    private val defaultApiVersion = "v5"

    private suspend inline fun <reified T> request(
        api: String,
        apiVersion: String,
        route: String,
    ): APIResponse<T> = withContext(Dispatchers.IO) {
        val fullUrl = "$api/$apiVersion/$route"
        try {
            Log.d("LegacyPatchApi", "Requesting: $fullUrl")
            client.request { url(fullUrl) }
        } catch (e: Exception) {
            Log.e("LegacyPatchApi", "Failed request: $fullUrl", e)
            throw e
        }
    }

    private suspend inline fun <reified T> request(
        route: String,
        apiVersion: String = defaultApiVersion,
    ) = request<T>(apiUrl(), apiVersion, route)

    suspend fun getPatchesUpdate() =
        request<RemoteAsset>("patches${prefs.usePatchesPrereleases.prereleaseString()}")

    suspend fun getPatchesHistory(apiUrl: String, prerelease: Boolean) =
        request<List<RemoteAssetHistory>>(
            apiUrl,
            defaultApiVersion,
            "patches/history${prerelease.prereleaseString()}",
        )

    suspend fun getDownloaderUpdate() =
        request<RemoteAsset>("manager/downloaders${prefs.useDownloaderPrerelease.prereleaseString()}")

    private companion object {
        suspend fun Preference<Boolean>.prereleaseString() =
            if (get()) "/prerelease" else ""

        fun Boolean.prereleaseString() =
            if (this) "/prerelease" else ""
    }
}
