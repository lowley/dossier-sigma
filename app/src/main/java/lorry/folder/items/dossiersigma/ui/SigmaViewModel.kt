package lorry.folder.items.dossiersigma.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.PermissionsManager
import lorry.folder.items.dossiersigma.domain.Folder
import lorry.folder.items.dossiersigma.domain.interfaces.IDiskRepository
import javax.inject.Inject


@HiltViewModel
class SigmaViewModel @Inject constructor(private val diskRepository: IDiskRepository) : ViewModel(){
    
    private val _folder = MutableStateFlow<Folder>(Folder("Chargement...", emptyList())) 
    val folder: StateFlow<Folder>
        get() = _folder
    
    fun updateFolder(newFolder: Folder){
        _folder.value = newFolder
    }
    
    init {
        val initialDirectoryPath = "/storage/7376-B000/SEXE 2"
        
        viewModelScope.launch {
            updateFolder(Folder(initialDirectoryPath, diskRepository.getFolderItems(initialDirectoryPath)))
            //updateFolder(diskRepository.getInitialFolder())
        }
    }
}