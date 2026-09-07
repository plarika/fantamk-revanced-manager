package app.revanced.manager.ui.screen.settings.update

import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.revanced.manager.BuildConfig
import app.revanced.manager.R
import app.revanced.manager.ui.component.ColumnWithScrollbar
import app.revanced.manager.ui.component.ConfirmDialog
import app.revanced.manager.ui.component.NexoraHeroCard
import app.revanced.manager.ui.component.NexoraLogoBadge
import app.revanced.manager.ui.component.NexoraNeonBackdrop
import app.revanced.manager.ui.component.TooltipIconButton
import app.revanced.manager.ui.viewmodel.UpdatesSettingsViewModel
import app.revanced.manager.util.relativeTime
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private val UpdatePurple = Color(0xFF8B3DFF)
private val UpdateViolet = Color(0xFFB36BFF)
private val UpdateCyan = Color(0xFF00D8FF)
private val UpdatePanel = Color(0xFF0D1324)
private val UpdatePanelAlt = Color(0xFF11182B)

@Composable
private fun NexoraUpdateToggleCard(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    warning: Boolean = false,
) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        UpdatePanelAlt,
                        if (warning) Color(0xFF35151F) else UpdatePurple.copy(alpha = 0.11f),
                        UpdatePanel,
                    )
                )
            )
            .border(
                1.dp,
                if (warning) Color(0xFFFF6F78).copy(alpha = 0.45f)
                else UpdateViolet.copy(alpha = 0.28f),
                shape,
            )
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (warning) Color(0xFF57202D) else UpdatePurple.copy(alpha = 0.18f),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (warning) Color(0xFFFFA0A8) else UpdateCyan,
                    modifier = Modifier.padding(12.dp).size(26.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (warning) Color(0xFFFFA0A8) else Color(0xFFB9BED0),
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun NexoraUpdateActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(UpdatePanelAlt, Color(0xFF181239), Color(0xFF0A2337))
                    )
                )
                .border(1.dp, UpdateViolet.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(shape = RoundedCornerShape(16.dp), color = UpdatePurple.copy(alpha = 0.2f)) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = UpdateCyan,
                    modifier = Modifier.padding(12.dp).size(26.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFB9BED0))
            }
            Text("›", style = MaterialTheme.typography.headlineSmall, color = UpdateViolet)
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
        drawable = remember(context) { AppCompatResources.getDrawable(context, R.drawable.ic_logo_ring) }
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

    val statusText = when {
        hasUpdate -> stringResource(R.string.nexora_metric_updates_available)
        managerVersion != null -> stringResource(R.string.nexora_metric_updates_current)
        else -> stringResource(R.string.nexora_updates_not_checked)
    }
    val latestVersionText = managerVersion ?: "—"
    val releaseAge = updateReleasedAt?.relativeTime(context)
    val changelogSubtitle = releaseAge?.let {
        stringResource(R.string.nexora_updates_release_age, it)
    } ?: stringResource(R.string.nexora_updates_changelog_subtitle)

    NexoraNeonBackdrop(modifier = Modifier.fillMaxSize()) {
        ColumnWithScrollbar(modifier = Modifier.fillMaxSize(), state = scrollState) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(30.dp),
                    color = Color(0xE6111728),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TooltipIconButton(onClick = onBackClick, tooltip = stringResource(R.string.back)) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = Color.White,
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        NexoraLogoBadge(painter = appIcon, size = 54)
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.updates),
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                            )
                            Text(
                                stringResource(R.string.nexora_updates_header_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFBCC1D3),
                            )
                        }
                    }
                }

                NexoraHeroCard(
                    title = stringResource(R.string.nexora_app_name),
                    subtitle = statusText,
                    primaryStat = BuildConfig.VERSION_NAME,
                    primaryLabel = stringResource(R.string.nexora_updates_installed),
                    secondaryStat = latestVersionText,
                    secondaryLabel = stringResource(R.string.nexora_updates_latest),
                    logo = appIcon,
                    actionLabel = stringResource(
                        when {
                            checkingForUpdate -> R.string.update_check
                            hasUpdate -> R.string.view_update
                            else -> R.string.manual_update_check
                        }
                    ),
                    onAction = ::runUpdateAction,
                )

                NexoraUpdateActionCard(
                    icon = Icons.Outlined.WorkOutline,
                    title = stringResource(R.string.changelog),
                    subtitle = changelogSubtitle,
                    onClick = onChangelogClick,
                )

                Text(
                    stringResource(R.string.nexora_updates_preferences),
                    style = MaterialTheme.typography.titleMedium,
                    color = UpdateViolet,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )

                NexoraUpdateToggleCard(
                    icon = Icons.Filled.Update,
                    title = stringResource(R.string.update_checking_manager),
                    description = stringResource(R.string.update_checking_manager_description),
                    checked = managerAutoUpdates,
                    onCheckedChange = { value ->
                        coroutineScope.launch { vm.managerAutoUpdates.update(value) }
                    },
                )

                AnimatedVisibility(visible = managerAutoUpdates) {
                    NexoraUpdateToggleCard(
                        icon = Icons.Filled.Notifications,
                        title = stringResource(R.string.show_manager_update_dialog_on_launch),
                        description = stringResource(R.string.show_manager_update_dialog_on_launch_description),
                        checked = showManagerUpdateDialogOnLaunch,
                        onCheckedChange = { value ->
                            coroutineScope.launch { vm.showManagerUpdateDialogOnLaunch.update(value) }
                        },
                    )
                }

                NexoraUpdateToggleCard(
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