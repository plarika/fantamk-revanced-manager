package app.revanced.manager.ui.component

import android.content.pm.PackageInfo
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val PatchingCard = Color(0xFF111827)
private val PatchingInner = Color(0xFF020617)
private val PatchingBorder = Color(0xFF1F2937)
private val PatchingPurple = Color(0xFF785CFF)
private val PatchingLavender = Color(0xFFA78BFA)
@Composable
fun NexoraPatchingAppHeader(
    appInfo: PackageInfo?,
    placeholderLabel: String,
    version: String,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(18.dp),
        color = PatchingCard,
        border = BorderStroke(1.dp, PatchingBorder),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppIcon(
                appInfo,
                contentDescription = null,
                modifier = Modifier.size(58.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                AppLabel(
                    appInfo,
                    style = MaterialTheme.typography.titleMedium,
                    defaultText = placeholderLabel,
                )
                Text(
                    text = version,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = PatchingPurple.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, PatchingLavender.copy(alpha = 0.40f)),
            ) {
                Text(
                    text = version,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = PatchingLavender,
                )
            }
        }
    }
}

@Composable
fun NexoraPatchingOption(
    title: String,
    description: String,
    enabled: Boolean = true,
    warningDescription: String? = null,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        color = PatchingInner,
        border = BorderStroke(1.dp, PatchingBorder),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                warningDescription?.let {
                    Text(
                        text = "(!) $it",
                        style = MaterialTheme.typography.labelSmall,
                        color = PatchingLavender,
                    )
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = PatchingLavender,
            )
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
        color = PatchingPurple.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, PatchingLavender.copy(alpha = 0.32f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.WarningAmber,
                contentDescription = null,
                tint = PatchingLavender,
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
fun NexoraPatchingDialog(
    title: String,
    description: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialogExtended(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = PatchingCard,
        tonalElevation = 0.dp,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Surface(
                onClick = onConfirm,
                shape = RoundedCornerShape(999.dp),
                color = PatchingPurple,
            ) {
                Text(
                    text = confirmText,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                )
            }
        },
    )
}