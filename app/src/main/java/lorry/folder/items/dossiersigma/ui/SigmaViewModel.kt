package lorry.folder.items.dossiersigma.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.domain.Folder
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.domain.interfaces.IClipboardRepository
import lorry.folder.items.dossiersigma.domain.interfaces.IDiskRepository
import lorry.folder.items.dossiersigma.domain.services.pictures.ChangingPictureService
import javax.inject.Inject
import kotlin.text.get
import kotlin.text.set


@HiltViewModel
class SigmaViewModel @Inject constructor(
    private val diskRepository: IDiskRepository,
    private val changingPictureService: ChangingPictureService
) : ViewModel(){
    
    private val _folder = MutableStateFlow<Folder>(Folder("Chargement...", emptyList())) 
    
    val folder: StateFlow<Folder>
        get() = _folder
    
    fun updateFolder(newFolder: Folder){
        _folder.value = newFolder
    }
    
    fun setPictureWithClipboard(item: Item){
        val newItem = changingPictureService.changeItemWithClipboardPicture(item) 
        updateItemContent(newItem.id, newItem.content)
    }

    fun updateItemContent(itemId: String, newContent: Bitmap?) {
        val currentFolder = _folder.value
        val index = currentFolder.items.indexOfFirst { it.id == itemId }
        if (index != -1) {
            val updatedItems = currentFolder.items.toMutableList()
            val updatedItem = updatedItems[index].copy(content = newContent)
            updatedItems[index] = updatedItem

            _folder.value = currentFolder.copy(items = updatedItems)
        }
    }
    
    
    init {
        val initialDirectoryPath = "/storage/7376-B000/SEXE 2"
        
        viewModelScope.launch {
            updateFolder(Folder(initialDirectoryPath, diskRepository.getFolderItems(initialDirectoryPath)))
            //updateFolder(diskRepository.getInitialFolder())
        }
    }
}