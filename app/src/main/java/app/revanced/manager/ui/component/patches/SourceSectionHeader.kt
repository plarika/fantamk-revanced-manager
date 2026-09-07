package app.revanced.manager.ui.component.patches

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Source
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import app.revanced.manager.R
import app.revanced.manager.patcher.patch.PatchBundleInfo
import app.revanced.manager.ui.component.NexoraSourceStatus
import app.revanced.manager.ui.component.TooltipIconButton
import app.revanced.manager.ui.component.haptics.HapticTriStateCheckbox
import app.revanced.manager.util.relativeTime

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SourceSectionHeader(
    bundle: PatchBundleInfo.Scoped,
    expanded: Boolean,
    selectionState: Boolean?,
    onClick: () -> Unit,
    onSelectionClick: () -> Unit,
    onExpandToggle: () -> Unit,
    onDeleteClick: () -> Unit,
    sourceEditMode: Boolean,
    readOnly: Boolean,
    loadIssue: String?
) {
    val toggleableState = when (selectionState) {
        true -> ToggleableState.On
        false -> ToggleableState.Off
        null -> ToggleableState.Indeterminate
    }
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 0f else -90f,
        animationSpec = tween(durationMillis = 250, easing = EaseInOut),
        label = "Bundle section expand state"
    )
    val isNexora = bundle.name.contains("Nexora", ignoreCase = true)
    val cardShape = RoundedCornerShape(26.dp)

    Column {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .border(
                    width = 1.dp,
                    color = if (isNexora) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)
                    },
                    shape = cardShape,
                ),
            shape = cardShape,
            color = if (isNexora) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.16f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
            tonalElevation = if (isNexora) 5.dp else 2.dp
        ) {
            ListItem(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            leadingContent = {
                if (readOnly) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isNexora) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isNexora) {
                                Image(
                                    painter = painterResource(R.drawable.ic_logo_ring),
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Source,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                } else {
                    HapticTriStateCheckbox(
                        state = toggleableState,
                        onClick = onSelectionClick,
                        enabled = true
                    )
                }
            },
            headlineContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = bundle.name,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (readOnly && loadIssue == null && bundle.version != null) {
                        NexoraSourceStatus(stringResource(R.string.nexora_source_ready))
                    }
                }
            },
            supportingContent = {
                val patchCount = bundle.patches.size
                val version = bundle.version?.takeIf { it.isNotBlank() }
                val releasedAt = bundle.releasedAt?.relativeTime(LocalContext.current)
                if (version == null && loadIssue == null) return@ListItem

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    version?.let {
                        Text(
                            text = listOf(
                                // Show release date only when view-only
                                if (readOnly && releasedAt != null) "v$it\u2002($releasedAt)"
                                else it,
                                pluralStringResource(
                                    R.plurals.patch_count,
                                    patchCount,
                                    patchCount
                                )
                            ).joinToString("\u2002\u2022\u2002"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    loadIssue?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            trailingContent = {
                if (sourceEditMode) {
                    TooltipIconButton(
                        onClick = onDeleteClick,
                        enabled = bundle.uid != 0,
                        tooltip = stringResource(R.string.delete)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.delete)
                        )
                    }
                } else {
                    TooltipIconButton(
                        onClick = onExpandToggle,
                        tooltip = stringResource(
                            if (expanded) R.string.collapse_content else R.string.expand_content
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = stringResource(
                                if (expanded) R.string.collapse_content else R.string.expand_content
                            ),
                            modifier = Modifier.rotate(arrowRotation)
                        )
                    }
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = androidx.compose.ui.graphics.Color.Transparent
            )
            )
        }
    }
}