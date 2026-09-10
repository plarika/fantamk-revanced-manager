package app.revanced.manager.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Source
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.revanced.manager.R
import app.revanced.manager.patcher.patch.PatchBundleInfo
import app.revanced.manager.patcher.patch.PatchInfo
import app.revanced.manager.ui.component.LazyColumnWithScrollbar
import app.revanced.manager.ui.component.NexoraCompactButton
import app.revanced.manager.ui.component.NexoraCompactSection
import app.revanced.manager.ui.component.NexoraOfficialActionCard
import app.revanced.manager.ui.component.NexoraOfficialBrandHeader
import app.revanced.manager.ui.component.NexoraOfficialCyan
import app.revanced.manager.ui.component.NexoraOfficialBorder
import app.revanced.manager.ui.component.NexoraOfficialMuted
import app.revanced.manager.ui.component.NexoraOfficialPanelStrong
import app.revanced.manager.ui.component.NexoraOfficialText
import app.revanced.manager.ui.component.NexoraOfficialMetric
import app.revanced.manager.ui.component.NexoraOfficialPanel
import app.revanced.manager.ui.component.NexoraOfficialSectionTitle
import app.revanced.manager.ui.component.NexoraOfficialViolet
import app.revanced.manager.ui.component.SearchBar
import app.revanced.manager.ui.component.TooltipHost
import app.revanced.manager.ui.component.TooltipIconButton
import app.revanced.manager.ui.component.haptics.HapticExtendedFloatingActionButton
import app.revanced.manager.ui.component.patches.BundleSection
import app.revanced.manager.ui.component.patches.IncompatiblePatchDialog
import app.revanced.manager.ui.component.patches.IncompatiblePatchesDialog
import app.revanced.manager.ui.component.patches.OptionsDialog
import app.revanced.manager.ui.component.patches.PatchItem
import app.revanced.manager.ui.component.patches.PatchesFilterBottomSheet
import app.revanced.manager.ui.component.patches.PatchesListHeader
import app.revanced.manager.ui.component.patches.SelectionWarningDialog
import app.revanced.manager.ui.component.patches.SourceSectionHeader
import app.revanced.manager.ui.component.patches.UniversalPatchWarningDialog
import app.revanced.manager.ui.component.patches.buildBundleSections
import app.revanced.manager.ui.viewmodel.PatchesSelectorViewModel
import app.revanced.manager.ui.viewmodel.PatchesSelectorViewModel.DialogState
import app.revanced.manager.util.Options
import app.revanced.manager.util.PatchSelection
import app.revanced.manager.util.isScrollingUp
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.sample

