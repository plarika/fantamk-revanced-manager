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
import androidx.compose.ui.text.input.PasswordVisualTransformation
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

    ListSection(title = "FantaMK private patches") {
        SettingsListItem(
            headlineContent = "Private patches source",
            supportingContent = if (viewModel.isConfigured) {
                "Connected. Updates automatically when Manager starts."
            } else {
                "Connect this Manager to the private FantaMK patch repository."
            },
            onClick = { showTokenDialog = true }
        )

        if (viewModel.isConfigured) {
            SettingsListItem(
                headlineContent = "Update now",
                supportingContent = "Check and download the latest FantaMK LKG patches.",
                onClick = viewModel::refresh
            )
            SettingsListItem(
                headlineContent = "Disconnect private source",
                supportingContent = "Remove the stored credential and FantaMK source.",
                onClick = viewModel::disconnect
            )
        }

        viewModel.status?.let {
            SettingsListItem(
                headlineContent = "Status",
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
            title = { Text("Connect FantaMK private patches") },
            text = {
                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    label = { Text("GitHub fine-grained token") },
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
                    Text("Save and connect")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    token = ""
                    showTokenDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}