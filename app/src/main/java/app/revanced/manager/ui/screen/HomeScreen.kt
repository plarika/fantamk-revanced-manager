package app.revanced.manager.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Update
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.revanced.manager.BuildConfig
import app.revanced.manager.R
import app.revanced.manager.ui.component.LazyColumnWithScrollbar
import app.revanced.manager.ui.component.LoadingIndicator
import app.revanced.manager.ui.component.NexoraOfficialActionCard
import app.revanced.manager.ui.component.NexoraOfficialAmber
import app.revanced.manager.ui.component.NexoraOfficialBrandHeader
import app.revanced.manager.ui.component.NexoraOfficialCyan
import app.revanced.manager.ui.component.NexoraOfficialGreen
import app.revanced.manager.ui.component.NexoraOfficialMetric
import app.revanced.manager.ui.component.NexoraOfficialPanel
import app.revanced.manager.ui.component.NexoraOfficialSectionTitle
import app.revanced.manager.ui.component.NexoraOfficialStatusPill
import app.revanced.manager.ui.component.NexoraOfficialViolet
import app.revanced.manager.ui.viewmodel.AppsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    sourceCount: Int,
    managerUpdateAvailable: Boolean,
    managerUpdateChecked: Boolean,
    onAppsClick: () -> Unit,
    onLibraryClick: () -> Unit,
    onUpdatesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: AppsViewModel = koinViewModel(),
) {
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val patchableApps by viewModel.patchableApps.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val channelLabel = stringResource(
        if (BuildConfig.VERSION_NAME.contains('-')) R.string.nexora_compact_dev
        else R.string.nexora_compact_stable,
    )
    val updateStatus = stringResource(
        when {
            managerUpdateAvailable -> R.string.nexora_metric_updates_available
            !managerUpdateChecked -> R.string.nexora_updates_not_checked
            else -> R.string.nexora_metric_updates_current
        },
    )
    val updateAccent = when {
        managerUpdateAvailable -> NexoraOfficialAmber
        !managerUpdateChecked -> NexoraOfficialCyan
        else -> NexoraOfficialGreen
    }
    LazyColumnWithScrollbar(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        val patched = installedApps
        val patchable = patchableApps
        if (patched == null || patchable == null) {
            item(key = "HOME_LOADING") {
                Box(
                    modifier = Modifier.fillParentMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingIndicator()
                }
            }
            return@LazyColumnWithScrollbar
        }

        val patchedPackages = patched
            .flatMap { listOf(it.currentPackageName, it.originalPackageName) }
            .toSet()
        val availableApps = patchable.count { it.packageName !in patchedPackages }

        item(key = "NEXORA_HOME") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                NexoraOfficialBrandHeader(
                    logo = painterResource(R.drawable.ic_logo_ring),
                    title = stringResource(R.string.nexora_home_hero_title),
                    subtitle = stringResource(R.string.nexora_home_hero_subtitle),
                )
                NexoraOfficialPanel {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        NexoraOfficialSectionTitle(
                            overline = channelLabel,
                            title = stringResource(R.string.nexora_workspace_title),
                            trailing = updateStatus,
                        )
                        Row(modifier = Modifier.fillMaxWidth()) {
                            NexoraOfficialMetric(
                                value = (patched.size + availableApps).toString(),
                                label = stringResource(R.string.nexora_metric_available),
                                icon = Icons.Default.Apps,
                                modifier = Modifier.weight(1f),
                                accent = NexoraOfficialViolet,
                            )
                            NexoraOfficialMetric(
                                value = patched.size.toString(),
                                label = stringResource(R.string.nexora_metric_modified),
                                icon = Icons.Default.AutoAwesome,
                                modifier = Modifier.weight(1f),
                                accent = NexoraOfficialGreen,
                            )
                            NexoraOfficialMetric(
                                value = sourceCount.toString(),
                                label = stringResource(R.string.nexora_metric_sources),
                                icon = Icons.Default.Storage,
                                modifier = Modifier.weight(1f),
                                accent = NexoraOfficialCyan,
                            )
                        }
                        NexoraOfficialStatusPill(
                            text = updateStatus,
                            accent = updateAccent,
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    NexoraOfficialActionCard(
                        icon = Icons.Default.Apps,
                        title = stringResource(R.string.nexora_nav_apps),
                        subtitle = stringResource(R.string.nexora_metric_apps_subtitle),
                        onClick = onAppsClick,
                        modifier = Modifier.weight(1f),
                        accent = NexoraOfficialViolet,
                    )
                    NexoraOfficialActionCard(
                        icon = Icons.Default.Folder,
                        title = stringResource(R.string.nexora_nav_library),
                        subtitle = stringResource(R.string.nexora_library_hero_subtitle),
                        onClick = onLibraryClick,
                        modifier = Modifier.weight(1f),
                        accent = NexoraOfficialCyan,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    NexoraOfficialActionCard(
                        icon = Icons.Default.Update,
                        title = stringResource(R.string.nexora_nav_updates),
                        subtitle = stringResource(R.string.nexora_updates_header_subtitle),
                        onClick = onUpdatesClick,
                        modifier = Modifier.weight(1f),
                        accent = updateAccent,
                    )
                    NexoraOfficialActionCard(
                        icon = Icons.Default.Settings,
                        title = stringResource(R.string.nexora_nav_settings),
                        subtitle = stringResource(R.string.nexora_compact_settings_desc),
                        onClick = onSettingsClick,
                        modifier = Modifier.weight(1f),
                        accent = NexoraOfficialViolet,
                    )
                }
            }
        }
    }
}