private enum class NexoraLibraryPage {
    SOURCES,
    COLLECTIONS,
    HISTORY,
}

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun PatchesSelectorScreen(
    onSave: (PatchSelection?, Options) -> Unit,
    onBackClick: () -> Unit,
    onBundleInfoClick: (Int) -> Unit,
    isSourceEditMode: Boolean = false,
    onSourceDeleteRequest: ((Int) -> Unit)? = null,
    onSyncAll: (() -> Unit)? = null,
    onAddSource: (() -> Unit)? = null,
    viewModel: PatchesSelectorViewModel
) {
    val stickyHeaderTopGap = 8.dp
    val readOnly = viewModel.readOnly
    var libraryPage by rememberSaveable { mutableStateOf(NexoraLibraryPage.SOURCES) }
    val bundles by viewModel.bundlesFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val displayBundles = remember(bundles, readOnly) {
        if (!readOnly) {
            bundles
        } else {
            bundles.distinctBy { bundle -> bundle.name.lowercase() to bundle.version }
        }
    }
    val bundleLoadIssues by viewModel.bundleLoadIssuesFlow.collectAsStateWithLifecycle(initialValue = emptyMap())
    val patchLazyListState = rememberLazyListState()
    val searchLazyListState = rememberLazyListState()
    val (query, setQuery) = rememberSaveable { mutableStateOf("") }
    val (searchExpanded, setSearchExpanded) = rememberSaveable { mutableStateOf(false) }
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    var collapsedBundleUids by rememberSaveable { mutableStateOf(emptyList<Int>()) }
    var selectedPackageFilters by rememberSaveable { mutableStateOf(emptySet<String>()) }
    val effectiveSelectedPackageFilters = if (readOnly) selectedPackageFilters else emptySet()

    LaunchedEffect(readOnly, libraryPage) {
        if (readOnly) patchLazyListState.scrollToItem(0)
    }

    val showSaveButton by remember {
        derivedStateOf { !readOnly && viewModel.selectionIsValid(bundles) }
    }

    val defaultPatchSelectionCount by viewModel.defaultSelectionCount
        .collectAsStateWithLifecycle(initialValue = 0)

    val selectedPatchCount by remember {
        derivedStateOf {
            viewModel.customPatchSelection?.values?.sumOf { it.size } ?: defaultPatchSelectionCount
        }
    }


    fun toggleBundleExpanded(bundleUid: Int) {
        if (isSourceEditMode) return
        collapsedBundleUids = if (bundleUid in collapsedBundleUids) {
            collapsedBundleUids - bundleUid
        } else {
            collapsedBundleUids + bundleUid
        }
    }

    fun onBundleSelectionClick(bundle: PatchBundleInfo.Scoped) {
        if (readOnly) return

        val selectionState = viewModel.getBundleSelectionState(bundle)
        when {
            viewModel.selectionWarningEnabled -> viewModel.showSelectionWarning()
            selectionState == false -> viewModel.restoreDefaults(bundle.uid)
            else -> viewModel.deselectAll(bundles, bundle.uid)
        }
    }

    val effectiveCollapsedBundleUids =
        remember(displayBundles, collapsedBundleUids, readOnly, isSourceEditMode) {
            when {
                isSourceEditMode -> displayBundles.map { it.uid }
                readOnly -> displayBundles.map { it.uid }.filter { it !in collapsedBundleUids }
                else -> collapsedBundleUids
            }
        }

    val sections = remember(
        displayBundles,
        viewModel.filter,
        effectiveCollapsedBundleUids,
        effectiveSelectedPackageFilters
    ) {
        buildBundleSections(
            bundles = displayBundles,
            filter = viewModel.filter,
            collapsedBundleUids = effectiveCollapsedBundleUids,
            selectedPackageNames = effectiveSelectedPackageFilters
        )
    }
    val searchSections = remember(
        displayBundles,
        query,
        viewModel.filter,
        effectiveCollapsedBundleUids,
        effectiveSelectedPackageFilters
    ) {
        buildBundleSections(
            bundles = displayBundles,
            query = query,
            filter = viewModel.filter,
            collapsedBundleUids = effectiveCollapsedBundleUids,
            selectedPackageNames = effectiveSelectedPackageFilters,
            forceExpanded = query.isNotBlank()
        )
    }

    if (showBottomSheet) {
        PatchesFilterBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sections = sections,
            patchLazyListState = patchLazyListState,
            bundles = displayBundles,
            filter = viewModel.filter,
            onToggleFlag = viewModel::toggleFlag,
            packageName = viewModel.packageName.ifBlank { null },
            readOnly = readOnly,
            selectionWarningEnabled = viewModel.selectionWarningEnabled,
            onShowSelectionWarning = viewModel::showSelectionWarning,
            onRestoreDefaults = viewModel::restoreDefaults,
            onDeselectAll = { uid -> viewModel.deselectAll(bundles, uid) },
            onInvertSelection = { uid -> viewModel.invertSelection(bundles, uid) },
            onDeselectAllExcept = viewModel::deselectAllExcept,
            selectedPackageFilters = selectedPackageFilters,
            onTogglePackageFilter = { pkg ->
                selectedPackageFilters = if (pkg in selectedPackageFilters) {
                    selectedPackageFilters - pkg
                } else {
                    selectedPackageFilters + pkg
                }
            }
        )
    }

    when (val dialog = viewModel.activeDialog) {
        is DialogState.IncompatiblePatch -> {
            IncompatiblePatchDialog(
                appVersion = viewModel.appVersion ?: stringResource(R.string.any_version),
                compatibleVersions = dialog.compatibleVersions,
                onDismissRequest = viewModel::dismissDialogs
            )
        }

        is DialogState.IncompatiblePatchesInfo -> {
            IncompatiblePatchesDialog(
                appVersion = viewModel.appVersion ?: stringResource(R.string.any_version),
                onDismissRequest = viewModel::dismissDialogs
            )
        }

        is DialogState.Options -> {
            OptionsDialog(
                onDismissRequest = viewModel::dismissDialogs,
                patch = dialog.patch,
                values = viewModel.getOptions(dialog.bundle, dialog.patch),
                reset = { viewModel.resetOptions(dialog.bundle, dialog.patch) },
                resetOption = { viewModel.resetOption(dialog.bundle, dialog.patch, it) },
                set = { key, value ->
                    viewModel.setOption(
                        dialog.bundle,
                        dialog.patch,
                        key,
                        value
                    )
                },
                selectionWarningEnabled = viewModel.selectionWarningEnabled,
                readOnly = readOnly,
            )
        }

        is DialogState.SelectionWarning -> {
            SelectionWarningDialog(onDismiss = viewModel::dismissDialogs)
        }

        is DialogState.UniversalPatchWarning -> {
            UniversalPatchWarningDialog(onDismiss = viewModel::dismissDialogs)
        }

        else -> {}
    }

    fun LazyListScope.patchList(
        uid: Int,
        patches: List<PatchInfo>,
        compatible: Boolean,
        keyPrefix: String,
        header: (@Composable () -> Unit)? = null
    ) {
        if (patches.isEmpty()) return

        header?.let {
            item(key = "$keyPrefix-header", contentType = 0) { it() }
        }

        itemsIndexed(
            items = patches,
            key = { index, patch -> patchItemKey(keyPrefix, patch.name, index) },
            contentType = { _, _ -> 1 }
        ) { _, patch ->
            PatchItem(
                patch = patch,
                onOptionsDialog = { viewModel.openOptionsDialog(uid, patch) },
                selected = compatible && viewModel.isSelected(uid, patch),
                onToggle = {
                    when {
                        !compatible -> viewModel.openIncompatibleDialog(patch)
                        viewModel.selectionWarningEnabled -> viewModel.showSelectionWarning()
                        patch.compatiblePackages == null && viewModel.universalPatchWarningEnabled ->
                            viewModel.showUniversalPatchWarning()

                        else -> viewModel.togglePatch(uid, patch)
                    }
                },
                compatible = compatible,
                readOnly = readOnly,
                scopedPackageName = viewModel.packageName.ifBlank { null }
            )
        }
    }

    fun LazyListScope.sectionedPatchList(
        sections: List<BundleSection>,
        keyPrefix: String
    ) {
        sections.forEach { section ->
            val bundle = section.bundle
            val loadIssueResId = bundleLoadIssues[bundle.uid]

            stickyHeader(key = "$keyPrefix-source-${bundle.uid}") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(1f)
                        .background(if (readOnly) Color.Transparent else MaterialTheme.colorScheme.surface)
                ) {
                    SourceSectionHeader(
                        bundle = bundle,
                        expanded = section.expanded,
                        selectionState = viewModel.getBundleSelectionState(bundle),
                        onClick = { onBundleInfoClick(bundle.uid) },
                        onSelectionClick = { onBundleSelectionClick(bundle) },
                        onExpandToggle = { toggleBundleExpanded(bundle.uid) },
                        onDeleteClick = { onSourceDeleteRequest?.invoke(bundle.uid) },
                        sourceEditMode = isSourceEditMode,
                        readOnly = readOnly,
                        loadIssue = loadIssueResId?.let { messageId ->
                            stringResource(messageId)
                        }
                    )
                }
            }

            if (!section.expanded) return@forEach

            patchList(
                uid = bundle.uid,
                patches = section.compatible,
                compatible = true,
                keyPrefix = "$keyPrefix-compatible-${bundle.uid}"
            )

            patchList(
                uid = bundle.uid,
                patches = section.universal,
                compatible = true,
                keyPrefix = "$keyPrefix-universal-${bundle.uid}"
            ) {
                PatchesListHeader(title = stringResource(R.string.universal_patches))
            }

            patchList(
                uid = bundle.uid,
                patches = section.incompatible,
                compatible = viewModel.allowIncompatiblePatches,
                keyPrefix = "$keyPrefix-incompatible-${bundle.uid}"
            ) {
                PatchesListHeader(
                    title = stringResource(R.string.incompatible_patches),
                    onHelpClick = { viewModel.showIncompatiblePatchesInfo() }
                )
            }
        }
    }

    Scaffold(
        containerColor = if (readOnly) Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            Box(modifier = Modifier.padding(horizontal = if (searchExpanded) 0.dp else 16.dp)) {
                SearchBar(
                    query = query,
                    onQueryChange = setQuery,
                    expanded = searchExpanded,
                    onExpandedChange = { expanded ->
                        if (readOnly && expanded) libraryPage = NexoraLibraryPage.SOURCES
                        setSearchExpanded(expanded)
                    },
                    placeholder = { Text(stringResource(R.string.search_patches)) },
                    windowInsets = if (readOnly) WindowInsets(top = 0, bottom = 0) else WindowInsets.systemBars,
                    leadingIcon = {
                        TooltipIconButton(
                            onClick = {
                                if (searchExpanded) setSearchExpanded(false) else onBackClick()
                            },
                            tooltip = stringResource(R.string.back),
                        ) { contentDescription ->
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = contentDescription
                            )
                        }
                    },
                    trailingIcon = {
                        AnimatedContent(
                            targetState = searchExpanded,
                            label = "Filter/Clear",
                            transitionSpec = { fadeIn() togetherWith fadeOut() }
                        ) { expanded ->
                            if (expanded) {
                                TooltipIconButton(
                                    onClick = { setQuery("") },
                                    enabled = query.isNotEmpty(),
                                    tooltip = stringResource(R.string.clear),
                                ) { contentDescription ->
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = contentDescription
                                    )
                                }
                            } else {
                                TooltipIconButton(
                                    onClick = {
                                        if (readOnly) libraryPage = NexoraLibraryPage.SOURCES
                                        showBottomSheet = true
                                    },
                                    tooltip = stringResource(R.string.more),
                                ) { contentDescription ->
                                    Icon(
                                        imageVector = Icons.Outlined.FilterList,
                                        contentDescription = contentDescription
                                    )
                                }
                            }
                        }
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(if (readOnly) Color.Transparent else MaterialTheme.colorScheme.surface)
                    ) {
                        LazyColumnWithScrollbar(
                            modifier = Modifier.fillMaxSize(),
                            state = searchLazyListState,
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            sectionedPatchList(
                                sections = searchSections,
                                keyPrefix = "search"
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (!showSaveButton) return@Scaffold

            AnimatedVisibility(
                visible = !searchExpanded,
                enter = slideInHorizontally { it } + fadeIn(),
                exit = slideOutHorizontally { it } + fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TooltipHost(tooltip = stringResource(R.string.reset)) { tooltipModifier ->
                        SmallFloatingActionButton(
                            onClick = viewModel::reset,
                            modifier = tooltipModifier,
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Icon(Icons.Outlined.Restore, stringResource(R.string.reset))
                        }
                    }

                    val isScrollingUp = patchLazyListState.isScrollingUp()
                    val expanded by produceState(true, isScrollingUp) {
                        value = isScrollingUp.value
                        snapshotFlow { isScrollingUp.value }
                            .sample(333L)
                            .collect { value = it }
                    }

                    HapticExtendedFloatingActionButton(
                        text = {
                            Text(stringResource(R.string.save_with_count, selectedPatchCount))
                        },
                        tooltip = stringResource(R.string.save),
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Save,
                                contentDescription = stringResource(R.string.save)
                            )
                        },
                        expanded = expanded,
                        onClick = {
                            onSave(viewModel.getCustomSelection(), viewModel.getOptions())
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        if (searchExpanded) return@Scaffold

        val appliedPadding = if (!readOnly) {
            paddingValues
        } else {
            PaddingValues(top = paddingValues.calculateTopPadding())
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(appliedPadding)
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(stickyHeaderTopGap)
                    .background(if (readOnly) Color.Transparent else MaterialTheme.colorScheme.surface)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyColumnWithScrollbar(
                    modifier = Modifier.fillMaxSize(),
                    state = patchLazyListState
                ) {
                    if (readOnly) {
                        item(key = "NEXORA_LIBRARY_OVERVIEW") {
                            NexoraLibraryOverview(
                                bundles = displayBundles,
                                onSyncAll = onSyncAll,
                                onAddSource = onAddSource,
                                selectedPage = libraryPage,
                                onPageSelected = { libraryPage = it },
                            )
                        }
                        when (libraryPage) {
                            NexoraLibraryPage.SOURCES -> sectionedPatchList(sections, "main")
                            NexoraLibraryPage.COLLECTIONS -> itemsIndexed(
                                items = displayBundles,
                                key = { _, bundle -> "collection-${bundle.uid}" },
                            ) { _, bundle ->
                                NexoraCollectionCard(bundle) { onBundleInfoClick(bundle.uid) }
                            }
                            NexoraLibraryPage.HISTORY -> itemsIndexed(
                                items = displayBundles.sortedByDescending { it.releasedAt?.toString().orEmpty() },
                                key = { _, bundle -> "history-${bundle.uid}" },
                            ) { _, bundle ->
                                NexoraHistoryCard(bundle) { onBundleInfoClick(bundle.uid) }
                            }
                        }
                    } else {
                        sectionedPatchList(sections, "main")
                    }
                }
            }
        }
    }
}

@Composable
private fun NexoraLibraryOverview(
    bundles: List<PatchBundleInfo.Scoped>,
    onSyncAll: (() -> Unit)?,
    onAddSource: (() -> Unit)?,
    selectedPage: NexoraLibraryPage,
    onPageSelected: (NexoraLibraryPage) -> Unit,
) {
    val totalPatches = remember(bundles) { bundles.sumOf { it.patches.size } }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        NexoraOfficialBrandHeader(
            logo = painterResource(R.drawable.ic_logo_ring),
            title = stringResource(R.string.nexora_nav_library),
            subtitle = stringResource(R.string.nexora_library_hero_subtitle),
        )
        NexoraOfficialPanel {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                NexoraOfficialSectionTitle(
                    overline = stringResource(R.string.nexora_library_hero_title),
                    title = stringResource(R.string.nexora_sources_available),
                    trailing = bundles.size.toString(),
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    NexoraOfficialMetric(
                        value = totalPatches.toString(),
                        label = stringResource(R.string.nexora_metric_patches),
                        icon = Icons.Outlined.FilterList,
                        modifier = Modifier.weight(1f),
                        accent = NexoraOfficialViolet,
                    )
                    NexoraOfficialMetric(
                        value = bundles.size.toString(),
                        label = stringResource(R.string.nexora_metric_sources),
                        icon = Icons.Outlined.Source,
                        modifier = Modifier.weight(1f),
                        accent = NexoraOfficialCyan,
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NexoraLibraryTab(
                text = stringResource(R.string.nexora_library_sources_tab),
                selected = selectedPage == NexoraLibraryPage.SOURCES,
                onClick = { onPageSelected(NexoraLibraryPage.SOURCES) },
                modifier = Modifier.weight(1f),
            )
            NexoraLibraryTab(
                text = stringResource(R.string.nexora_library_collections_tab),
                selected = selectedPage == NexoraLibraryPage.COLLECTIONS,
                onClick = { onPageSelected(NexoraLibraryPage.COLLECTIONS) },
                modifier = Modifier.weight(1f),
            )
            NexoraLibraryTab(
                text = stringResource(R.string.nexora_library_history_tab),
                selected = selectedPage == NexoraLibraryPage.HISTORY,
                onClick = { onPageSelected(NexoraLibraryPage.HISTORY) },
                modifier = Modifier.weight(1f),
            )
        }
        if (selectedPage == NexoraLibraryPage.SOURCES) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                onSyncAll?.let {
                    NexoraOfficialActionCard(
                        icon = Icons.Outlined.Restore,
                        title = stringResource(R.string.nexora_sync_now),
                        subtitle = stringResource(R.string.nexora_library_hero_subtitle),
                        onClick = it,
                        modifier = Modifier.weight(1f),
                        accent = NexoraOfficialCyan,
                    )
                }
                onAddSource?.let {
                    NexoraOfficialActionCard(
                        icon = Icons.Outlined.Source,
                        title = stringResource(R.string.nexora_add_source),
                        subtitle = stringResource(R.string.nexora_metric_sources_subtitle),
                        onClick = it,
                        modifier = Modifier.weight(1f),
                        accent = NexoraOfficialViolet,
                    )
                }
            }
        } else if (selectedPage == NexoraLibraryPage.COLLECTIONS) {
            NexoraLibraryIntro(
                title = stringResource(R.string.nexora_collections_title),
                subtitle = stringResource(R.string.nexora_collections_subtitle),
            )
        } else {
            NexoraLibraryIntro(
                title = stringResource(R.string.nexora_history_title),
                subtitle = stringResource(R.string.nexora_history_subtitle),
            )
        }
    }
}

@Composable
private fun NexoraLibraryIntro(
    title: String,
    subtitle: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = NexoraOfficialPanelStrong,
        border = BorderStroke(1.dp, NexoraOfficialBorder),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = NexoraOfficialText)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = NexoraOfficialMuted,
            )
        }
    }
}

