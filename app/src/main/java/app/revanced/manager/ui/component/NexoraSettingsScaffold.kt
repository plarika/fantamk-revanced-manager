package app.revanced.manager.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import app.revanced.manager.R

@Composable
fun NexoraPageScaffold(
    title: String,
    subtitle: String,
    onBackClick: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    NexoraOfficialBackdrop(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                NexoraOfficialPageHeader(
                    title = title,
                    subtitle = subtitle,
                    backLabel = stringResource(R.string.back),
                    onBackClick = onBackClick,
                    actions = actions,
                )
            },
            bottomBar = bottomBar,
            floatingActionButton = floatingActionButton,
            snackbarHost = snackbarHost,
            containerColor = Color.Transparent,
            content = content,
        )
    }
}

@Composable
fun NexoraSettingsScaffold(
    title: String,
    subtitle: String,
    onBackClick: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) = NexoraPageScaffold(
    title = title,
    subtitle = subtitle,
    onBackClick = onBackClick,
    actions = actions,
    bottomBar = bottomBar,
    floatingActionButton = floatingActionButton,
    snackbarHost = snackbarHost,
    content = content,
)
