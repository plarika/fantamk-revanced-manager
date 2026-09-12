package app.revanced.manager.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.SettingsBackupRestore
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.revanced.manager.R
import app.revanced.manager.data.room.apps.installed.InstallType
import app.revanced.manager.ui.component.AppInfo
import app.revanced.manager.ui.component.ColumnWithScrollbar
import app.revanced.manager.ui.component.NexoraOfficialAmber
import app.revanced.manager.ui.component.NexoraOfficialBackdrop
import app.revanced.manager.ui.component.NexoraOfficialCyan
import app.revanced.manager.ui.component.NexoraOfficialGreen
import app.revanced.manager.ui.component.NexoraOfficialMuted
import app.revanced.manager.ui.component.NexoraOfficialPageHeader
import app.revanced.manager.ui.component.NexoraOfficialPanel
import app.revanced.manager.ui.component.NexoraOfficialPanelStrong
import app.revanced.manager.ui.component.NexoraOfficialText
import app.revanced.manager.ui.component.NexoraOfficialViolet
import app.revanced.manager.ui.viewmodel.InstalledAppInfoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstalledAppInfoScreen(
    onPatchClick: (packageName: String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: InstalledAppInfoViewModel
) {

    SideEffect {
        viewModel.onBackClick = onBackClick
    }

    var showUninstallDialog by rememberSaveable { mutableStateOf(false) }
    var showAppliedPatchesDialog by rememberSaveable { mutableStateOf(false) }

    if (showUninstallDialog)
        UninstallDialog(
            onDismiss = { showUninstallDialog = false },
            onConfirm = { viewModel.uninstall() }
        )
    if (showAppliedPatchesDialog) {
        AppliedPatchesDialog(
            onDismissRequest = { showAppliedPatchesDialog = false },
            appliedPatches = viewModel.appliedPatches,
            patchBundles = viewModel.patchBundles
        )
    }

    val installedApp = viewModel.installedApp

    NexoraOfficialBackdrop(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                NexoraOfficialPageHeader(
                    title = stringResource(R.string.app_info),
                    subtitle = installedApp?.currentPackageName ?: stringResource(R.string.app_info),
                    backLabel = stringResource(R.string.back),
                    onBackClick = onBackClick,
                )
            },
            containerColor = Color.Transparent,
        ) { paddingValues ->
            ColumnWithScrollbar(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                val app = installedApp ?: return@ColumnWithScrollbar

                NexoraOfficialPanel(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    AppInfo(viewModel.appInfo) {
                        Text(
                            app.version,
                            color = NexoraOfficialMuted,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        if (app.installType == InstallType.MOUNT) {
                            Text(
                                text = if (viewModel.isMounted) stringResource(R.string.mounted)
                                else stringResource(R.string.not_mounted),
                                color = if (viewModel.isMounted) NexoraOfficialGreen else NexoraOfficialAmber,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        NexoraInstalledAction(
                            icon = Icons.AutoMirrored.Outlined.OpenInNew,
                            text = stringResource(R.string.open_app),
                            accent = NexoraOfficialCyan,
                            onClick = viewModel::launch,
                        )
                        NexoraInstalledAction(
                            icon = Icons.Outlined.Update,
                            text = stringResource(R.string.repatch),
                            accent = NexoraOfficialViolet,
                            enabled = app.installType != InstallType.MOUNT || viewModel.rootInstaller.hasRootAccess(),
                            onClick = { onPatchClick(app.originalPackageName) },
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        when (app.installType) {
                            InstallType.DEFAULT -> NexoraInstalledAction(
                                icon = Icons.Outlined.Delete,
                                text = stringResource(R.string.uninstall),
                                accent = NexoraOfficialAmber,
                                onClick = viewModel::uninstall,
                            )

                            InstallType.MOUNT -> {
                                NexoraInstalledAction(
                                    icon = Icons.Outlined.SettingsBackupRestore,
                                    text = stringResource(R.string.unpatch),
                                    accent = NexoraOfficialAmber,
                                    enabled = viewModel.rootInstaller.hasRootAccess(),
                                    onClick = { showUninstallDialog = true },
                                )
                                NexoraInstalledAction(
                                    icon = Icons.Outlined.Circle,
                                    text = if (viewModel.isMounted) stringResource(R.string.unmount)
                                    else stringResource(R.string.mount),
                                    accent = NexoraOfficialCyan,
                                    enabled = viewModel.rootInstaller.hasRootAccess(),
                                    onClick = viewModel::mountOrUnmount,
                                )
                            }
                        }
                    }
                }

                NexoraOfficialPanel(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        val patchCount = viewModel.appliedPatches?.values?.sumOf { it.size } ?: 0
                        NexoraInstalledDetailRow(
                            label = stringResource(R.string.applied_patches),
                            value = pluralStringResource(R.plurals.patch_count, patchCount, patchCount),
                            onClick = { showAppliedPatchesDialog = true },
                        )
                        NexoraInstalledDetailRow(
                            label = stringResource(R.string.package_name),
                            value = app.currentPackageName,
                        )
                        if (app.originalPackageName != app.currentPackageName) {
                            NexoraInstalledDetailRow(
                                label = stringResource(R.string.original_package_name),
                                value = app.originalPackageName,
                            )
                        }
                        NexoraInstalledDetailRow(
                            label = stringResource(R.string.install_type),
                            value = stringResource(app.installType.stringResource),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.NexoraInstalledAction(
    icon: ImageVector,
    text: String,
    accent: Color,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .weight(1f)
            .heightIn(min = 82.dp),
        shape = RoundedCornerShape(18.dp),
        color = if (enabled) NexoraOfficialPanelStrong else NexoraOfficialPanelStrong.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, accent.copy(alpha = if (enabled) 0.55f else 0.18f)),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = if (enabled) accent else NexoraOfficialMuted.copy(alpha = 0.5f),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = if (enabled) NexoraOfficialText else NexoraOfficialMuted.copy(alpha = 0.55f),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun NexoraInstalledDetailRow(
    label: String,
    value: String,
    onClick: (() -> Unit)? = null,
) {
    val rowModifier = Modifier
        .fillMaxWidth()
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(horizontal = 14.dp, vertical = 11.dp)

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = NexoraOfficialMuted,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = NexoraOfficialText,
                maxLines = 2,
            )
        }
        if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = label,
                tint = NexoraOfficialViolet,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UninstallDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) = AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.unpatch_app), color = NexoraOfficialText) },
    text = { Text(stringResource(R.string.unpatch_description), color = NexoraOfficialMuted) },
    containerColor = NexoraOfficialPanelStrong,
    confirmButton = {
        TextButton(
            onClick = {
                onConfirm()
                onDismiss()
            },
            shapes = ButtonDefaults.shapes()
        ) {
            Text(stringResource(R.string.ok), color = NexoraOfficialViolet)
        }
    },
    dismissButton = {
        TextButton(
            onClick = onDismiss, shapes = ButtonDefaults.shapes()
        ) {
            Text(stringResource(R.string.cancel), color = NexoraOfficialMuted)
        }
    }
)