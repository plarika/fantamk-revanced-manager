package app.revanced.manager.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import app.revanced.manager.R
import app.revanced.manager.data.room.apps.installed.InstalledPatchBundle
import app.revanced.manager.ui.component.FullscreenDialog
import app.revanced.manager.ui.component.LazyColumnWithScrollbar
import app.revanced.manager.ui.component.NexoraOfficialBackdrop
import app.revanced.manager.ui.component.NexoraOfficialPageHeader
import app.revanced.manager.ui.component.patches.PatchesListHeader
import app.revanced.manager.util.PatchSelection
import app.revanced.manager.util.transparentListItemColors

@Composable
fun AppliedPatchesDialog(
    onDismissRequest: () -> Unit,
    appliedPatches: PatchSelection?,
    patchBundles: List<InstalledPatchBundle>,
) {
    FullscreenDialog(onDismissRequest = onDismissRequest) {
        NexoraOfficialBackdrop(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    NexoraOfficialPageHeader(
                        title = stringResource(R.string.applied_patches),
                        subtitle = stringResource(R.string.patches),
                        backLabel = stringResource(R.string.back),
                        onBackClick = onDismissRequest,
                    )
                },
            ) { paddingValues ->
                LazyColumnWithScrollbar(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {
                    val patches = appliedPatches ?: return@LazyColumnWithScrollbar
                    val bundleMap = patchBundles.associateBy { it.bundleUid }

                    patches.forEach { (bundleUid, patchNames) ->
                        val bundle = bundleMap[bundleUid]
                        item(key = "header_$bundleUid") {
                            PatchesListHeader(
                                title = bundle?.let {
                                    it.bundleVersion?.let { version ->
                                        "${it.bundleName} v$version"
                                    } ?: it.bundleName
                                } ?: "${stringResource(R.string.patches)} $bundleUid",
                            )
                        }

                        items(
                            items = patchNames.sorted(),
                            key = { "${bundleUid}_$it" },
                        ) { patchName ->
                            ListItem(
                                headlineContent = { Text(patchName) },
                                colors = transparentListItemColors,
                            )
                        }
                    }
                }
            }
        }
    }
}