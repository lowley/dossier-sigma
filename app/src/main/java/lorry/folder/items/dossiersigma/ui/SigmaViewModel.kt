package lorry.folder.items.dossiersigma.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.data.base64.IBase64DataSource
import lorry.folder.items.dossiersigma.data.base64.IVideoInfoEmbedder
import lorry.folder.items.dossiersigma.data.base64.Tags
import lorry.folder.items.dossiersigma.data.bento.BentoRepository
import lorry.folder.items.dossiersigma.data.interfaces.IPlayingDataSource
import lorry.folder.items.dossiersigma.domain.ColoredTag
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.domain.SigmaFolder
import lorry.folder.items.dossiersigma.domain.interfaces.IDiskRepository
import lorry.folder.items.dossiersigma.domain.usecases.browser.BrowserUseCase
import lorry.folder.items.dossiersigma.domain.usecases.files.ChangePathUseCase
import lorry.folder.items.dossiersigma.domain.usecases.pictures.ChangingPictureUseCase
import lorry.folder.items.dossiersigma.ui.components.BottomTools
import lorry.folder.items.dossiersigma.ui.components.BottomTools.viewModel
import lorry.folder.items.dossiersigma.ui.components.TagInfos
import lorry.folder.items.dossiersigma.ui.components.Tool
import lorry.folder.items.dossiersigma.ui.components.Tools
import lorry.folder.items.dossiersigma.ui.components.Tools.DEFAULT
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URLDecoder
import java.util.UUID
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
    val base64Embedder: IVideoInfoEmbedder,
) : ViewModel() {

    val imageCache = mutableMapOf<String, Any?>()
    val scaleCache = mutableMapOf<String, ContentScale>()

    private val _flagCache = MutableStateFlow(mutableMapOf<String, ColoredTag>())
    val sortingCache = mutableMapOf<String, ITEMS_ORDERING_STRATEGY>()
    val flagCache: StateFlow<MutableMap<String, ColoredTag>> = _flagCache

    fun setFlagCacheValue(key: String, tag: ColoredTag) {
        _flagCache.value = _flagCache.value.toMutableMap().apply {
            put(key, tag)
        }
        println("ajout de clé dans flagCache, il y a ${_flagCache.value.size} clés")

    }

    fun removeFlagCacheForKey(key: String): ColoredTag? {
        return _flagCache.value.remove(key)
    }

    fun clearFlagCache() {
        _flagCache.value = mutableMapOf()
        println("clearFlagCache, il y a ${_flagCache.value.size} clés")

    }

    private val _dragTargetItem = MutableStateFlow<Item?>(null)
    val dragTargetItem: StateFlow<Item?> = _dragTargetItem

    fun setDragTargetItem(item: Item?) {
        _dragTargetItem.value = item
    }

    private val _dragOffset = MutableStateFlow<Offset?>(null)
    val dragOffset: StateFlow<Offset?> = _dragOffset

    fun setDragOffset(offset: Offset?) {
        _dragOffset.value = offset
    }

    fun addToDragOffset(offset: Offset) {
        _dragOffset.value = (_dragOffset.value ?: Offset.Zero) + offset
    }

    private val _draggableStartPosition = MutableStateFlow<Offset?>(null)
    val draggableStartPosition: StateFlow<Offset?> = _draggableStartPosition

    fun setDraggableStartPosition(position: Offset?) {
        _draggableStartPosition.value = position
    }

    private val _draggedTag = MutableStateFlow<ColoredTag?>(null)
    val draggedTag: StateFlow<ColoredTag?> = _draggedTag

    fun setDraggedTag(tag: ColoredTag?) {
        _draggedTag.value = tag
    }

    private val _sorting = MutableStateFlow(ITEMS_ORDERING_STRATEGY.DATE_DESC)
    val sorting: StateFlow<ITEMS_ORDERING_STRATEGY> = _sorting

    private val _pictureUpdateId = MutableStateFlow(0)
    val pictureUpdateId: StateFlow<Int> = _pictureUpdateId

    private val _isContextMenuVisible = MutableStateFlow(false)
    val isContextMenuVisible: StateFlow<Boolean> = _isContextMenuVisible

    fun setIsContextMenuVisible(isVisible: Boolean) {
        _isContextMenuVisible.value = isVisible
    }

    private val _folderPathHistory = MutableStateFlow<List<String>>(emptyList())
    val folderPathHistory: StateFlow<List<String>> = _folderPathHistory

    val currentFolderPath: StateFlow<String> = folderPathHistory
        .map { it.lastOrNull() ?: "/storage/emulated/0/Movies" }
        .stateIn(
            scope = viewModelScope,
            started = Eagerly,
            initialValue = "/storage/emulated/0/Movies"
        )
    private val reloadTrigger = MutableStateFlow(0)

    private val _isFolderVisible = MutableStateFlow(false)
    val isFolderVisible: StateFlow<Boolean> = _isFolderVisible

    fun setIsFolderVisible(isVisible: Boolean) {
        _isFolderVisible.value = isVisible
    }

    // combine chemin + trigger pour déclencher un nouveau getSigmaFolder
    val currentFolder: StateFlow<SigmaFolder> = combine(
        currentFolderPath,
        reloadTrigger
    ) { path, _ ->
        path
    }.mapLatest { path ->
        diskRepository.getSigmaFolder(path, sorting.value)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SigmaFolder(
            path = "/storage/emulated/0/Movies",
            name = "Veuillez attendre",
            picture = null,
            items = emptyList<Item>(),
            modificationDate = System.currentTimeMillis()
        )
    )

    companion object {
        private val _refreshRequested = MutableSharedFlow<Unit>(replay = 0)
        val refreshRequested = _refreshRequested.asSharedFlow()

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        fun requestRefresh() {
            scope.launch {
                _refreshRequested.emit(Unit)
            }
        }
    }

    init {
        viewModelScope.launch {
            refreshRequested.collect {
                refreshCurrentFolder()
            }
        }
    }

    private val _dialogMessage = MutableStateFlow("")
    val dialogMessage: StateFlow<String?> = _dialogMessage

    fun setDialogMessage(message: String) {
        _dialogMessage.value = message
    }

    var dialogOnOkLambda: (suspend (String, SigmaViewModel, Context) -> Unit)? = null
    var dialogYesNoLambda: (suspend (Boolean, SigmaViewModel, Context) -> Unit)? = null
    var dialogTagLambda: (suspend (tagInfos: TagInfos?, vm: SigmaViewModel, context: Context) -> Unit)? = null

    fun refreshCurrentFolder() {
        reloadTrigger.value = reloadTrigger.value + 1 // redéclenchement immédiat
    }

    fun addFolderPathToHistory(folderPath: String) {
        val currentHistory = _folderPathHistory.value
        _folderPathHistory.value = currentHistory + folderPath
    }

    fun removeLastFolderPathHistory() {
        val currentHistory = _folderPathHistory.value
        _folderPathHistory.value = currentHistory.dropLast(1)
    }

    fun setSorting(sorting: ITEMS_ORDERING_STRATEGY) {
        _sorting.value = sorting
    }

    fun notifyPictureUpdated() {
        _pictureUpdateId.value += 1
    }

    //SELECTED ITEM
    private val _selectedItem = MutableStateFlow<Item?>(null)
    val selectedItem: StateFlow<Item?> = _selectedItem

    fun setSelectedItem(item: Item?, keepBottomToolsAsIs: Boolean = false) {
        _selectedItem.value = item

        if (!keepBottomToolsAsIs) {
            if (item != null)
                BottomTools.setCurrentContent(Tools.FILE)
            else
                BottomTools.setCurrentContent(Tools.DEFAULT)
        }
    }

    val selectedItemFullPath = selectedItem
        .map { item -> item?.fullPath }
        .stateIn(
            scope = viewModelScope,
            started = Eagerly,
            initialValue = null
        )


    suspend fun updatePicture(
        newPicture: Any?,
        onlyCropped: Boolean = false
    ) {
        if (_selectedItem.value == null)
            return

        var pictureBitmap =
            if (newPicture is String) withContext(Dispatchers.IO) {
                changingPictureUseCase.urlToBitmap(newPicture)
            }
            else newPicture as Bitmap

        if (pictureBitmap == null)
            return

        //s'assure que les refreshs ci-dessous verront bien la nouvelle image
        imageCache.remove(_selectedItem.value?.fullPath)

        //expérimental
        pictureBitmap = compressBitmapToMaxSizeAsBitmap(pictureBitmap)

        if (_selectedItem.value?.isFile() == true) {
            val image64 = base64Embedder.bitmapToBase64(pictureBitmap)
            val item = _selectedItem.value
            if (item == null)
                return


            val base64_tag = if (onlyCropped) {
                base64Embedder.extractBase64FromFile(
                    File(item.fullPath),
                    tagSuffix = Tags.COVER
                )
            } else null

            base64Embedder.removeBothBase64(File(item.fullPath))

            if (onlyCropped)
                base64Embedder.appendBase64ToFile(File(item.fullPath), base64_tag!!, tagSuffix = Tags.COVER)
            else
                base64Embedder.appendBase64ToFile(File(item.fullPath), image64, tagSuffix = Tags.COVER)

            base64Embedder.appendBase64ToFile(File(item.fullPath), image64, tagSuffix = Tags.COVER_CROPPED)
        } else {
            _selectedItem.value = _selectedItem.value!!.copy(picture = pictureBitmap)
            setPictureInFolder(
                _selectedItem.value!!,
                fromClipboard = false,
                onlyCropped = onlyCropped
            )
        }
    }

    fun compressBitmapToMaxSizeAsBitmap(
        original: Bitmap,
        maxBytes: Int = 65535,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
    ): Bitmap {
        var quality = 100
        var compressedBytes: ByteArray

        do {
            val stream = ByteArrayOutputStream()
            original.compress(format, quality, stream)
            compressedBytes = stream.toByteArray()
            quality -= 5
        } while (compressedBytes.size > maxBytes && quality > 5)

        return BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)
    }

    fun setPictureInFolder(item: Item, fromClipboard: Boolean = false, onlyCropped: Boolean) {
        var newItem = item
        if (fromClipboard)
            newItem = changingPictureUseCase.changeItemWithClipboardPicture(item)

        viewModelScope.launch {
            changingPictureUseCase.savePictureOfFolder(
                newItem,
                onlyCropped = onlyCropped
            )
            withContext(Dispatchers.Main) {
                refreshCurrentFolder()
            }
        }
    }

