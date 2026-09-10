package app.revanced.manager.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

val NexoraOfficialBackground = Color(0xFF02040C)
val NexoraOfficialPanel = Color(0xE60A1224)
val NexoraOfficialPanelStrong = Color(0xF20B1428)
val NexoraOfficialViolet = Color(0xFF9B5CFF)
val NexoraOfficialPurple = Color(0xFF6D43FF)
val NexoraOfficialCyan = Color(0xFF39D9FF)
val NexoraOfficialBlue = Color(0xFF3B82F6)
val NexoraOfficialGreen = Color(0xFF36E6A5)
val NexoraOfficialAmber = Color(0xFFFFB84A)
val NexoraOfficialText = Color(0xFFF5F7FF)
val NexoraOfficialMuted = Color(0xFFAAB4D6)
val NexoraOfficialBorder = Color(0xFF233B69)

private val officialAccentBrush = Brush.linearGradient(
    listOf(NexoraOfficialCyan, NexoraOfficialViolet, NexoraOfficialPurple)
)

@Composable
fun NexoraOfficialBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF030713), NexoraOfficialBackground, Color(0xFF010208))
                )
            )
            .drawBehind {
                val stars = listOf(
                    .08f to .12f, .19f to .07f, .33f to .15f, .47f to .05f,
                    .61f to .13f, .74f to .08f, .88f to .18f, .95f to .06f,
                    .13f to .31f, .39f to .26f, .57f to .34f, .82f to .28f,
                    .23f to .51f, .68f to .47f, .91f to .58f, .45f to .69f,
                )
                stars.forEachIndexed { index, point ->
                    drawCircle(
                        color = if (index % 3 == 0) NexoraOfficialCyan.copy(.38f)
                        else NexoraOfficialViolet.copy(.32f),
                        radius = if (index % 4 == 0) 2.2f else 1.25f,
                        center = Offset(size.width * point.first, size.height * point.second),
                    )
                }
            }
    ) {
        Box(
            Modifier
                .size(310.dp)
                .align(Alignment.TopEnd)
                .offset(x = 120.dp, y = (-86).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFF342078),
                            Color(0xFF111B50),
                            Color(0xFF05081A),
                        )
                    )
                )
                .border(1.dp, NexoraOfficialViolet.copy(.65f), CircleShape)
        )
        Box(
            Modifier
                .size(360.dp)
                .align(Alignment.TopEnd)
                .offset(x = 145.dp, y = (-105).dp)
                .border(2.dp, NexoraOfficialCyan.copy(.12f), CircleShape)
        )
        content()
    }
}

@Composable
fun NexoraOfficialBrandHeader(
    logo: Painter,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NexoraLogoBadge(painter = logo, size = 58)
            Column {
                Text(
                    text = "N E X O R A",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NexoraOfficialText,
                )
                Text(
                    text = "M A N A G E R",
                    style = MaterialTheme.typography.labelSmall,
                    color = NexoraOfficialMuted,
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = NexoraOfficialText,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = NexoraOfficialMuted,
        )
    }
}

@Composable
fun NexoraOfficialPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(officialAccentBrush)
            .padding(1.dp)
            .clip(shape)
            .background(NexoraOfficialPanelStrong)
    ) {
        content()
    }
}

@Composable
fun NexoraOfficialSectionTitle(
    overline: String,
    title: String,
    trailing: String? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(width = 3.dp, height = 30.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(officialAccentBrush)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp),
        ) {
            Text(
                text = overline.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = NexoraOfficialViolet,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = NexoraOfficialText,
            )
        }
        trailing?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = NexoraOfficialCyan,
            )
        }
    }
}

@Composable
fun NexoraOfficialMetric(
    value: String,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    accent: Color = NexoraOfficialViolet,
) {
    Column(
        modifier = modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(icon, null, tint = accent, modifier = Modifier.size(23.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, color = NexoraOfficialText)
        Text(label, style = MaterialTheme.typography.labelSmall, color = NexoraOfficialMuted)
    }
}

@Composable
fun NexoraOfficialActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = NexoraOfficialViolet,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 116.dp),
        shape = RoundedCornerShape(18.dp),
        color = NexoraOfficialPanel,
        border = BorderStroke(1.dp, accent.copy(.55f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(13.dp),
                color = accent.copy(.12f),
                border = BorderStroke(1.dp, accent.copy(.38f)),
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.padding(9.dp).size(22.dp),
                )
            }
            Text(title, style = MaterialTheme.typography.titleSmall, color = NexoraOfficialText)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = NexoraOfficialMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun NexoraOfficialStatusPill(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = NexoraOfficialGreen,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = accent.copy(.10f),
        border = BorderStroke(1.dp, accent.copy(.55f)),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = accent,
        )
    }
}
@Composable
fun NexoraOfficialPageHeader(
    title: String,
    subtitle: String,
    backLabel: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            onClick = onBackClick,
            shape = RoundedCornerShape(14.dp),
            color = NexoraOfficialPanelStrong,
            border = BorderStroke(1.dp, NexoraOfficialViolet.copy(.48f)),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = backLabel,
                tint = NexoraOfficialViolet,
                modifier = Modifier.padding(10.dp).size(22.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "N E X O R A",
                style = MaterialTheme.typography.labelSmall,
                color = NexoraOfficialViolet,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = NexoraOfficialText,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = NexoraOfficialMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(
            Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(NexoraOfficialViolet)
                .border(3.dp, NexoraOfficialViolet.copy(.18f), CircleShape)
        )
    }
}
@Composable
fun NexoraOfficialProgressPanel(
    progress: Float,
    title: String,
    status: String,
    modifier: Modifier = Modifier,
) {
    val safeProgress = progress.coerceIn(0f, 1f)
    NexoraOfficialPanel(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "N E X O R A",
                        style = MaterialTheme.typography.labelMedium,
                        color = NexoraOfficialViolet,
                    )
                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelSmall,
                        color = NexoraOfficialMuted,
                    )
                }
                Text(
                    text = "${(safeProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = NexoraOfficialText,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color(0xFF111B35))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(safeProgress)
                        .height(10.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(officialAccentBrush)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = NexoraOfficialCyan,
            )
        }
    }
}