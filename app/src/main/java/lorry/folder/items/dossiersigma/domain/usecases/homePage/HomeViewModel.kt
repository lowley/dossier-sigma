package lorry.folder.items.dossiersigma.domain.usecases.homePage

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.ui.components.HomeItemInfos
import lorry.folder.items.dossiersigma.ui.settings.SettingsManager
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val homeUseCase: HomeUseCase,
    val settingsManager: SettingsManager
) : ViewModel() {

    private val _homePageVisible = MutableStateFlow<Boolean>(true)
    val homePageVisible: StateFlow<Boolean> = _homePageVisible

    fun setHomePageVisible(visible: Boolean) {
        _homePageVisible.value = visible
    }

    ///////////////////////////////////////////
    // envoi itemInfos à dialogHomeItemInfos //
    ///////////////////////////////////////////
    private val _dialogHomeItemInfos = MutableStateFlow<HomeItemInfos?>(null)
    val dialogHomeItemInfos: StateFlow<HomeItemInfos?> = _dialogHomeItemInfos

    fun setDialogHomeItemInfos(infos: HomeItemInfos?) {
        _dialogHomeItemInfos.value = infos
    }

    private val _homeItems = MutableStateFlow<List<HomeItem>>(emptyList())
    val homeItems: StateFlow<List<HomeItem>> = _homeItems

    fun setHomeItems(items: List<HomeItem>) {
        _homeItems.value = items
    }

    fun addHomeItem(item: HomeItem) {
        _homeItems.value = homeItems.value + item
    }

    fun removeHomeItem(item: HomeItem) {
        val existings = homeItems.value.toMutableList()
        existings.remove(item)
        _homeItems.value = existings
    }

    fun StateFlow<List<HomeItem>>.replaceWith(newItem: HomeItem) {
        val existings = homeItems.value.toMutableList()
        existings.removeIf { item -> item.id == newItem.id }
        existings.add(newItem)
        _homeItems.value = existings
    }

    fun StateFlow<List<HomeItem>>.clear() {
        _homeItems.value = emptyList<HomeItem>()
    }

    init {
        viewModelScope.launch {
            // On bascule sur un thread I/O pour la tâche longue (c'est correct).
            val homeItemsFromSettings = withContext(Dispatchers.IO) {
                settingsManager.homeItemsFlow.firstOrNull() ?: emptyList()
            }

            // De retour sur le thread principal automatiquement.
            // On peut maintenant mettre à jour notre StateFlow en toute sécurité.
            val homeItemsList = homeItemsFromSettings.map {
                HomeItem(
                    title = it.newTitle ?: "",
                    path = it.path ?: "",
                    icon = 0,
                    picture = it.picture
                )
            }
            setHomeItems(homeItemsList)
        }
    }
}

data class HomeItem(
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val path: String,
    @DrawableRes val icon: Int = 0,
    val picture: Bitmap? = null
)





