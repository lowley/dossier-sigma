package lorry.folder.items.dossiersigma.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.domain.SigmaFolder
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.domain.SigmaFile
import lorry.folder.items.dossiersigma.domain.interfaces.IDiskRepository
import lorry.folder.items.dossiersigma.domain.services.pictures.ChangingPictureService
import javax.inject.Inject

@HiltViewModel
class SigmaViewModel @Inject constructor(
    private val diskRepository: IDiskRepository,
    val changingPictureService: ChangingPictureService
) : ViewModel() {

    private val _folder = MutableStateFlow<SigmaFolder>(SigmaFolder(
        fullPath = "Veuillez attendre",
        picture = null,
        items = listOf<Item>()
    ))

    val folder: StateFlow<SigmaFolder>
        get() = _folder
    
    fun setPictureWithClipboard(item: Item) {
        val newItem = changingPictureService.changeItemWithClipboardPicture(item)
        updateItemList(newItem)
    }

    fun updateItemPicture(itemId: String, newPicture: Bitmap?) {
        val currentFolder = _folder.value
        val index = currentFolder.items.indexOfFirst { it.id == itemId }
        if (index != -1) {
            val updatedItems = currentFolder.items.toMutableList()
            val updatedItem =  when (val item = updatedItems[index]) {
                is SigmaFile -> item.copy(picture = newPicture)
                is SigmaFolder -> item.copy(picture = newPicture)
                else -> item
            }
            
            updatedItems[index] = updatedItem
            _folder.value = currentFolder.copy(items = updatedItems)
        }
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

    init {
        val initialDirectoryPath = "/storage/7376-B000/SEXE 2"
        goToFolder(initialDirectoryPath)
        //goToFolder(diskRepository.getInitialFolder())
    }
}
