package app.revanced.manager.ui.screen.settings.update

import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.revanced.manager.BuildConfig
import app.revanced.manager.R
import app.revanced.manager.domain.repository.isNewerVersion
import app.revanced.manager.ui.component.ColumnWithScrollbar
import app.revanced.manager.ui.component.ConfirmDialog
import app.revanced.manager.ui.component.NexoraLogoBadge
import app.revanced.manager.ui.component.TooltipIconButton
import app.revanced.manager.ui.viewmodel.UpdatesSettingsViewModel
import app.revanced.manager.util.relativeTime
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private val CompactBackground = Color(0xFF050509)
private val CompactCard = Color(0xFF111827)
private val CompactInner = Color(0xFF020617)
private val CompactBorder = Color(0xFF1F2937)
private val CompactPurple = Color(0xFF785CFF)
private val CompactLavender = Color(0xFFA78BFA)
private val CompactSecondary = Color(0xFF9CA3AF)
private val CompactGreen = Color(0xFF22C55E)

@Composable
private fun CompactVersionMetric(
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = CompactInner,
        border = BorderStroke(1.dp, CompactBorder),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = accent,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = CompactSecondary,
            )
        }
    }
}

@Composable
private fun CompactPrimaryAction(
    text: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = CompactPurple,
        border = BorderStroke(1.dp, CompactLavender.copy(alpha = 0.45f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun CompactUpdateToggleCard(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    warning: Boolean = false,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CompactCard,
        border = BorderStroke(
            1.dp,
            if (warning) MaterialTheme.colorScheme.error.copy(alpha = 0.45f) else CompactBorder,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CompactInner,
                border = BorderStroke(1.dp, CompactBorder),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (warning) MaterialTheme.colorScheme.error else CompactLavender,
                    modifier = Modifier.padding(9.dp).size(21.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFF9FAFB),
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (warning) MaterialTheme.colorScheme.error else CompactSecondary,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}

@Composable
private fun CompactUpdateActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CompactCard,
        border = BorderStroke(1.dp, CompactBorder),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CompactInner,
                border = BorderStroke(1.dp, CompactBorder),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CompactLavender,
                    modifier = Modifier.padding(9.dp).size(21.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFF9FAFB),
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = CompactSecondary,
                )
            }
            Text(
                text = "›",
                style = MaterialTheme.typography.titleLarge,
                color = CompactLavender,
            )
        }
    }
}

