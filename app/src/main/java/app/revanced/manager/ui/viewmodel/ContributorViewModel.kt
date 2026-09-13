package app.revanced.manager.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.revanced.manager.domain.repository.NexoraProjectService
import app.revanced.manager.network.dto.ProjectRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContributorViewModel(private val projectService: NexoraProjectService) : ViewModel() {
    var repositories: List<ProjectRepository>? by mutableStateOf(null)
    	private set

    init {
        viewModelScope.launch {
            repositories = withContext(Dispatchers.IO) {
                runCatching { projectService.getContributors() }.getOrDefault(emptyList())
            }
        }
    }
}