package lorry.folder.items.dossiersigma.headless.usecases.homePage

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.headless.domain.SigmaPath
import lorry.folder.items.dossiersigma.headless.favoriteObservation.service.FilesAccessibleChannel
import lorry.folder.items.dossiersigma.ui.dialogs.HomeItemInfos
import lorry.folder.items.dossiersigma.ui.settings.SettingsManager
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val homeUseCase: HomeUseCase,
    val context: Context,
    val settings: SettingsManager,
    val filesAccessibleCommunicator: FilesAccessibleChannel
) : ViewModel() {

//    @Inject lateinit var settings: SettingsManager

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _homePageVisible = MutableStateFlow<Boolean>(true)
    val homePageVisible: StateFlow<Boolean> = _homePageVisible

    fun setHomePageVisible(visible: Boolean) {
        _homePageVisible.value = visible
    }

    fun toggleHomePageVisible() {
        _homePageVisible.value = !homePageVisible.value
    }

    ///////////////////////////////////////////////////////
    // envoi signal fin accès datastore au daemonService //
    ///////////////////////////////////////////////////////
    companion object{
        val homeReady = MutableSharedFlow<Unit>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
    }

    ///////////////////////////////////////////
    // envoi itemInfos à dialogHomeItemInfos //
    ///////////////////////////////////////////
    private val _dialogHomeItemInfos = MutableStateFlow<HomeItemInfos?>(null)
    val dialogHomeItemInfos: StateFlow<HomeItemInfos?> = _dialogHomeItemInfos

    fun setDialogHomeItemInfos(infos: HomeItemInfos?) {
        _dialogHomeItemInfos.value = infos
    }

    fun setHomeItems(items: List<HomeItem>) {
        _uiState.update { uiState ->
            if (uiState is HomeUiState.Ready) {
                uiState.copy(items = items)
            } else
                uiState
        }
    }

    fun addHomeItem(item: HomeItem) {
        _uiState.update { uiState ->
            if (uiState is HomeUiState.Ready) {
                val existings = uiState.items.toMutableList()
                existings.add(item)
                uiState.copy(items = existings)
            } else
                uiState
        }
    }

    fun removeHomeItem(item: HomeItem) {
        _uiState.update { uiState ->
            if (uiState is HomeUiState.Ready) {
                val existings = uiState.items.toMutableList()
                existings.remove(item)
                uiState.copy(items = existings)
            } else
                uiState
        }
    }

    fun StateFlow<List<HomeItem>>.clear() {
        _uiState.update { uiState ->
            if (uiState is HomeUiState.Ready) {
                uiState.copy(items = emptyList())
            } else
                uiState
        }
    }

    init {
        viewModelScope.launch {
            refreshAfterPermission()
        }
    }

    private suspend fun computeElements() {
        try {
            // On bascule sur un thread I/O pour la tâche longue (c'est correct).
            val homeItemsFromSettings = withContext(Dispatchers.IO) {
                settings.homeItemsFlow.firstOrNull() ?: emptyList()
            }

            // De retour sur le thread principal automatiquement.
            // On peut maintenant mettre à jour notre StateFlow en toute sécurité.
            val homeItemsList = homeItemsFromSettings.map {
                HomeItem(
                    title = it.newTitle ?: "",
                    path = it.path ?: SigmaPath(""),
                    icon = 0,
                    picture = it.picture
                )
            }

            //fin charge disque: le daemonService peut commencer sa collecte le cas échéant
            homeReady.tryEmit(Unit)

            _uiState.value = HomeUiState.Ready(settings, homeItemsList)
            //            setHomeItems(homeItemsList)
        } catch (e: Exception) {
            _uiState.value = HomeUiState.Error(e)
        }
    }

    fun refreshAfterPermission() {
        viewModelScope.launch {
            filesAccessibleCommunicator.isActivated.collect {
                computeElements()
                return@collect
            }
        }
    }
}

data class HomeItem(
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val path: SigmaPath,
    @DrawableRes val icon: Int = 0,
    val picture: Bitmap? = null,
    val index: Int = 0
)

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Ready(val settings: SettingsManager, val items: List<HomeItem>) : HomeUiState()
    data class Error(val cause: Throwable) : HomeUiState()
}




