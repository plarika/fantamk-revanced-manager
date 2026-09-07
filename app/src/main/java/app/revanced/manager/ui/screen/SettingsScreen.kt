package app.revanced.manager.ui.screen

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
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
import androidx.compose.material.icons.outlined.Downloadimport androidx.compose.material.icons.outlined.Settings
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
import app.revanced.manager.ui.component.TooltipIconButtonimport app.revanced.manager.ui.model.navigation.Settings
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
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Icon(
                    imageVector = section.image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(14.dp).size(28.dp),
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(section.name),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (supportingColor == Color.Unspecified) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else supportingColor,
                )
            }
            Text(
                text = "›",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )        }
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
    val context = LocalContext.current    val powerManager = remember(context) { context.getSystemService<PowerManager>()!! }
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
    val developerSection = remember(showDeveloperSettings) {        Section(
            R.string.developer_options,
            R.string.developer_options_description,
            Icons.Outlined.Code,
            Settings.Developer,
        ).takeIf { showDeveloperSettings }
    }

    Scaffold { paddingValues ->
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
                    shape = RoundedCornerShape(34.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),                        verticalAlignment = Alignment.CenterVertically,
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
                            modifier = Modifier.size(58.dp),
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.nexora_settings_title),
                                style = MaterialTheme.typography.headlineSmall,
                            )
                            Text(
                                text = stringResource(R.string.nexora_settings_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    color = MaterialTheme.colorScheme.primary,
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
                    color = MaterialTheme.colorScheme.primary,
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
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = appIcon,
                            contentDescription = null,
                            modifier = Modifier.size(52.dp),
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.nexora_app_name),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                text = BuildConfig.VERSION_NAME,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }                        Text(
                            text = "›",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
        }
    }
}
