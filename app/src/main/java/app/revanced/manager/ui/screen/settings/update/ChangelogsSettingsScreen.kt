package app.revanced.manager.ui.screen.settings.update

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.paging.compose.collectAsLazyPagingItems
import app.revanced.manager.R
import app.revanced.manager.domain.repository.ChangelogSource
import app.revanced.manager.ui.component.AppTopBar
import app.revanced.manager.ui.component.ChangelogList
import app.revanced.manager.ui.component.NexoraPageScaffold
import app.revanced.manager.ui.viewmodel.ChangelogsViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogsSettingsScreen(
    source: ChangelogSource,
    onBackClick: () -> Unit,
    vm: ChangelogsViewModel = koinViewModel { parametersOf(source) }
) {
    val changelogs = vm.changelogs.collectAsLazyPagingItems()

    NexoraPageScaffold(
        title = stringResource(R.string.changelog),
        subtitle = stringResource(R.string.changelog_description),
        onBackClick = onBackClick,
    ) { paddingValues ->
        ChangelogList(changelogs = changelogs, modifier = Modifier.padding(paddingValues))
    }
}