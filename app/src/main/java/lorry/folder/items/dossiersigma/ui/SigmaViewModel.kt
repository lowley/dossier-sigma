package lorry.folder.items.dossiersigma.ui

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
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
import lorry.folder.items.dossiersigma.data.bento.BentoRepository
import lorry.folder.items.dossiersigma.data.dataSaver.CompositeManager
import lorry.folder.items.dossiersigma.data.dataSaver.CroppedPicture
import lorry.folder.items.dossiersigma.data.dataSaver.Flag
import lorry.folder.items.dossiersigma.data.dataSaver.InitialPicture
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

    ////////////////
    // imageCache //
    ////////////////
    private val _imageCache = MutableStateFlow(mutableMapOf<String, Any?>())
    val imageCache: StateFlow<MutableMap<String, Any?>> = _imageCache

    fun setImageCacheValue(key: String, image: Any?) {
        if (image != null) {
            _imageCache.value[key] = image
            println("ajout de clé dans imageCache: ${key.takeLast(20)}: $image")
            println("il y a ${_imageCache.value.size} clés")
        }
    }

    fun clearImageCache() {
        _imageCache.value.clear()
    }

    fun removeImageFromCache(key: String?) {
        if (key != null)
            _imageCache.value.remove(key)
    }

    ////////////////
    // scaleCache //
    ////////////////
    private val _scaleCache = MutableStateFlow(mutableMapOf<String, ContentScale>())
    val scaleCache: StateFlow<MutableMap<String, ContentScale>> = _scaleCache

    fun setScaleCacheValue(key: String, scale: ContentScale?) {
        if (scale == null)
            return
        _scaleCache.value = _scaleCache.value.toMutableMap().apply {
            put(key, scale)
            println("ajout de clé dans scaleCache: ${key.takeLast(20)}: $scale")
            println("il y a ${_scaleCache.value.size} clés")
        }
    }

    fun clearScalecache() {
        _scaleCache.value.clear()
    }

    val sortingCache = mutableMapOf<String, ITEMS_ORDERING_STRATEGY>()

    ///////////////
    // flagCache //
    ///////////////
    private val _flagCache = MutableStateFlow(mutableMapOf<String, ColoredTag>())
    val flagCache: StateFlow<MutableMap<String, ColoredTag>> = _flagCache

    fun setFlagCacheValue(key: String, tag: ColoredTag?) {
        if (tag == null)
            return
        _flagCache.value = _flagCache.value.toMutableMap().apply {
            put(key, tag)
            println("ajout de clé dans flagCache: ${key.takeLast(20)}: $tag")
            println("il y a ${_flagCache.value.size} clés")
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

    ///////////////
    // memoCache //
    ///////////////
    private val _memoCache = MutableStateFlow(mutableMapOf<String, String>())
    val memoCache: StateFlow<MutableMap<String, String>> = _memoCache

    fun setMemoCacheValue(key: String, memo: String?) {
        if (memo == null)
            return
        _memoCache.value = _memoCache.value.toMutableMap().apply {
            put(key, memo)
            println("ajout de clé dans memoCache: ${key.takeLast(20)}: $memo")
            println("il y a ${_memoCache.value.size} clés")
        }

        println("ajout de clé dans memoCache, il y a ${_memoCache.value.size} clés")
    }

    fun removeMemoCacheForKey(key: String): String? {
        return _memoCache.value.remove(key)
    }

    fun clearMemoCache() {
        _memoCache.value = mutableMapOf()
        println("clearMemoCache, il y a ${_memoCache.value.size} clés")

    }

    ///////////////////////
    // affichage du mémo //
    ///////////////////////
    private val _isDisplayingMemo = MutableStateFlow(false)
    val isDisplayingMemo: StateFlow<Boolean> = _isDisplayingMemo

    fun setIsDisplayingMemo(isVisible: Boolean) {
        _isDisplayingMemo.value = isVisible
    }

    //////////////
    // settings //
    //////////////
    private val _isSettingsPageVisible = MutableStateFlow<Boolean>(false)
    val isSettingsPageVisible: StateFlow<Boolean> = _isSettingsPageVisible

    fun setIsSettingsPageVisible(visible: Boolean) {
        _isSettingsPageVisible.value = visible
    }

    /////////////////
    // drag'n drop //
    /////////////////
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

    ////////////////////
    // maj de l'image //
    ////////////////////
    private val _pictureUpdateId = MutableStateFlow(0)
    val pictureUpdateId: StateFlow<Int> = _pictureUpdateId

//    fun notifyPictureUpdated() {
//        _pictureUpdateId.value += 1
//    }

    ///////////////////////////////
    // dossiers, dossier courant //
    ///////////////////////////////
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

    val currentFolder: StateFlow<SigmaFolder> = combine(
        currentFolderPath,
        reloadTrigger
    ) { path, _ ->
        path
    }.mapLatest { path ->
        val folder = diskRepository.getSigmaFolder(path, sorting.value)

        clearAllCaches()
        folder.items.forEach { item ->
            val path = item.fullPath
            setImageCacheValue(path, item.picture)
            setFlagCacheValue(path, item.tag)
            setScaleCacheValue(path, item.scale)
            setMemoCacheValue(path, item.memo)
        }

        folder

    }.stateIn(
        scope = viewModelScope,
        started = Eagerly,
        initialValue = SigmaFolder(
            path = "/storage/emulated/0/Movies",
            name = "Veuillez attendre",
            picture = null,
            items = emptyList(),
            tag = null,
            scale = ContentScale.Crop,
            modificationDate = System.currentTimeMillis(),
            memo = ""
        )
    )

    val currentMemo: StateFlow<String> = combine(
        currentFolderPath, memoCache
    ) { path, cache ->
        cache[path] ?: ""
    }.stateIn(
        scope = viewModelScope,
        started = Eagerly,
        initialValue = ""
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

    ////////////////////////
    // boîtes de dialogue //
    ////////////////////////
    private val _dialogMessage = MutableStateFlow("")
    val dialogMessage: StateFlow<String?> = _dialogMessage

    fun setDialogMessage(message: String) {
        _dialogMessage.value = message
    }

    private val _dialogInitialText = MutableStateFlow("")
    val dialogInitialText: StateFlow<String?> = _dialogInitialText

    fun setDialogInitialText(text: String) {
        _dialogInitialText.value = text
    }

    var dialogOnOkLambda: (suspend (String, SigmaViewModel, Context) -> Unit)? = null
    var dialogYesNoLambda: (suspend (Boolean, SigmaViewModel, Context) -> Unit)? = null
    var dialogTagLambda: (suspend (tagInfos: TagInfos?, vm: SigmaViewModel, context: Context) -> Unit)? =
        null

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

    //SELECTED ITEM
    private val _selectedItem = MutableStateFlow<Item?>(null)
    val selectedItem: StateFlow<Item?> = _selectedItem

    /**
     * Affecte item à SelectedItem
     */
    fun setSelectedItem(item: Item?, keepBottomToolsAsIs: Boolean = false) {
        _selectedItem.value = item

        if (!keepBottomToolsAsIs) {
            if (item != null)
                BottomTools.setCurrentContent(Tools.FILE)
            else
                BottomTools.setCurrentContent(DEFAULT)
        }
    }

    fun clearAllCaches() {
        clearImageCache()
        clearFlagCache()
        clearScalecache()
        clearMemoCache()
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

        val itemPath = selectedItemFullPath.value

        var pictureBitmap =
            if (newPicture is String) withContext(Dispatchers.IO) {
                changingPictureUseCase.urlToBitmap(newPicture)
            }
            else newPicture as Bitmap

        if (pictureBitmap == null)
            return

        //s'assure que les refreshs ci-dessous verront bien la nouvelle image
        removeImageFromCache(itemPath!!)

        val compositeMgr = CompositeManager(itemPath)
        compositeMgr.save(CroppedPicture(pictureBitmap, base64Embedder))

        if (!onlyCropped)
            compositeMgr.save(InitialPicture(pictureBitmap, base64Embedder))

        setImageCacheValue(itemPath, pictureBitmap)
    }

    fun goToFolder(folderPath: String, sorting: ITEMS_ORDERING_STRATEGY? = null) {
        sortingCache[currentFolderPath.value] = this.sorting.value

        if (sorting != null)
            setSorting(sorting)
        else
            setSorting(sortingCache[folderPath] ?: ITEMS_ORDERING_STRATEGY.DATE_DESC)

        clearImageCache()
        scaleCache.value.clear()
        clearMemoCache()
        clearFlagCache()
        DEFAULT.content().updateTools(emptyList<Tool>())

        viewModelScope.launch(Dispatchers.Main) {
            if (folderPath == currentFolderPath.value)
                refreshCurrentFolder()
            else
                addFolderPathToHistory(folderPath)
        }
    }

    init {
        viewModel = this
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

        viewModel = this
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
            val compositeMgr = CompositeManager(item.fullPath)
            compositeMgr.save(Flag(tag))

            removeFlagCacheForKey(item.fullPath)
            refreshCurrentFolder()
        }
    }

    suspend fun getInfoSup(item: Item): String {
        return withContext(Dispatchers.IO) {
            val infos = if (item is SigmaFolder) diskRepository
                .countFilesAndFolders(File(item.fullPath)).component1().toString() else item.name
                .substringAfterLast(".").toUpperCase(Locale.current)

            infos
        }
    }

    suspend fun getInfoInf(item: Item): String {
        return withContext(Dispatchers.IO) {
            val infos = if (item is SigmaFolder)
                viewModel.diskRepository.countFilesAndFolders(File(item.fullPath)).component2()
                    .toString()
            else formatFileSizeShort(viewModel.diskRepository.getSize(File(item.fullPath)))

            infos
        }
    }

    fun formatFileSizeShort(bytes: Long): String {
        if (bytes < 1024) return "${bytes}B"
        val z = (63 - java.lang.Long.numberOfLeadingZeros(bytes)) / 10
        val value = bytes.toDouble() / (1L shl (z * 10))
        return String.format("%.1f%c", value, " KMGTPE"[z])
    }
}

enum class ITEMS_ORDERING_STRATEGY {
    DATE_DESC,
    NAME_ASC
}

fun StateFlow<MutableMap<String, ColoredTag>>.containsFlagAsValue(valueId: UUID): Boolean {
    return valueId in this.value.values.map { it.id }
}