package lorry.folder.items.dossiersigma.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.data.bento.BentoRepository
import lorry.folder.items.dossiersigma.data.interfaces.IPlayingDataSource
import lorry.folder.items.dossiersigma.domain.SigmaFolder
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.domain.interfaces.IDiskRepository
import lorry.folder.items.dossiersigma.domain.usecases.clipboard.AccessingToInternetSiteForPictureUseCase
import lorry.folder.items.dossiersigma.domain.usecases.pictures.ChangingPictureUseCase
import okio.Path
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class SigmaViewModel @Inject constructor(
    private val diskRepository: IDiskRepository,
    val changingPictureUseCase: ChangingPictureUseCase,
    val accessingToInternet: AccessingToInternetSiteForPictureUseCase,
    private val ffmpegRepository: BentoRepository,
    val playingDataSource: IPlayingDataSource
) : ViewModel() {

    private val _folder = MutableStateFlow<SigmaFolder>(SigmaFolder(
        fullPath = "Veuillez attendre",
        picture = null,
        items = listOf<Item>(),
        modificationDate = System.currentTimeMillis()
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

    //BROWSER SEARCH
    private val _browserSearch = MutableStateFlow("")
    val browserSearch: StateFlow<String> = _browserSearch

    //other case is for movies
    private val _searchIsForPersonNotMovies = MutableStateFlow(true)
    val searchIsForPersonNotMovies: StateFlow<Boolean> = _searchIsForPersonNotMovies
    
    fun setBrowserPersonSearch(search: String) {
        _browserSearch.value = search  
        _searchIsForPersonNotMovies.value = true
    }

    fun setBrowserMovieSearch(search: String) {
        _browserSearch.value = search
        _searchIsForPersonNotMovies.value = false
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
        viewModelScope.launch{
            ffmpegRepository.addPictureToMP4Metadata(newPicture as String, _selectedItem.value!!.fullPath)
        }
        _selectedItemPicture.value = PictureWrapper(picture = newPicture,
            id =  _selectedItemPicture.value.id + 1)
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

    fun goToFolder(folderPath: String, sorting: ITEMS_ORDERING_STRATEGY) {
        viewModelScope.launch {
            val newFolder = diskRepository.getSigmaFolder(folderPath, sorting)
            _folder.value = newFolder
        }
    }
    
    init {
        val initialDirectoryPath = "/storage/7376-B000/SEXE 2"
        goToFolder(initialDirectoryPath, ITEMS_ORDERING_STRATEGY.DATE_DESC)
    }

    fun getIconFile(context: Context, drawableResId: Int): File? {
        val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, drawableResId)

        // Créer un fichier temporaire pour stocker l'icône
        val tempFile = File("/storage/7376-B000/SEXE 2/icon.jpg")

        return try {
            FileOutputStream(tempFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out) // Sauvegarde en JPG
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun playMP4File(mp4FullPath: String) {
        viewModelScope.launch { 
            playingDataSource.playMP4File(mp4FullPath, "video/mp4")
        }
    }

    fun playHtmlFile(htmlFullPath: String) {
        viewModelScope.launch {
            playingDataSource.playMP4File(htmlFullPath, "text/html")
        }
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

enum class ITEMS_ORDERING_STRATEGY{
    DATE_DESC,
    NAME_ASC
}
