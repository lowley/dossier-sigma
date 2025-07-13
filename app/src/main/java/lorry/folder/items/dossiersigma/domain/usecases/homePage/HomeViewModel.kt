package lorry.folder.items.dossiersigma.domain.usecases.homePage

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.ui.SigmaViewModel
import lorry.folder.items.dossiersigma.ui.components.HomeItemInfos
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val homeUseCase: HomeUseCase
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
        addHomeItem(
            HomeItem(
                title = "Filles",
                icon = R.drawable.sexe,
                path = "/storage/emulated/0/Movies/sexe/filles",
            )
        )

        addHomeItem(
            HomeItem(
                title = "Fantasmes",
                icon = R.drawable.sexe,
                path = "/storage/emulated/0/Movies/sexe/fantasmes"
            )
        )

        addHomeItem(
            HomeItem(
                title = "Films",
                icon = R.drawable.film,
                path = "/storage/emulated/0/Movies"
            )
        )

        addHomeItem(
            HomeItem(
                title = "Bizarre",
                icon = R.drawable.film,
                path = "/storage/emulated/0/Movies/bizarre"
            )
        )

        addHomeItem(
            HomeItem(
                title = "sensations fortes",
                icon = R.drawable.film,
                path = "/storage/emulated/0/Movies/sensations fortes.fort"
            )
        )

        addHomeItem(
            HomeItem(
                title = "humour",
                icon = R.drawable.film,
                path = "/storage/emulated/0/Movies/humour"
            )
        )

        addHomeItem(
            HomeItem(
                title = "fantastique",
                icon = R.drawable.film,
                path = "/storage/emulated/0/Movies/fantastique"
            )
        )

        addHomeItem(
            HomeItem(
                title = "intériorité",
                icon = R.drawable.film,
                path = "/storage/emulated/0/Movies/faiblesse & intériorité"
            )
        )

        addHomeItem(
            HomeItem(
                title = "acteurs",
                icon = R.drawable.acteur,
                path = "/storage/emulated/0/Movies/acteurs"
            )
        )

        addHomeItem(
            HomeItem(
                title = "1DM+",
                icon = R.drawable.sexe,
                path = "/storage/emulated/0/Download/1DMP/General"
            )
        )

        addHomeItem(
            HomeItem(
                title = "Nzbs",
                icon = R.drawable.downloads2,
                path = "/storage/emulated/0/Download/nzb"
            )
        )

        addHomeItem(
            HomeItem(
                title = "Films/Sexe",
                icon = R.drawable.sexe,
                path = "/storage/emulated/0/Movies/sexe"
            )
        )

        addHomeItem(
            HomeItem(
                title = "Stockage principal",
                icon = R.drawable.hdd,
                path = "/storage/emulated/0"
            )
        )

        addHomeItem(
            HomeItem(
                title = "Téléchargements",
                icon = R.drawable.downloads2,
                path = "/storage/emulated/0/Download"
            )
        )
    }
}

data class HomeItem(
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val path: String,
    @DrawableRes val icon: Int = 0,
    val picture: Bitmap? = null
)





