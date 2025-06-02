package lorry.folder.items.dossiersigma.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.data.base64.IBase64DataSource
import lorry.folder.items.dossiersigma.data.base64.IMp4Base64Embedder
import lorry.folder.items.dossiersigma.data.bento.BentoRepository
import lorry.folder.items.dossiersigma.data.interfaces.IPlayingDataSource
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.domain.SigmaFolder
import lorry.folder.items.dossiersigma.domain.interfaces.IDiskRepository
import lorry.folder.items.dossiersigma.domain.usecases.clipboard.AccessingToInternetSiteForPictureUseCase
import lorry.folder.items.dossiersigma.domain.usecases.files.ChangePathUseCase
import lorry.folder.items.dossiersigma.domain.usecases.pictures.ChangingPictureUseCase
import java.io.File
import java.io.FileOutputStream
import java.net.URLDecoder
import javax.inject.Inject

@HiltViewModel
class SigmaViewModel @Inject constructor(
    val diskRepository: IDiskRepository,
    val changingPictureUseCase: ChangingPictureUseCase,
    val changePathUseCase: ChangePathUseCase,
    val accessingToInternet: AccessingToInternetSiteForPictureUseCase,
    val ffmpegRepository: BentoRepository,
    val playingDataSource: IPlayingDataSource,
    val base64DataSource: IBase64DataSource,
    val base64Embedder: IMp4Base64Embedder
) : ViewModel() {

    val imageCache = mutableMapOf<String, Any?>()

    private val _folder = MutableStateFlow<SigmaFolder>(
        SigmaFolder(
            fullPath = "Veuillez attendre",
            picture = null,
            items = listOf<Item>(),
            modificationDate = System.currentTimeMillis()
        )
    )

    val folder: StateFlow<SigmaFolder>
        get() = _folder

    private val _isBrowserVisible = MutableStateFlow(false)
    val isBrowserVisible: StateFlow<Boolean> = _isBrowserVisible

    private val _sorting = MutableStateFlow(ITEMS_ORDERING_STRATEGY.DATE_DESC)
    val sorting: StateFlow<ITEMS_ORDERING_STRATEGY> = _sorting

    private val _isGoogle = MutableStateFlow(false)
    val isGoogle: StateFlow<Boolean> = _isGoogle

    fun setIsGoogle(isGoogle: Boolean) {
        _isGoogle.value = isGoogle
    }
    
    fun showBrowser() {
        _isBrowserVisible.value = true
    }

    fun hideBrowser() {
        _isBrowserVisible.value = false
    }

    fun setSorting(sorting: ITEMS_ORDERING_STRATEGY) {
        _sorting.value = sorting
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
            id = _selectedItemPicture.value.id + 1
        )
        Log.d("toto", "resetPictureFlow: ${_selectedItemPicture.value}")
    }

    fun startPictureFlow() {
        _selectedItemPicture.value = PictureWrapper(
            id = _selectedItemPicture.value.id
        )
    }

    suspend fun updatePicture(newPicture: Any?) {
        if (_selectedItem.value == null)
            return

        if (_selectedItem.value?.isFile() == true)
            withContext(Dispatchers.IO) {
                ffmpegRepository.addPictureToMP4Metadata(
                    newPicture as String,
                    _selectedItem.value!!.fullPath
                )
            }
        else {
            //url vers bitmap puis dans _selectedItem 
            val pictureBitmap = withContext(Dispatchers.IO) {
                changingPictureUseCase.urlToBitmap(newPicture as String)
            }
            if (pictureBitmap != null) {
                withContext(Dispatchers.Main) {
                    _selectedItem.value = _selectedItem.value!!.copy(picture = pictureBitmap)
                    setPicture(_selectedItem.value!!, false)
                    
                    goToFolder(_selectedItem.value!!.path, ITEMS_ORDERING_STRATEGY.DATE_DESC)
                }
            }
        }

        _selectedItemPicture.value = PictureWrapper(
            picture = newPicture,
            id = _selectedItemPicture.value.id + 1
        )
    }

    fun setPicture(item: Item, fromClipboard: Boolean = false) {
        var newItem = item
        if (fromClipboard)
            newItem  = changingPictureUseCase.changeItemWithClipboardPicture(item)
        
        viewModelScope.launch {
            changingPictureUseCase.savePictureOfFolder(newItem)
            withContext(Dispatchers.Main) {
                updateItemList(newItem)
            }
        }
    }

    fun openBrowser(item: Item, isGoogle: Boolean = false) {
        setIsGoogle(isGoogle)
        accessingToInternet.openBrowser(item, this)
    }

    suspend fun updateItemList(newItem: Item) {
        val currentFolder = _folder.value
        val index = currentFolder.items.indexOfFirst { it.id == newItem.id }
        if (index != -1) {
            val updatedItems = currentFolder.items.toMutableList()
            updatedItems[index] = newItem
            _folder.value = currentFolder.copy(items = updatedItems)
        }
    }

    fun goToFolder(folderPath: String, sorting: ITEMS_ORDERING_STRATEGY) {
        setSorting(sorting)
        imageCache.clear()
        viewModelScope.launch(Dispatchers.IO) {
            val newFolder = diskRepository.getSigmaFolder(folderPath, sorting)
            withContext(Dispatchers.Main) {
                _folder.value = newFolder
            }
        }
    }
    
    init {
        val initialDirectoryPath = "/storage/emulated/0/Movies"
        goToFolder(initialDirectoryPath, ITEMS_ORDERING_STRATEGY.DATE_DESC)
    }

    fun getIconFile(context: Context, drawableResId: Int): File? {
        val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, drawableResId)

        // Créer un fichier temporaire pour stocker l'icône
        val tempFile = File("/storage/emulated/0/Download/icon.jpg")

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
        viewModelScope.launch(Dispatchers.IO) {
            playingDataSource.playMP4File(mp4FullPath, "video/mp4")
        }
    }

    fun playHtmlFile(htmlFullPath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            playingDataSource.playMP4File(htmlFullPath, "text/html")
        }
    }

    fun onFolderSelected(pathUri: Uri?) {
        if (pathUri != null) {
            val nouvelle = URLDecoder.decode(pathUri.toString().substringAfter("7376-B000","^^")).drop(1)
            val ancienne = URLDecoder.decode(pathUri.toString().substringAfter(
                "6539-3963", "^^")).drop(1)
            
            var valueToSave = "^^"
            if (nouvelle != "^")
               valueToSave = "/storage/7376-B000/" + nouvelle
            if (ancienne != "^")
                valueToSave = "/storage/6539-3963/" + ancienne
            if (valueToSave != "^^")
                goToFolder(valueToSave, ITEMS_ORDERING_STRATEGY.DATE_DESC)
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
        return "id:$id,picture:${if (picture == null) "non" else "oui"}, reset:$reset, ${
            System.identityHashCode(
                this
            )
        }"
    }
}

enum class ITEMS_ORDERING_STRATEGY {
    DATE_DESC,
    NAME_ASC
}
