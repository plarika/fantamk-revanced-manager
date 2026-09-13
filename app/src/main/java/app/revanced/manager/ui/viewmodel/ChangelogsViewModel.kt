package app.revanced.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import app.revanced.manager.domain.repository.ChangelogSource
import app.revanced.manager.domain.repository.ChangelogsRepository
import app.revanced.manager.domain.repository.ManagerUpdateRepository
import app.revanced.manager.network.api.LegacyPatchApi
import app.revanced.manager.network.dto.RemoteAssetHistory
import kotlinx.coroutines.flow.Flow

class ChangelogsViewModel(
    private val api: LegacyPatchApi,
    private val managerUpdateRepository: ManagerUpdateRepository,
    private val source: ChangelogSource,
) : ViewModel() {
    val changelogs: Flow<PagingData<RemoteAssetHistory>> = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { ChangelogsRepository(api, managerUpdateRepository, source) }
    ).flow.cachedIn(viewModelScope)
}