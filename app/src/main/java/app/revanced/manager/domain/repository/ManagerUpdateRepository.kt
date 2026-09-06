package app.revanced.manager.domain.repository

import app.revanced.manager.BuildConfig
import app.revanced.manager.network.dto.ReVancedAsset
import app.revanced.manager.network.dto.ReVancedAssetHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDateTime

class ManagerUpdateRepository(
    private val releaseService: NexoraManagerReleaseService,
) {
    private var asset: ReVancedAsset? = null
    private val _releasedAt = MutableStateFlow<LocalDateTime?>(null)
    private val _version = MutableStateFlow<String?>(null)
    private val _hasUpdate = MutableStateFlow(false)

    val releasedAt = _releasedAt.asStateFlow()
    val hasUpdate = _hasUpdate.asStateFlow()
    val version = _version.asStateFlow()

    suspend fun refresh(): ReVancedAsset {
        val update = releaseService.getLatest()
        asset = update

        _releasedAt.value = update.createdAt
        _version.value = update.version
        _hasUpdate.value = isNewerVersion(update.version, BuildConfig.VERSION_NAME)

        return update
    }

    suspend fun getUpdateOrNull(refetch: Boolean = false): ReVancedAsset? {
        val current = if (refetch || asset == null) refresh() else asset!!
        return current.takeIf { _hasUpdate.value }
    }

    suspend fun getHistory(): List<ReVancedAssetHistory> =
        releaseService.getHistory()

    fun clearState() {
        asset = null
        _releasedAt.value = null
        _version.value = null
        _hasUpdate.value = false
    }
}

internal fun isNewerVersion(candidate: String, current: String): Boolean {
    val candidateVersion = SemVersion.parse(candidate) ?: return false
    val currentVersion = SemVersion.parse(current) ?: return false
    return candidateVersion > currentVersion
}

private data class SemVersion(
    val major: Long,
    val minor: Long,
    val patch: Long,
    val prerelease: List<String>?,
) : Comparable<SemVersion> {
    override fun compareTo(other: SemVersion): Int {
        compareValues(major, other.major).takeIf { it != 0 }?.let { return it }
        compareValues(minor, other.minor).takeIf { it != 0 }?.let { return it }
        compareValues(patch, other.patch).takeIf { it != 0 }?.let { return it }

        if (prerelease == null && other.prerelease == null) return 0
        if (prerelease == null) return 1
        if (other.prerelease == null) return -1

        val common = minOf(prerelease.size, other.prerelease.size)
        for (index in 0 until common) {
            val left = prerelease[index]
            val right = other.prerelease[index]
            val comparison = comparePrereleaseIdentifier(left, right)
            if (comparison != 0) return comparison
        }

        return compareValues(prerelease.size, other.prerelease.size)
    }

    companion object {
        private val VERSION_REGEX = Regex(
            "^v?(\\d+)\\.(\\d+)\\.(\\d+)(?:-([0-9A-Za-z.-]+))?(?:\\+[0-9A-Za-z.-]+)?$"
        )

        fun parse(value: String): SemVersion? {
            val match = VERSION_REGEX.matchEntire(value.trim()) ?: return null
            val major = match.groupValues[1].toLongOrNull() ?: return null
            val minor = match.groupValues[2].toLongOrNull() ?: return null
            val patch = match.groupValues[3].toLongOrNull() ?: return null
            val prerelease = match.groupValues[4]
                .takeIf { it.isNotEmpty() }
                ?.split('.')
                ?.takeIf { identifiers -> identifiers.all { it.isNotEmpty() } }
                ?: match.groupValues[4].takeIf { it.isNotEmpty() }?.let { return null }

            return SemVersion(major, minor, patch, prerelease)
        }

        private fun comparePrereleaseIdentifier(left: String, right: String): Int {
            val leftNumber = left.toLongOrNull()
            val rightNumber = right.toLongOrNull()

            return when {
                leftNumber != null && rightNumber != null -> compareValues(leftNumber, rightNumber)
                leftNumber != null -> -1
                rightNumber != null -> 1
                else -> left.compareTo(right)
            }
        }
    }
}