//    fun updateItemList(newItem: Item) {
//        viewModelScope.launch {
//            val currentFolderPath = this@SigmaViewModel.currentFolder.value
//
//            var currentFolder: SigmaFolder? = null
//            currentFolder = withContext(Dispatchers.IO) {
//                diskRepository.getSigmaFolder(
//                    currentFolderPath,
//                    ITEMS_ORDERING_STRATEGY.DATE_DESC
//                )
//            }
//            
//            val index = currentFolder.items.indexOfFirst { it.id == newItem.id }
//            if (index == -1)
//                return@launch
//
//            val updatedItems = currentFolder.items.toMutableList()
//            updatedItems[index] = newItem
//            _folder.value = currentFolder.copy(items = updatedItems)
//        }
//
//    }

    fun goToFolder(folderPath: String, sorting: ITEMS_ORDERING_STRATEGY? = null) {
        sortingCache[currentFolderPath.value] = this.sorting.value

        if (sorting != null)
            setSorting(sorting)
        else
            setSorting(sortingCache[folderPath] ?: ITEMS_ORDERING_STRATEGY.DATE_DESC)

        imageCache.clear()
        scaleCache.clear()
        clearFlagCache()
        DEFAULT.content().updateTools(emptyList<Tool>())

        viewModelScope.launch(Dispatchers.IO) {
            //val newFolder = diskRepository.getSigmaFolder(folderPath, sorting)
            withContext(Dispatchers.Main) {
                if (folderPath == currentFolderPath.value)
                    refreshCurrentFolder()
                else
                    addFolderPathToHistory(folderPath)
            }
        }


    }

    init {
        BottomTools.viewModel = this
        viewModelScope.launch() {
            BottomTools.progress.collect { p ->
                if (p == 0 || p == 100)
                    BottomTools.updateMovePasteText("Coller")
                else
                    BottomTools.updateMovePasteText("$p %")
            }
        }

        viewModelScope.launch() {
            BottomTools.nasProgress.collect { p ->
                if (p == 0 || p == 100)
                    BottomTools.updateMoveNASText("1 -> NAS")
                else
                    BottomTools.updateMoveNASText("$p %")
            }
        }

        BottomTools.viewModel = this
        BottomTools.observeDefaultContent(this)
        BottomTools.setCurrentContent(DEFAULT)
    }

