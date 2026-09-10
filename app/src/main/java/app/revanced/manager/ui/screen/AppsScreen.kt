package app.revanced.manager.ui.screen

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.revanced.manager.BuildConfig
import app.revanced.manager.R
import app.revanced.manager.data.room.apps.installed.InstalledApp
import app.revanced.manager.ui.component.AppIcon
import app.revanced.manager.ui.component.AppLabel
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
import app.revanced.manager.ui.component.NexoraCompactAppRow
import app.revanced.manager.ui.component.NexoraCompactBadge
import app.revanced.manager.ui.component.NexoraCompactButton
import app.revanced.manager.ui.component.NexoraCompactQuickCard
import app.revanced.manager.ui.component.NexoraCompactSection
import app.revanced.manager.ui.component.SearchBar
import app.revanced.manager.ui.component.SurfaceChip
import app.revanced.manager.ui.component.TooltipIconButton
import app.revanced.manager.ui.model.SelectedApp
import app.revanced.manager.ui.viewmodel.AppsViewModel
import app.revanced.manager.util.APK_MIMETYPE
import app.revanced.manager.util.AppInfo
import app.revanced.manager.util.EventEffect
import app.revanced.manager.util.toast
import app.revanced.manager.util.transparentListItemColors
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)
@Composable
fun AppsScreen(
    onAppClick: (InstalledApp) -> Unit,
    onPatchableAppClick: (String) -> Unit,
    onStorageSelect: (SelectedApp.Local) -> Unit,
    sourceCount: Int,
    managerUpdateAvailable: Boolean,
    managerUpdateChecked: Boolean,
    onAppsClick: () -> Unit,
    onLibraryClick: () -> Unit,
    onUpdatesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    lazyListState: LazyListState = rememberLazyListState(),
    searchLazyListState: LazyListState = rememberLazyListState(),
    onSearchExpandedChange: (Boolean) -> Unit = {},
    viewModel: AppsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    EventEffect(flow = viewModel.storageSelectionFlow) {
        onStorageSelect(it)
    }

    val pickApkLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let(viewModel::handleStorageResult)
        }

    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val patchableApps by viewModel.patchableApps.collectAsStateWithLifecycle()
    val suggestedVersions by viewModel.suggestedVersions.collectAsStateWithLifecycle()
    val pinnedApps by viewModel.pinnedApps.collectAsStateWithLifecycle()

    val patchedPackageNames =
        installedApps?.flatMap { listOf(it.currentPackageName, it.originalPackageName) }?.toSet()
            .orEmpty()

    fun InstalledApp.matchesQuery(query: String): Boolean {
        if (query.isBlank()) return true

        val packageInfo = viewModel.packageInfoMap[currentPackageName]
        return currentPackageName.contains(
            query, ignoreCase = true
        ) || originalPackageName.contains(query, ignoreCase = true) || viewModel.loadLabel(
            packageInfo
        ).contains(query, ignoreCase = true)
    }

    fun patchableMatchesQuery(packageName: String, label: String?, query: String): Boolean {
        if (query.isBlank()) return true

        return packageName.contains(query, ignoreCase = true) || label?.contains(
            query, ignoreCase = true
        ) == true
    }

    var searchExpanded by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(searchExpanded) {
        onSearchExpandedChange(searchExpanded)
    }
    val filterText by viewModel.filterText.collectAsStateWithLifecycle()

    val showAppSearchBar by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex >= 1 }
    }

    Scaffold(topBar = {
        if (showAppSearchBar || searchExpanded) {
            Box(modifier = Modifier.padding(horizontal = if (searchExpanded) 0.dp else 16.dp)) {
            SearchBar(
                query = filterText,
                onQueryChange = viewModel::setFilterText,
                expanded = searchExpanded,
                onExpandedChange = { searchExpanded = it },
                placeholder = { Text(stringResource(R.string.search_apps)) },
                windowInsets = WindowInsets(0, 0, 0, 0),
                leadingIcon = {
                    TooltipIconButton(
                        onClick = {
                            if (searchExpanded) {
                                searchExpanded = false
                                viewModel.setFilterText("")
                            }
                        },
                        tooltip = if (searchExpanded) stringResource(R.string.back) else stringResource(
                            R.string.search
                        ),
                    ) { _ ->
                        Crossfade(
                            targetState = searchExpanded, label = "SearchIcon"
                        ) { expanded ->
                            Icon(
                                imageVector = if (expanded) Icons.AutoMirrored.Filled.ArrowBack else Icons.Outlined.Search,
                                contentDescription = if (expanded) stringResource(R.string.back) else stringResource(
                                    R.string.search
                                )
                            )
                        }
                    }
                },
                trailingIcon = {
                    if (searchExpanded && filterText.isNotEmpty()) {
                        TooltipIconButton(
                            onClick = { viewModel.setFilterText("") },
                            tooltip = stringResource(R.string.clear),
                        ) { contentDescription ->
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = contentDescription
                            )
                        }
                    }
                },
            ) {
                val query = filterText.trim()
                val patched = installedApps
                val patchable = patchableApps
                val filteredPatchedApps = patched?.filter { it.matchesQuery(query) }.orEmpty()
                val filteredPatchableApps = patchable?.filter { app ->
                    app.packageName !in patchedPackageNames && patchableMatchesQuery(
                        packageName = app.packageName,
                        label = viewModel.loadLabel(app.packageInfo),
                        query = query
                    )
                }.orEmpty()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    if (patched == null || patchable == null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingIndicator()
                        }
                    } else {
                        LazyColumnWithScrollbar(
                            modifier = Modifier.fillMaxSize(), state = searchLazyListState
                        ) {
                            appItems(
                                items = buildAppList(
                                    filteredPatchedApps, filteredPatchableApps, pinnedApps
                                ),
                                viewModel = viewModel,
                                suggestedVersions = suggestedVersions,
                                onPatchedClick = {
                                    searchExpanded = false
                                    viewModel.setFilterText(""); onAppClick(it)
                                },
                                onPatchableClick = {
                                    searchExpanded = false
                                    viewModel.setFilterText(""); onPatchableAppClick(it)
                                })
                        }
                    }
                }
            }
        }
        }
    }) { paddingValues ->
        if (searchExpanded) return@Scaffold

        LazyColumnWithScrollbar(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .padding(top = 8.dp),
            state = lazyListState,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            val patched = installedApps
            val patchable = patchableApps

            if (patched == null || patchable == null) {
                item(key = "LOADING") {
                    Box(
                        modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator()
                    }
                }
                return@LazyColumnWithScrollbar
            }

            val allPatchableApps = patchable.filter { it.packageName !in patchedPackageNames }

            item(key = "NEXORA_OVERVIEW") {
                val channelLabel = stringResource(
                    if (BuildConfig.VERSION_NAME.contains('-')) R.string.nexora_compact_dev
                    else R.string.nexora_compact_stable
                )
                val updateStatus = stringResource(
                    when {
                        managerUpdateAvailable -> R.string.nexora_metric_updates_available
                        !managerUpdateChecked -> R.string.nexora_updates_not_checked
                        else -> R.string.nexora_metric_updates_current
                    }
                )
                val updateAccent = when {
                    managerUpdateAvailable -> NexoraOfficialAmber
                    !managerUpdateChecked -> NexoraOfficialCyan
                    else -> NexoraOfficialGreen
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
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
                                    value = (patched.size + allPatchableApps.size).toString(),
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
                    NexoraOfficialActionCard(
                        icon = Icons.Default.Storage,
                        title = stringResource(R.string.nexora_compact_add_app),
                        subtitle = stringResource(R.string.nexora_compact_patcher_desc),
                        onClick = {
                            try {
                                pickApkLauncher.launch(APK_MIMETYPE)
                            } catch (_: ActivityNotFoundException) {
                                context.toast(R.string.no_file_picker_found)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        accent = NexoraOfficialCyan,
                    )
                }
            }

            item(key = "ALL_APPS_HEADER") {
                SectionHeader(
                    icon = Icons.Default.Apps,
                    title = stringResource(R.string.nexora_compact_all_apps),
                )
            }

            appItems(
                items = buildAppList(patched, allPatchableApps, pinnedApps),
                viewModel = viewModel,
                suggestedVersions = suggestedVersions,
                onPatchedClick = onAppClick,
                onPatchableClick = onPatchableAppClick
            )
        }
    }
}

private sealed interface AppListItem {
    data class Patched(val app: InstalledApp) : AppListItem
    data class Patchable(val app: AppInfo) : AppListItem
    data object PinnedHeader : AppListItem
    data object AvailableHeader : AppListItem
}

private fun buildAppList(
    patchedApps: List<InstalledApp>, patchableApps: List<AppInfo>, pinnedApps: Set<String>
): List<AppListItem> = buildList {
    val (pinnedPatched, unpinnedPatched) = patchedApps.partition { it.currentPackageName in pinnedApps }
    val (pinnedPatchable, unpinnedPatchable) = patchableApps.partition { it.packageName in pinnedApps }

    if (pinnedPatched.isNotEmpty() || pinnedPatchable.isNotEmpty()) {
        add(AppListItem.PinnedHeader)
        pinnedPatched.mapTo(this) { AppListItem.Patched(it) }
        pinnedPatchable.mapTo(this) { AppListItem.Patchable(it) }
        if (unpinnedPatched.isNotEmpty() || unpinnedPatchable.isNotEmpty()) {
            add(AppListItem.AvailableHeader)
        }
    }

    unpinnedPatched.mapTo(this) { AppListItem.Patched(it) }
    unpinnedPatchable.mapTo(this) { AppListItem.Patchable(it) }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.appItems(
    items: List<AppListItem>,
    viewModel: AppsViewModel,
    suggestedVersions: Map<String, String?>,
    onPatchedClick: (InstalledApp) -> Unit,
    onPatchableClick: (String) -> Unit
) {
    items(
        items = items,
        key = {
            when (it) {
                is AppListItem.Patched -> "PATCHED-${it.app.currentPackageName}"
                is AppListItem.Patchable -> "PATCHABLE-${it.app.packageName}"
                AppListItem.PinnedHeader -> "HEADER_PINNED"
                AppListItem.AvailableHeader -> "HEADER_AVAILABLE"
            }
        },
        contentType = { it::class.simpleName }
    ) { item ->
        when (item) {
            is AppListItem.PinnedHeader -> SectionHeader(
                icon = Icons.Default.PushPin,
                title = stringResource(R.string.pinned_apps_section_title),
                modifier = Modifier.animateItem()
            )

            is AppListItem.AvailableHeader -> SectionHeader(
                icon = Icons.Default.Apps,
                title = stringResource(R.string.available_apps_section_title),
                modifier = Modifier.animateItem()
            )

            is AppListItem.Patched -> AppItem(
                modifier = Modifier.animateItem(),
                onClick = { onPatchedClick(item.app) },
                onLongClick = { viewModel.togglePinned(item.app.currentPackageName) },
                packageName = item.app.currentPackageName,
                packageInfo = viewModel.packageInfoMap[item.app.currentPackageName],
                isPatched = true,
            )

            is AppListItem.Patchable -> AppItem(
                modifier = Modifier.animateItem(),
                onClick = { onPatchableClick(item.app.packageName) },
                onLongClick = { viewModel.togglePinned(item.app.packageName) },
                packageInfo = item.app.packageInfo,
                packageName = item.app.packageName,
                patchCount = item.app.patches,
                suggestedVersion = suggestedVersions[item.app.packageName]
                    ?: stringResource(R.string.any_version),
            )
        }
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector, title: String, modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = NexoraOfficialViolet
        )
        Text(
            text = title,
            color = NexoraOfficialViolet,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    packageName: String,
    packageInfo: android.content.pm.PackageInfo?,
    isPatched: Boolean = false,
    patchCount: Int? = null,
    suggestedVersion: String? = null,
) {
    val cardShape = RoundedCornerShape(18.dp)
    val accent = if (isPatched) NexoraOfficialGreen else NexoraOfficialViolet
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 5.dp)
            .border(1.dp, accent.copy(alpha = if (isPatched) 0.62f else 0.45f), cardShape)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = cardShape,
        color = Color(0xEE071020),
        tonalElevation = 0.dp
    ) {
        ListItem(
            leadingContent = {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF050A16),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
                    tonalElevation = 0.dp,
                ) {
                    Box(
                        modifier = Modifier.padding(6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        AppIcon(
                            packageInfo = packageInfo,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            },
            headlineContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isPatched) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = NexoraOfficialViolet
                        )
                    }
                    AppLabel(packageInfo, defaultText = packageName)
                }
            },
            supportingContent = {
                patchCount?.let {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (patchCount > 0) SurfaceChip(
                            pluralStringResource(
                                R.plurals.patch_count,
                                patchCount,
                                patchCount
                            )
                        )
                        if (suggestedVersion != null) SurfaceChip(suggestedVersion)
                    }
                } ?: Text(
                    text = packageName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            trailingContent = if (packageInfo == null) {
                { Text(stringResource(R.string.not_installed)) }
            } else null,
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}
