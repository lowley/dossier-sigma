package lorry.folder.items.dossiersigma.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.domain.SigmaFolder
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.domain.interfaces.IDiskRepository
import lorry.folder.items.dossiersigma.domain.usecases.clipboard.AccessingToInternetSiteForPictureUseCase
import lorry.folder.items.dossiersigma.domain.usecases.pictures.ChangingPictureUseCase
import javax.inject.Inject

@HiltViewModel
class SigmaViewModel @Inject constructor(
    private val diskRepository: IDiskRepository,
    val changingPictureUseCase: ChangingPictureUseCase,
    val accessingToInternet: AccessingToInternetSiteForPictureUseCase,
) : ViewModel() {

    private val _folder = MutableStateFlow<SigmaFolder>(SigmaFolder(
        fullPath = "Veuillez attendre",
        picture = null,
        items = listOf<Item>()
    ))

    val folder: StateFlow<SigmaFolder>
        get() = _folder

    private val _isBrowserVisible = MutableStateFlow(false)
    val isBrowserVisible: StateFlow<Boolean> = _isBrowserVisible

    fun showBrowser() {
        _isBrowserVisible.value = true
    }

    fun hideBrowser() {
        _isBrowserVisible.value = false
    }

    //BROWSER PERSON SEARCH
    private val _browserPersonSearch = MutableStateFlow("")
    val browserPersonSearch: StateFlow<String> = _browserPersonSearch
    
    fun setBrowserPersonSearch(search: String) {
        _browserPersonSearch.value = search   
    }
    
    //SELECTED ITEM
    private val _selectedItem = MutableStateFlow<Item?>(null)
    val selectedItem: StateFlow<Item?> = _selectedItem

    fun setSelectedItem(item: Item) {
        _selectedItem.value = item
    }
     
    //SELECTED PICTURE
    private val _selectedItemPicture = MutableStateFlow(PictureWrapper())
    val selectedItemPicture: StateFlow<PictureWrapper> = _selectedItemPicture

    fun resetPictureFlow() {
        _selectedItemPicture.value = PictureWrapper(
            reset = true,
            id =  _selectedItemPicture.value.id + 1)
        Log.d("toto", "resetPictureFlow: ${_selectedItemPicture.value}")
    }

    fun startPictureFlow() {
        _selectedItemPicture.value = PictureWrapper(
            id =  _selectedItemPicture.value.id)
    }

    fun updatePicture(newPicture: Any?) {
        _selectedItemPicture.value = PictureWrapper(picture = newPicture,
            id =  _selectedItemPicture.value.id + 1)
        Log.d("toto", "updatePicture: ${_selectedItemPicture.value}")
    }
    
    
    fun setPictureWithClipboard(item: Item) {
        val newItem = changingPictureUseCase.changeItemWithClipboardPicture(item)
        updateItemList(newItem)
    }

    fun openBrowser(item: Item) {
        //accessingToInternet.startListeningToClipboard(item.id)
        accessingToInternet.openBrowser(item, this)
        
    }

    fun updateItemList(newItem: Item) {
        val currentFolder = _folder.value
        val index = currentFolder.items.indexOfFirst { it.id == newItem.id }
        if (index != -1) {
            val updatedItems = currentFolder.items.toMutableList()
            updatedItems[index] = newItem
            _folder.value = currentFolder.copy(items = updatedItems)
        }
    }

    fun goToFolder(folderPath: String) {
        viewModelScope.launch {
            val newFolder = SigmaFolder(folderPath, null, diskRepository.getFolderItems(folderPath))
            _folder.value = newFolder
        }
    }

    fun goToFolder(newFolder: SigmaFolder){
        _folder.value = newFolder
    }

    fun goToFolderSafely(folderPath: String) {
        viewModelScope.launch {
            val newFolder = SigmaFolder(folderPath, null, emptyList())
            _folder.value = newFolder
        }   
    }
    
    init {
        val initialDirectoryPath = "/storage/7376-B000/SEXE 2"
        goToFolder(initialDirectoryPath)
        //goToFolder(diskRepository.getInitialFolder())
    }
}

data class PictureWrapper(
    val picture: Any? = null,
    // Indique si le flux est en mode réinitialisation
    val reset: Boolean = false,
    val id: Int = 0
) {
    override fun toString(): String {
        return "id:$id,picture:${if (picture == null) "non" else "oui"}, reset:$reset, ${System.identityHashCode(this)}"
    }
}
