package app.revanced.manager.ui.component.patcher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.revanced.manager.R
import app.revanced.manager.ui.component.ArrowButton
import app.revanced.manager.ui.component.LoadingIndicator
import app.revanced.manager.ui.component.NexoraOfficialBorder
import app.revanced.manager.ui.component.NexoraOfficialCyan
import app.revanced.manager.ui.component.NexoraOfficialGreen
import app.revanced.manager.ui.component.NexoraOfficialMuted
import app.revanced.manager.ui.component.NexoraOfficialPanelStrong
import app.revanced.manager.ui.component.NexoraOfficialText
import app.revanced.manager.ui.component.NexoraOfficialViolet
import app.revanced.manager.ui.model.State
import app.revanced.manager.ui.model.StepCategory
import app.revanced.manager.ui.model.Step
import java.util.Locale
import kotlin.math.floor

// Credits: https://github.com/Aliucord/AliucordManager/blob/main/app/src/main/kotlin/com/aliucord/manager/ui/component/installer/InstallGroup.kt
@Composable
fun Steps(
    category: StepCategory,
    steps: List<Step>,
    isExpanded: Boolean = false,
    onExpand: () -> Unit,
    onClick: () -> Unit
) {
    val state = remember(steps) {
        when {
            steps.all { it.state == State.COMPLETED } -> State.COMPLETED
            steps.any { it.state == State.FAILED } -> State.FAILED
            steps.any { it.state == State.RUNNING } -> State.RUNNING
            else -> State.WAITING
        }
    }

    val filteredSteps = remember(steps) {
        val failedCount = steps.count { it.state == State.FAILED }

        steps.filter { step ->
            // Show hidden steps if it's the only failed step.
            !step.hide || (step.state == State.FAILED && failedCount == 1)
        }
    }

    LaunchedEffect(state) {
        if (state == State.RUNNING || state == State.FAILED)
            onExpand()
    }

    val accentColor = when (state) {
        State.COMPLETED -> NexoraOfficialGreen
        State.FAILED -> MaterialTheme.colorScheme.error
        State.RUNNING -> NexoraOfficialCyan
        State.WAITING -> NexoraOfficialMuted
    }
    val statusText = stringResource(
        when (state) {
            State.COMPLETED -> R.string.step_completed
            State.FAILED -> R.string.step_failed
            State.RUNNING -> R.string.step_running
            State.WAITING -> R.string.step_waiting
        }
    )
    val phaseMarker = String.format(Locale.ROOT, "%02d", StepCategory.entries.indexOf(category) + 1)
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(22.dp))
            .fillMaxWidth()
            .background(NexoraOfficialPanelStrong)
            .border(
                1.dp,
                if (state == State.RUNNING) NexoraOfficialCyan.copy(alpha = 0.55f) else NexoraOfficialBorder,
                RoundedCornerShape(22.dp),
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .clickable(true, onClick = onClick)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = accentColor.copy(alpha = 0.10f),
                border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.30f)),
            ) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = phaseMarker,
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColor,
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(category.displayName),
                    style = MaterialTheme.typography.titleMedium,
                    color = NexoraOfficialText,
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                )
            }

            Text(
                text = "${filteredSteps.count { it.state == State.COMPLETED }}/${filteredSteps.size}",
                style = MaterialTheme.typography.labelSmall,
                color = NexoraOfficialMuted,
            )

            ArrowButton(modifier = Modifier.size(24.dp), expanded = isExpanded, onClick = null)
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .background(Color(0xCC030816))
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                filteredSteps.forEachIndexed { index, step ->
                    val (progress, progressText) = step.progress?.let { (current, total) ->
                        if (total != null) current.toFloat() / total.toFloat() to "${current.megaBytes}/${total.megaBytes} MB"
                        else null to "${current.megaBytes} MB"
                    } ?: (null to null)

                    SubStep(
                        name = step.title,
                        state = step.state,
                        message = step.message,
                        progress = progress,
                        progressText = progressText,
                        isFirst = index == 0,
                        isLast = index == filteredSteps.lastIndex,
                    )
                }
            }
        }
    }
}

@Composable
fun SubStep(
    name: String,
    state: State,
    message: String? = null,
    progress: Float? = null,
    progressText: String? = null,
    isFirst: Boolean = false,
    isLast: Boolean = false,
) {
    var messageExpanded by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .run {
                if (message != null)
                    clickable { messageExpanded = !messageExpanded }
                else this
            }
            .padding(top = if (isFirst) 10.dp else 8.dp, bottom = if (isLast) 20.dp else 8.dp)
            .padding(horizontal = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StepIcon(
                size = 18.dp,
                state = state,
                progress = progress,
            )

            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, true),
            )

            when {
                message != null -> Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ArrowButton(
                        modifier = Modifier.size(20.dp),
                        expanded = messageExpanded,
                        onClick = null
                    )
                }

                progressText != null -> Text(
                    progressText,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        AnimatedVisibility(visible = messageExpanded && message != null) {
            Text(
                text = message.orEmpty(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(horizontal = 36.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun StepIcon(state: State, progress: Float? = null, size: Dp) {
    val strokeWidth = Dp(floor(size.value / 10) + 1)

    Crossfade(targetState = state, label = "State CrossFade") { state ->
        when (state) {
            State.COMPLETED -> Icon(
                Icons.Filled.CheckCircle,
                contentDescription = stringResource(R.string.step_completed),
                tint = NexoraOfficialGreen,
                modifier = Modifier.size(size)
            )

            State.FAILED -> Icon(
                Icons.Filled.Cancel,
                contentDescription = stringResource(R.string.step_failed),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(size)
            )

            State.WAITING -> Icon(
                Icons.Outlined.Circle,
                contentDescription = stringResource(R.string.step_waiting),
                tint = NexoraOfficialBorder,
                modifier = Modifier.size(size)
            )

            State.RUNNING -> {
                LoadingIndicator(
                    modifier = stringResource(R.string.step_running).let { description ->
                        Modifier
                            .size(size)
                            .semantics {
                                contentDescription = description
                            }
                    },
                    progress = { progress },
                    color = NexoraOfficialCyan,
                    trackColor = NexoraOfficialBorder,
                    strokeWidth = strokeWidth,
                )
            }
        }
    }
}

private val Long.megaBytes get() = "%.1f".format(locale = Locale.ROOT, toDouble() / 1_000_000)