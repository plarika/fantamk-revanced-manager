package app.revanced.manager.ui.screen.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.revanced.manager.R
import app.revanced.manager.ui.component.AppScaffold
import app.revanced.manager.ui.component.AppTopBar
import app.revanced.manager.ui.component.Scrollbar
import app.revanced.manager.ui.component.NexoraOfficialBackground
import app.revanced.manager.ui.component.NexoraOfficialText
import app.revanced.manager.ui.component.NexoraOfficialViolet
import app.revanced.manager.ui.component.NexoraPageScaffold
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.m3.chipColors
import com.mikepenz.aboutlibraries.ui.compose.m3.libraryColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesSettingsScreen(
    onBackClick: () -> Unit,
) {
    NexoraPageScaffold(
        title = stringResource(R.string.opensource_licenses),
        subtitle = stringResource(R.string.opensource_licenses_description),
        onBackClick = onBackClick,
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            val lazyListState = rememberLazyListState()
            val libraries by produceLibraries(R.raw.aboutlibraries)
            val chipColors = LibraryDefaults.chipColors(
                containerColor = NexoraOfficialViolet,
                contentColor = NexoraOfficialText,
            )

            LibrariesContainer(
                modifier = Modifier
                    .fillMaxSize(),
                libraries = libraries,
                lazyListState = lazyListState,
                colors = LibraryDefaults.libraryColors(
                    libraryBackgroundColor = NexoraOfficialBackground,
                    libraryContentColor = NexoraOfficialText,
                    versionChipColors = chipColors,
                    licenseChipColors = chipColors,
                    fundingChipColors = chipColors,
                )
            )
            Scrollbar(lazyListState = lazyListState, modifier = Modifier.padding(paddingValues))
        }
    }
}