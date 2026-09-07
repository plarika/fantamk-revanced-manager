package app.revanced.manager.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private val NexoraViolet = Color(0xFF9B4DFF)
private val NexoraPurple = Color(0xFF6B2BFF)
private val NexoraCyan = Color(0xFF00D8FF)
private val NexoraBlue = Color(0xFF1677FF)
private val NexoraPanel = Color(0xFF0D1324)
private val NexoraPanelAlt = Color(0xFF11192D)

@Composable
fun NexoraNeonBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                listOf(
                    Color(0xFF080B17),
                    Color(0xFF060A14),
                    Color(0xFF050812),
                )
            )
        )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NexoraPurple.copy(alpha = 0.16f),
                            Color.Transparent,
                        ),
                        radius = 1100f,
                    )
                )
        )
        content()
    }
}

@Composable
fun NexoraLogoBadge(
    painter: Painter,
    modifier: Modifier = Modifier,
    size: Int = 54,
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        NexoraPurple.copy(alpha = 0.95f),
                        NexoraViolet.copy(alpha = 0.85f),
                        NexoraBlue.copy(alpha = 0.75f),
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(listOf(NexoraViolet, NexoraCyan)),
                shape = RoundedCornerShape(18.dp),
            )
            .padding(6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.size((size - 12).dp),
        )
    }
}

@Composable
fun NexoraHeroCard(
    title: String,
    subtitle: String,
    primaryStat: String,
    primaryLabel: String,
    secondaryStat: String,
    secondaryLabel: String,
    logo: Painter,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(30.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF15143B),
                        Color(0xFF24104D),
                        Color(0xFF071F3E),
                    )
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(listOf(NexoraViolet, NexoraCyan)),
                shape,
            )
            .padding(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                NexoraLogoBadge(painter = logo, size = 66)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFC7C7DA),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                NexoraStatPill(
                    value = primaryStat,
                    label = primaryLabel,
                    modifier = Modifier.weight(1f),
                    accent = NexoraCyan,
                )
                NexoraStatPill(
                    value = secondaryStat,
                    label = secondaryLabel,
                    modifier = Modifier.weight(1f),
                    accent = NexoraViolet,
                )
            }
            if (actionLabel != null && onAction != null) {
                Surface(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.Transparent,
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    listOf(NexoraPurple, NexoraViolet, NexoraCyan)
                                )
                            )
                            .padding(horizontal = 18.dp, vertical = 13.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = actionLabel,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NexoraStatPill(
    value: String,
    label: String,
    modifier: Modifier,
    accent: Color,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xAA10172B))
            .border(1.dp, accent.copy(alpha = 0.38f), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
     ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(50))
                .background(accent)
        )
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFB8BCD1),
            )
        }
    }
}

@Composable
fun NexoraFeatureTile(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    accent: Color = NexoraPurple,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(24.dp)
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Box(
        modifier = modifier
            .then(clickModifier)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        NexoraPanelAlt,
                        accent.copy(alpha = 0.18f),
                        NexoraPanel,
                    )
                )
            )
            .border(1.dp, accent.copy(alpha = 0.55f), shape)
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = accent.copy(alpha = 0.2f),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (accent == NexoraPurple) NexoraViolet else accent,
                    modifier = Modifier.padding(10.dp).size(24.dp),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFADB3CA),
            )
        }
    }
}

@Composable
fun NexoraSourceStatus(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFF063B37))
            .border(1.dp, Color(0xFF00E5B0).copy(alpha = 0.55f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF00E5B0))
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFB7FFE9),
        )
    }
}

@Composable
fun NexoraLibraryHero(
    title: String,
    subtitle: String,
    patchCount: Int,
    logo: Painter,
    onSync: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(30.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(listOf(Color(0xFF1A103B), Color(0xFF291057), Color(0xFF062B48))))
            .border(1.dp, Brush.linearGradient(listOf(NexoraViolet, NexoraCyan)), shape)
            .padding(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                NexoraLogoBadge(painter = logo, size = 62)
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.headlineSmall, color = Color.White)
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFC9CAE0))
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$patchCount patches", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Surface(onClick = onSync, shape = RoundedCornerShape(18.dp), color = NexoraPurple) {
                    Text("Sincronizar agora", modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp), style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            }
        }
    }
}