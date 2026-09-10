package app.revanced.manager.ui.screen

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import app.revanced.manager.BuildConfig
import app.revanced.manager.R
import app.revanced.manager.domain.manager.PreferencesManager
import app.revanced.manager.ui.component.ColumnWithScrollbar
import app.revanced.manager.ui.component.NotificationCard
import app.revanced.manager.ui.component.NotificationCardType
import app.revanced.manager.ui.component.NexoraOfficialBackdrop
import app.revanced.manager.ui.component.NexoraOfficialBackground
import app.revanced.manager.ui.component.NexoraOfficialBorder
import app.revanced.manager.ui.component.NexoraOfficialCyan
import app.revanced.manager.ui.component.NexoraOfficialPanelStrong
import app.revanced.manager.ui.component.NexoraOfficialText
import app.revanced.manager.ui.component.NexoraOfficialMuted
import app.revanced.manager.ui.component.NexoraOfficialViolet
import app.revanced.manager.ui.component.TooltipIconButton
import app.revanced.manager.ui.model.navigation.Settings
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import org.koin.compose.koinInject

private data class Section(
    @param:StringRes val name: Int,
    @param:StringRes val description: Int,
    val image: ImageVector,
    val destination: Settings.Destination,
)

@Composable
private fun NexoraSettingsCard(
    section: Section,
    supportingText: String,
    supportingColor: Color = Color.Unspecified,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = NexoraOfficialPanelStrong,
        border = BorderStroke(1.dp, NexoraOfficialBorder),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = NexoraOfficialViolet.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, NexoraOfficialViolet.copy(alpha = 0.38f)),
            ) {
                Icon(
                    imageVector = section.image,
                    contentDescription = null,
                    tint = NexoraOfficialCyan,
                    modifier = Modifier.padding(10.dp).size(22.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(section.name),
                    style = MaterialTheme.typography.titleSmall,
                    color = NexoraOfficialText,
                )
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (supportingColor == Color.Unspecified) {
                        NexoraOfficialMuted
                    } else supportingColor,
                )
            }
            Text(
                text = "›",
                style = MaterialTheme.typography.titleLarge,
                color = NexoraOfficialViolet,
            )
        }
    }
}

