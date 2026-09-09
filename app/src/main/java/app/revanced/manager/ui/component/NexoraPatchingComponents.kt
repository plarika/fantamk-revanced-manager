package app.revanced.manager.ui.component

import android.content.pm.PackageInfo
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val NexoraBackground = Color(0xFF050509)
private val NexoraCard = Color(0xFF111827)
private val NexoraInner = Color(0xFF020617)
private val NexoraBorder = Color(0xFF1F2937)
private val NexoraPurple = Color(0xFF785CFF)
private val NexoraLavender = Color(0xFFA78BFA)

@Composable
fun NexoraFlowTopBar(
    title: String,
    backContentDescription: String,
    onBackClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = NexoraBackground,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                onClick = onBackClick,
                shape = RoundedCornerShape(12.dp),
                color = NexoraInner,
                border = BorderStroke(1.dp, NexoraBorder),
            ) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = backContentDescription,
                        tint = NexoraLavender,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "NEXORA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.6.sp,
                    ),
                    color = NexoraLavender,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = NexoraPurple.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, NexoraLavender.copy(alpha = 0.30f)),
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(NexoraLavender)
                    )
                }
            }
        }
    }
}

@Composable
fun NexoraPatchingAppHeader(
    appInfo: PackageInfo?,
    placeholderLabel: String,
    version: String,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        color = NexoraCard,
        border = BorderStroke(1.dp, NexoraLavender.copy(alpha = 0.22f)),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = NexoraInner,
                border = BorderStroke(1.dp, NexoraBorder),
            ) {
                Box(
                    modifier = Modifier.size(66.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    AppIcon(
                        appInfo,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                AppLabel(
                    appInfo,
                    style = MaterialTheme.typography.titleMedium,
                    defaultText = placeholderLabel,
                )
                Text(
                    text = placeholderLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.height(5.dp))
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = NexoraPurple.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, NexoraLavender.copy(alpha = 0.34f)),
                ) {
                    Text(
                        text = version,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = NexoraLavender,
                    )
                }
            }
        }
    }
}

@Composable
fun NexoraPatchingOption(
    marker: String,
    title: String,
    description: String,
    enabled: Boolean = true,
    warningDescription: String? = null,
    onClick: () -> Unit,
) {
    val border = if (warningDescription != null) {
        NexoraLavender.copy(alpha = 0.38f)
    } else {
        NexoraBorder
    }

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = NexoraCard,
        border = BorderStroke(1.dp, border),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
      ) {
            Surface(
                shape = RoundedCornerShape(11.dp),
                color = NexoraPurple.copy(alpha = if (enabled) 0.13f else 0.05f),
                border = BorderStroke(1.dp, NexoraLavender.copy(alpha = 0.24f)),
            ) {
                Box(
                    modifier = Modifier.size(38.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = marker,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (enabled) NexoraLavender else MaterialTheme.colorScheme.outline,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                warningDescription?.let {
                    Text(
                        text = "(!) $it",
                        style = MaterialTheme.typography.labelSmall,
                        color = NexoraLavender,
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = NexoraInner,
                border = BorderStroke(1.dp, NexoraBorder),
            ) {
                Box(
                    modifier = Modifier.size(34.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = if (enabled) NexoraLavender else MaterialTheme.colorScheme.outline,
                    )
                }
            }
      }
    }
}

@Composable
fun NexoraInlineWarning(text: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        color = NexoraPurple.copy(alpha = 0.07f),
        border = BorderStroke(1.dp, NexoraLavender.copy(alpha = 0.28f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
            Icon(
                imageVector = Icons.Outlined.WarningAmber,
                contentDescription = null,
                tint = NexoraLavender,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun NexoraPipelineProgress(progress: Float) {
    val safeProgress = progress.coerceIn(0f, 1f)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        color = NexoraCard,
        border = BorderStroke(1.dp, NexoraBorder),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "NEXORA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.4.sp,
                    ),
                    color = NexoraLavender,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${(safeProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(NexoraBorder),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(safeProgress)
                        .height(6.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(NexoraLavender),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexoraPatchingDialog(
    title: String,
    description: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = NexoraCard,
            border = BorderStroke(1.dp, NexoraLavender.copy(alpha = 0.28f)),
            tonalElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "NEXORA",
                        style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                          letterSpacing = 1.6.sp,
                        ),
                        color = NexoraLavender,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(NexoraLavender)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Surface(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = NexoraInner,
                        border = BorderStroke(1.dp, NexoraBorder),
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = dismissText,
                              style = MaterialTheme.typography.labelLarge,
                              color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Surface(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = NexoraPurple,
                        border = BorderStroke(1.dp, NexoraLavender.copy(alpha = 0.42f)),
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = confirmText,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                            )
                        }
                    }
                }
            }
        }
    }
}