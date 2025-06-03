package lorry.folder.items.dossiersigma.ui

import android.net.Uri
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
import lorry.folder.items.dossiersigma.domain.usecases.browser.BrowserUseCase
import lorry.folder.items.dossiersigma.domain.usecases.files.ChangePathUseCase
import lorry.folder.items.dossiersigma.domain.usecases.pictures.ChangingPictureUseCase
import java.io.File
import java.net.URLDecoder
import javax.inject.Inject

@HiltViewModel
class SigmaViewModel @Inject constructor(
    val diskRepository: IDiskRepository,
    val changingPictureUseCase: ChangingPictureUseCase,
    val changePathUseCase: ChangePathUseCase,
    val browserManager: BrowserUseCase,
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

    private val _sorting = MutableStateFlow(ITEMS_ORDERING_STRATEGY.DATE_DESC)
    val sorting: StateFlow<ITEMS_ORDERING_STRATEGY> = _sorting

    private val _pictureUpdateId = MutableStateFlow(0)
    val pictureUpdateId: StateFlow<Int> = _pictureUpdateId

    fun setSorting(sorting: ITEMS_ORDERING_STRATEGY) {
        _sorting.value = sorting
    }

    fun notifyPictureUpdated() {
        _pictureUpdateId.value += 1
    }

    //SELECTED ITEM
    private val _selectedItem = MutableStateFlow<Item?>(null)
    val selectedItem: StateFlow<Item?> = _selectedItem

    fun setSelectedItem(item: Item) {
        _selectedItem.value = item
    }

    suspend fun updatePicture(newPicture: Any?) {
        if (_selectedItem.value == null)
            return

        val pictureBitmap = withContext(Dispatchers.IO) {
            changingPictureUseCase.urlToBitmap(newPicture as String)
        }

        if (pictureBitmap == null)
            return

        if (_selectedItem.value?.isFile() == true) {
            val image64 = base64Embedder.bitmapToBase64(pictureBitmap)
            val item = _selectedItem.value
            if (item == null)
                return
            base64Embedder.removeEmbeddedBase64(File(item.fullPath))
            base64Embedder.appendBase64ToMp4(File(item.fullPath), image64)
        } else {
            //url vers bitmap puis dans _selectedItem

            _selectedItem.value = _selectedItem.value!!.copy(picture = pictureBitmap)
            setPicture(_selectedItem.value!!, false)
        }

        goToFolder(_selectedItem.value!!.path, ITEMS_ORDERING_STRATEGY.DATE_DESC)

        notifyPictureUpdated()
    }

    fun setPicture(item: Item, fromClipboard: Boolean = false) {
        var newItem = item
        if (fromClipboard)
            newItem = changingPictureUseCase.changeItemWithClipboardPicture(item)

        viewModelScope.launch {
            changingPictureUseCase.savePictureOfFolder(newItem)
            withContext(Dispatchers.Main) {
                updateItemList(newItem)
            }
        }
    }

    fun openBrowser(item: Item, isGoogle: Boolean = false) {
        setSelectedItem(item)
        browserManager.setIsGoogle(isGoogle)
        browserManager.openBrowser(item, this)
    }

    fun updateItemList(newItem: Item) {
        val currentFolder = _folder.value
        val index = currentFolder.items.indexOfFirst { it.id == newItem.id }
        if (index == -1)
            return

        val updatedItems = currentFolder.items.toMutableList()
        updatedItems[index] = newItem
        _folder.value = currentFolder.copy(items = updatedItems)
        
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

    fun playVideoFile(videoFullPath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            playingDataSource.playFile(videoFullPath, "video/mp4")
        }
    }

    fun playHtmlFile(htmlFullPath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            playingDataSource.playFile(htmlFullPath, "text/html")
        }
    }

    /**
     * callback from intent
     */
    fun onFolderSelected(pathUri: Uri?) {
        if (pathUri != null) {
            val nouvelle =
                URLDecoder.decode(pathUri.toString().substringAfter("7376-B000", "^^")).drop(1)
            val ancienne = URLDecoder.decode(
                pathUri.toString().substringAfter(
                    "6539-3963", "^^"
                )
            ).drop(1)

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

enum class ITEMS_ORDERING_STRATEGY {
    DATE_DESC,
    NAME_ASC
}