@Composable
private fun NexoraCollectionCard(
    bundle: PatchBundleInfo.Scoped,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        color = NexoraOfficialPanelStrong,
        border = BorderStroke(1.dp, NexoraOfficialBorder),
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = NexoraOfficialViolet.copy(alpha = 0.16f),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Source,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = NexoraOfficialViolet,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(bundle.name, style = MaterialTheme.typography.titleMedium, color = NexoraOfficialText)
                Text(
                    text = "${bundle.version ?: stringResource(R.string.any_version)} • ${bundle.patches.size} ${stringResource(R.string.nexora_metric_patches)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NexoraOfficialMuted,
                )
            }
            Text(
                text = "›",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun NexoraHistoryCard(
    bundle: PatchBundleInfo.Scoped,
    onClick: () -> Unit,
) {
    val releasedText = bundle.releasedAt?.date?.toString()?.let {
        stringResource(R.string.nexora_history_released, it)
    } ?: stringResource(R.string.nexora_history_date_unknown)
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        color = NexoraOfficialPanelStrong,
        border = BorderStroke(1.dp, NexoraOfficialBorder),
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = NexoraOfficialViolet.copy(alpha = 0.16f),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Restore,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = NexoraOfficialViolet,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(bundle.name, style = MaterialTheme.typography.titleMedium, color = NexoraOfficialText)
                Text(
                    text = bundle.version ?: stringResource(R.string.any_version),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = releasedText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NexoraOfficialMuted,
                )
                Text(
                    text = "${bundle.patches.size} ${stringResource(R.string.nexora_metric_patches)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = NexoraOfficialMuted,
                )
            }
            Text(
                text = "›",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun NexoraLibraryTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) NexoraOfficialViolet else NexoraOfficialPanelStrong,
        border = BorderStroke(1.dp, if (selected) NexoraOfficialViolet else NexoraOfficialBorder),
        tonalElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) Color.White else NexoraOfficialText,
            )
        }
    }
}
private fun patchItemKey(keyPrefix: String, patchName: String, index: Int) =
    "$keyPrefix-$index-$patchName"