//    init {
//        val initialDirectoryPath = "/storage/emulated/0/Movies"
//        goToFolder(initialDirectoryPath, ITEMS_ORDERING_STRATEGY.DATE_DESC)
//    }

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
                goToFolder(valueToSave)
        }
    }

    /**
     * se produit lors du drag'n drop d'une étiquette dans bottomTools
     * l'Item peut déjà contenir une étiquette -> modification
     * sinon -> ajout
     * @see BottomTools.BottomToolBar
     */
    fun assignColoredTagToItem(item: Item, tag: ColoredTag) {
//        println("DRAG assignColoredTagToItem, item = ${item.name}, tag = ${tag.title}")

        val containsTag = item.tag != null

        viewModelScope.launch {
            if (item.isFile()) {
                val file = File(item.fullPath)
                if (containsTag) {
                    base64Embedder.removeFlagFromFile(file)
                    base64Embedder.appendFlagToFile(file, tag)
                } else {
                    base64Embedder.appendFlagToFile(file, tag)
                }
            }

            if (item.isFolder()) {
                if (containsTag) {
                    diskRepository.removeTagFromHtml(item.fullPath)
                    diskRepository.insertTagToHtmlFile(item, tag)
                } else {
                    diskRepository.insertTagToHtmlFile(item, tag)
                }
            }

            removeFlagCacheForKey(item.fullPath)
            refreshCurrentFolder()
        }
    }

    suspend fun getInfoSup(item: Item): String {
        return withContext(Dispatchers.IO) {
            val infos = if (item is SigmaFolder) diskRepository
                .countFilesAndFolders(File(item.fullPath)).component1().toString() else item.name
                .substringAfterLast(".")
            
            infos
        }
    }

    suspend fun getInfoInf(item: Item): String {
        return withContext(Dispatchers.IO) {
            val infos = if (item is SigmaFolder)
                viewModel.diskRepository.countFilesAndFolders(File(item.fullPath)).component2().toString()
            else viewModel.diskRepository.getSize(File(item.fullPath)).toString()
            
            infos
        }
    }
}

enum class ITEMS_ORDERING_STRATEGY {
    DATE_DESC,
    NAME_ASC
}

fun StateFlow<MutableMap<String, ColoredTag>>.containsFlagAsValue(valueId: UUID): Boolean {
    return valueId in this.value.values.map { it.id }
}