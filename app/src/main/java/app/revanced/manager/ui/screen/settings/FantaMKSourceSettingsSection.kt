package app.revanced.manager.ui.screen.settings

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import app.revanced.manager.R
import app.revanced.manager.ui.component.ListSection
import app.revanced.manager.ui.component.settings.SettingsListItem
import app.revanced.manager.ui.viewmodel.FantaMKSettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun FantaMKSourceSettingsSection(
    viewModel: FantaMKSettingsViewModel = koinViewModel()
) {
    var showTokenDialog by rememberSaveable { mutableStateOf(false) }
    var token by rememberSaveable { mutableStateOf("") }

    ListSection(title = stringResource(R.string.fantamk_private_patches)) {
        SettingsListItem(
            headlineContent = stringResource(R.string.fantamk_private_source),
            supportingContent = if (viewModel.isConfigured) {
                stringResource(R.string.fantamk_private_source_connected)
            } else {
                stringResource(R.string.fantamk_private_source_disconnected)
            },
            onClick = { showTokenDialog = true }
        )

        if (viewModel.isConfigured) {
            SettingsListItem(
                headlineContent = stringResource(R.string.fantamk_update_now),
                supportingContent = stringResource(R.string.fantamk_update_now_description),
                onClick = viewModel::refresh
            )
            SettingsListItem(
                headlineContent = stringResource(R.string.fantamk_disconnect),
                supportingContent = stringResource(R.string.fantamk_disconnect_description),
                onClick = viewModel::disconnect
            )
        }

        viewModel.status?.let {
            SettingsListItem(
                headlineContent = stringResource(R.string.fantamk_status),
                supportingContent = it,
                onClick = {}
            )
        }
        if (viewModel.isBusy) {
            CircularProgressIndicator()
        }
    }

    if (showTokenDialog) {
        AlertDialog(
            onDismissRequest = {
                token = ""
                showTokenDialog = false
            },
            title = { Text(stringResource(R.string.fantamk_connect_title)) },
            text = {
                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    label = { Text(stringResource(R.string.fantamk_token_label)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
            },
            confirmButton = {
                TextButton(
                    enabled = token.isNotBlank() && !viewModel.isBusy,
                    onClick = {
                        val submitted = token
                        token = ""
                        showTokenDialog = false
                        viewModel.saveToken(submitted)
                    }
                ) {
                    Text(stringResource(R.string.fantamk_save_connect))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    token = ""
                    showTokenDialog = false
                }) {
                    Text(stringResource(R.string.fantamk_cancel))
                }
            }
        )
    }
}
