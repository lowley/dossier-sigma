package lorry.folder.items.dossiersigma.domain.usecases.homePage

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.ui.SigmaViewModel
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val homeUseCase: HomeUseCase
) : ViewModel() {

    private val _homePageVisible = MutableStateFlow<Boolean>(false)
    val homePageVisible: StateFlow<Boolean> = _homePageVisible

    fun setHomePageVisible(visible: Boolean) {
        _homePageVisible.value = visible
    }

    private val _homeItems = MutableStateFlow<List<HomeItem>>(emptyList())
    val homeItems: StateFlow<List<HomeItem>> = _homeItems

    fun StateFlow<List<HomeItem>>.set(items: List<HomeItem>) {
        _homeItems.value = items
    }

    fun StateFlow<List<HomeItem>>.add(item: HomeItem) {
        _homeItems.value = homeItems.value + item
    }

    fun StateFlow<List<HomeItem>>.remove(item: HomeItem) {
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
        homeItems.add(
            HomeItem(
            title = "Stockage principal",
            icon = R.drawable.hdd,
            onClick = { mainVM, homeVM ->
                homeVM.setHomePageVisible(false)
                mainVM.addFolderPathToHistory("/storage/emulated/0")
            }
        ))

        homeItems.add(
            HomeItem(
            title = "Téléchargements",
            icon = R.drawable.downloads2,
            onClick = { mainVM, homeVM ->
                homeVM.setHomePageVisible(false)
                mainVM.addFolderPathToHistory("/storage/emulated/0/Download")
            }
        ))

        homeItems.add(
            HomeItem(
                title = "Films",
                icon = R.drawable.film,
                onClick = { mainVM, homeVM ->
                    homeVM.setHomePageVisible(false)
                    mainVM.addFolderPathToHistory("/storage/emulated/0/Movies")
                }
            ))

        homeItems.add(
            HomeItem(
            title = "Films/Sexe",
            icon = R.drawable.sexe,
            onClick = { mainVM, homeVM ->
                homeVM.setHomePageVisible(false)
                mainVM.addFolderPathToHistory("/storage/emulated/0/Movies/sexe")
            }
        ))
        
        homeItems.add(
            HomeItem(
            title = "1DM+",
            icon = R.drawable.sexe,
            onClick = { mainVM, homeVM ->
                homeVM.setHomePageVisible(false)
                mainVM.addFolderPathToHistory("/storage/emulated/0/Download/1DMP/General")
            }
        ))

        homeItems.add(
            HomeItem(
            title = "Nzbs",
            icon = R.drawable.downloads2,
            onClick = { mainVM, homeVM ->
                homeVM.setHomePageVisible(false)
                mainVM.addFolderPathToHistory("/storage/emulated/0/Download/nzb")
            }
        ))
    }
}

data class HomeItem(
    val id: UUID = UUID.randomUUID(),
    val title: String,
    @DrawableRes val icon: Int,
    val onClick: (
        mainVM: SigmaViewModel,
        homeVM: HomeViewModel
    ) -> Unit
)





