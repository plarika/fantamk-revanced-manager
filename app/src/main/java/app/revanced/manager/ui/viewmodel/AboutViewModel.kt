package app.revanced.manager.ui.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.lifecycle.ViewModel
import app.revanced.manager.domain.manager.PreferencesManager
import app.revanced.manager.network.dto.ProjectSocial
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Brands
import compose.icons.fontawesomeicons.brands.Github

class AboutViewModel(
    prefs: PreferencesManager,
) : ViewModel() {
    val socials = listOf(
        ProjectSocial("GitHub", PROJECT_URL, preferred = true),
    )
    val contact: String? = null
    val donate: String? = null

    val showDeveloperSettings = prefs.showDeveloperSettings

    companion object {
        const val DEVELOPER_OPTIONS_TAPS = 5
        private const val PROJECT_URL = "https://github.com/plarika/nexora-manager"

        fun getSocialIcon(name: String) = when (name) {
            "GitHub" -> FontAwesomeIcons.Brands.Github
            else -> Icons.Outlined.Language
        }
    }
}