@Composable
fun UpdatesSettingsScreen(
    onBackClick: () -> Unit,
    onChangelogClick: () -> Unit,
    onUpdateClick: () -> Unit,
    vm: UpdatesSettingsViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var checkingForUpdate by rememberSaveable { mutableStateOf(false) }
    var showPrereleaseWarning by rememberSaveable { mutableStateOf(false) }

    val managerVersion by vm.managerVersion.collectAsStateWithLifecycle()
    val hasUpdate by vm.hasUpdate.collectAsStateWithLifecycle()
    val updateReleasedAt by vm.updateReleasedAt.collectAsStateWithLifecycle()
    val managerAutoUpdates by vm.managerAutoUpdates.getAsState()
    val showManagerUpdateDialogOnLaunch by vm.showManagerUpdateDialogOnLaunch.getAsState()
    val useManagerPrereleases by vm.useManagerPrereleases.getAsState()

    val appIcon = rememberDrawablePainter(
        drawable = remember(context) {
            AppCompatResources.getDrawable(context, R.drawable.ic_logo_ring)
        },
    )

    fun runUpdateAction() {
        if (checkingForUpdate) return
        coroutineScope.launch {
            if (hasUpdate) {
                onUpdateClick()
                return@launch
            }
            checkingForUpdate = true
            try {
                if (vm.checkUpdates()) onUpdateClick()
            } finally {
                checkingForUpdate = false
            }
        }
    }

    fun updatePrereleasePreference(value: Boolean) {
        coroutineScope.launch {
            vm.useManagerPrereleases.update(value)
            vm.clearAvailableManagerUpdate()
            vm.checkUpdates(false)
        }
    }

    if (showPrereleaseWarning) {
        ConfirmDialog(
            onDismiss = { showPrereleaseWarning = false },
            onConfirm = {
                updatePrereleasePreference(!useManagerPrereleases)
                showPrereleaseWarning = false
            },
            title = stringResource(R.string.prerelease_title),
            description = stringResource(R.string.prereleases_warning),
            icon = Icons.Outlined.WarningAmber,
        )
    }

    val installedAheadOfPublished = managerVersion?.let { publishedVersion ->
        isNewerVersion(BuildConfig.VERSION_NAME, publishedVersion)
    } == true
    val statusText = when {
        hasUpdate -> stringResource(R.string.nexora_metric_updates_available)
        installedAheadOfPublished -> stringResource(R.string.nexora_updates_installed_ahead)
        managerVersion != null -> stringResource(R.string.nexora_metric_updates_current)
        else -> stringResource(R.string.nexora_updates_not_checked)
    }
    val latestVersionText = managerVersion ?: "—"
    val latestVersionLabel = stringResource(
        if (installedAheadOfPublished) {
            R.string.nexora_updates_published_channel
        } else {
            R.string.nexora_updates_latest
        },
    )
    val releaseAge = updateReleasedAt?.relativeTime(context)
    val changelogSubtitle = releaseAge?.let {
        stringResource(R.string.nexora_updates_release_age, it)
    } ?: stringResource(R.string.nexora_updates_changelog_subtitle)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF111827), CompactBackground, Color.Black),
                ),
            ),
    ) {
        ColumnWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            state = scrollState,
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = CompactCard,
                    border = BorderStroke(1.dp, CompactBorder),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        TooltipIconButton(
                            onClick = onBackClick,
                            tooltip = stringResource(R.string.back),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = Color(0xFFF9FAFB),
                            )
                        }
                        NexoraLogoBadge(painter = appIcon, size = 40)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.updates),
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFFF9FAFB),
                            )
                            Text(
                                text = stringResource(R.string.nexora_updates_header_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = CompactLavender,
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = CompactCard,
                    border = BorderStroke(1.dp, CompactBorder),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(9.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            NexoraLogoBadge(painter = appIcon, size = 36)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.nexora_app_name),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFFF9FAFB),
                                )
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = when {
                                        hasUpdate -> CompactLavender
                                        managerVersion != null -> CompactGreen
                                        else -> CompactSecondary
                                    },
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            CompactVersionMetric(
                                value = BuildConfig.VERSION_NAME,
                                label = stringResource(R.string.nexora_updates_installed),
                                accent = Color(0xFFF9FAFB),
                                modifier = Modifier.weight(1f),
                            )
                            CompactVersionMetric(
                                value = latestVersionText,
                                label = latestVersionLabel,
                                accent = CompactLavender,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        CompactPrimaryAction(
                            text = stringResource(
                                when {
                                    checkingForUpdate -> R.string.update_check
                                    hasUpdate -> R.string.view_update
                                    else -> R.string.manual_update_check
                                },
                            ),
                            onClick = ::runUpdateAction,
                        )
                    }
                }

                CompactUpdateActionCard(
                    icon = Icons.Outlined.WorkOutline,
                    title = stringResource(R.string.changelog),
                    subtitle = changelogSubtitle,
                    onClick = onChangelogClick,
                )

                Text(
                    text = stringResource(R.string.nexora_updates_preferences),
                    style = MaterialTheme.typography.titleMedium,
                    color = CompactLavender,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                )

                CompactUpdateToggleCard(
                    icon = Icons.Filled.Update,
                    title = stringResource(R.string.update_checking_manager),
                    description = stringResource(R.string.update_checking_manager_description),
                    checked = managerAutoUpdates,
                    onCheckedChange = { value ->
                        coroutineScope.launch { vm.managerAutoUpdates.update(value) }
                    },
                )

                AnimatedVisibility(visible = managerAutoUpdates) {
                    CompactUpdateToggleCard(
                        icon = Icons.Filled.Notifications,
                        title = stringResource(R.string.show_manager_update_dialog_on_launch),
                        description = stringResource(R.string.show_manager_update_dialog_on_launch_description),
                        checked = showManagerUpdateDialogOnLaunch,
                        onCheckedChange = { value ->
                            coroutineScope.launch {
                                vm.showManagerUpdateDialogOnLaunch.update(value)
                            }
                        },
                    )
                }

                CompactUpdateToggleCard(
                    icon = Icons.Outlined.WarningAmber,
                    title = stringResource(R.string.manager_prereleases),
                    description = stringResource(R.string.manager_prereleases_description),
                    checked = useManagerPrereleases,
                    warning = useManagerPrereleases,
                    onCheckedChange = { value ->
                        if (value != vm.useManagerPrereleases.default) {
                            showPrereleaseWarning = true
                        } else {
                            updatePrereleasePreference(value)
                        }
                    },
                )
            }
        }
    }
}