@SuppressLint("BatteryLife")
@Composable
fun SettingsScreen(onBackClick: () -> Unit, navigate: (Settings.Destination) -> Unit) {
    val prefs: PreferencesManager = koinInject()
    val showDeveloperSettings by prefs.showDeveloperSettings.getAsState()
    val disablePatchVersionCompatCheck by prefs.disablePatchVersionCompatCheck.getAsState()
    val disableSelectionWarning by prefs.disableSelectionWarning.getAsState()
    val disableUniversalPatchCheck by prefs.disableUniversalPatchCheck.getAsState()
    val suggestedVersionSafeguard by prefs.suggestedVersionSafeguard.getAsState()
    val safeguardsToggled by remember(
        disablePatchVersionCompatCheck,
        disableSelectionWarning,
        disableUniversalPatchCheck,
        suggestedVersionSafeguard,
    ) {
        derivedStateOf {
            disablePatchVersionCompatCheck ||
                disableSelectionWarning ||
                disableUniversalPatchCheck ||
                !suggestedVersionSafeguard
        }
    }

    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val powerManager = remember(context) { context.getSystemService<PowerManager>()!! }
    var showBatteryOptimizationsWarning by remember {
        mutableStateOf(!powerManager.isIgnoringBatteryOptimizations(context.packageName))
    }
    val batteryOptimizationsLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            showBatteryOptimizationsWarning = !powerManager.isIgnoringBatteryOptimizations(context.packageName)
        }
    val appIcon = rememberDrawablePainter(
        drawable = remember(context) {
            AppCompatResources.getDrawable(context, R.drawable.ic_logo_ring)
        }
    )

    val generalSections = remember {
        listOf(
            Section(R.string.general, R.string.general_description, Icons.Outlined.Settings, Settings.General),
            Section(R.string.updates, R.string.updates_description, Icons.Outlined.Update, Settings.Updates),
            Section(R.string.downloads, R.string.downloads_description, Icons.Outlined.Download, Settings.Downloads),
        )
    }
    val advancedSections = remember {
        listOf(
            Section(R.string.import_export, R.string.import_export_description, Icons.Outlined.SwapVert, Settings.ImportExport),
            Section(R.string.advanced, R.string.advanced_description, Icons.Outlined.Tune, Settings.Advanced),
        )
    }
    val developerSection = remember(showDeveloperSettings) {
        Section(
            R.string.developer_options,
            R.string.developer_options_description,
            Icons.Outlined.Code,
            Settings.Developer,
        ).takeIf { showDeveloperSettings }
    }

    NexoraOfficialBackdrop(modifier = Modifier.fillMaxSize()) {
        Scaffold(containerColor = Color.Transparent) { paddingValues ->
        ColumnWithScrollbar(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            state = scrollState,
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF111827),
                    border = BorderStroke(1.dp, Color(0xFF1F2937)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TooltipIconButton(
                            onClick = onBackClick,
                            tooltip = stringResource(R.string.back),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Image(
                            painter = appIcon,
                            contentDescription = stringResource(R.string.nexora_app_name),
                            modifier = Modifier.size(42.dp),
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.nexora_settings_title),
                                style = MaterialTheme.typography.titleLarge,
                                color = NexoraOfficialText,
                            )
                            Text(
                                text = stringResource(R.string.nexora_settings_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = NexoraOfficialViolet,
                            )
                        }
                    }
                }
                if (showBatteryOptimizationsWarning) {
                    NotificationCard(
                        type = NotificationCardType.WARNING,
                        icon = Icons.Default.BatteryAlert,
                        text = stringResource(R.string.battery_optimization_notification),
                        onClick = {
                            batteryOptimizationsLauncher.launch(
                                Intent(
                                    android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                    Uri.fromParts("package", context.packageName, null),
                                )
                            )
                        },
                    )
                }

                Text(
                    text = stringResource(R.string.nexora_settings_essential),
                    style = MaterialTheme.typography.titleMedium,
                    color = NexoraOfficialViolet,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                generalSections.forEach { section ->
                    NexoraSettingsCard(
                        section = section,
                        supportingText = stringResource(section.description),
                        onClick = { navigate(section.destination) },
                    )
                }
                Text(
                    text = stringResource(R.string.nexora_settings_system),
                    style = MaterialTheme.typography.titleMedium,
                    color = NexoraOfficialViolet,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                advancedSections.forEach { section ->
                    val hasSafeguardWarning = section.destination == Settings.Advanced && safeguardsToggled
                    NexoraSettingsCard(
                        section = section,
                        supportingText = if (hasSafeguardWarning) {
                            stringResource(R.string.nexora_safeguards_changed)
                        } else {
                            stringResource(section.description)
                        },
                        supportingColor = if (hasSafeguardWarning) {
                            MaterialTheme.colorScheme.error
                        } else {
                            Color.Unspecified
                        },
                        onClick = { navigate(section.destination) },
                    )
                }
                developerSection?.let { section ->
                    NexoraSettingsCard(
                        section = section,
                        supportingText = stringResource(section.description),
                        onClick = { navigate(section.destination) },
                    )
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navigate(Settings.About) },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF111827),
                    border = BorderStroke(1.dp, Color(0xFF1F2937)),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = appIcon,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.nexora_app_name),
                                style = MaterialTheme.typography.titleMedium,
                                color = NexoraOfficialText,
                            )
                            Text(
                                text = BuildConfig.VERSION_NAME,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9CA3AF),
                            )
                        }
                        Text(
                            text = "›",
                            style = MaterialTheme.typography.headlineSmall,
                            color = NexoraOfficialViolet,
                        )
                    }
                }
            }
        }
    }
    }
}
