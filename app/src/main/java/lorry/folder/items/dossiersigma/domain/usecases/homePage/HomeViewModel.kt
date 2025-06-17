package lorry.folder.items.dossiersigma.domain.usecases.homePage

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.ui.SigmaViewModel
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
                title = "Filles",
                icon = R.drawable.sexe,
                onClick = { mainVM, homeVM ->
                    mainVM.addFolderPathToHistory("/storage/emulated/0/Movies/sexe/filles")
                    homeVM.setHomePageVisible(false)
                }
            ))

        homeItems.add(
            HomeItem(
                title = "Fantasmes",
                icon = R.drawable.sexe,
                onClick = { mainVM, homeVM ->
                    mainVM.addFolderPathToHistory("/storage/emulated/0/Movies/sexe/fantasmes")
                    homeVM.setHomePageVisible(false)
                }
            ))

        homeItems.add(
            HomeItem(
                title = "Films",
                icon = R.drawable.film,
                onClick = { mainVM, homeVM ->
                    mainVM.addFolderPathToHistory("/storage/emulated/0/Movies")
                    homeVM.setHomePageVisible(false)
                }
            ))

        homeItems.add(
            HomeItem(
                title = "1DM+",
                icon = R.drawable.sexe,
                onClick = { mainVM, homeVM ->
                    mainVM.addFolderPathToHistory("/storage/emulated/0/Download/1DMP/General")
                    homeVM.setHomePageVisible(false)
                }
            ))

        homeItems.add(
            HomeItem(
                title = "Nzbs",
                icon = R.drawable.downloads2,
                onClick = { mainVM, homeVM ->
                    mainVM.addFolderPathToHistory("/storage/emulated/0/Download/nzb")
                    homeVM.setHomePageVisible(false)
                }
            ))

        homeItems.add(
            HomeItem(
                title = "Films/Sexe",
                icon = R.drawable.sexe,
                onClick = { mainVM, homeVM ->
                    mainVM.addFolderPathToHistory("/storage/emulated/0/Movies/sexe")
                    homeVM.setHomePageVisible(false)
                }
            ))
        
        homeItems.add(
            HomeItem(
            title = "Stockage principal",
            icon = R.drawable.hdd,
            onClick = { mainVM, homeVM ->
                mainVM.addFolderPathToHistory("/storage/emulated/0")
                homeVM.setHomePageVisible(false)
            }
        ))

        homeItems.add(
            HomeItem(
            title = "Téléchargements",
            icon = R.drawable.downloads2,
            onClick = { mainVM, homeVM ->
                mainVM.addFolderPathToHistory("/storage/emulated/0/Download")
                homeVM.setHomePageVisible(false)
            }
        ))
    }
}

data class HomeItem(
    val id: UUID = UUID.randomUUID(),
    val title: String,
    @DrawableRes val icon: Int,
    val onClick: suspend (
        mainVM: SigmaViewModel,
        homeVM: HomeViewModel
    ) -> Unit
)





