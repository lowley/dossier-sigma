package lorry.folder.items.dossiersigma.ui.sigma

import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import lorry.folder.items.dossiersigma.external.base64.IBase64DataSource
import lorry.folder.items.dossiersigma.external.base64.IVideoInfoEmbedder
import lorry.folder.items.dossiersigma.external.capsule.CapsuleComponent
import lorry.folder.items.dossiersigma.external.capsule.utilities.CroppedPicture
import lorry.folder.items.dossiersigma.external.capsule.utilities.Flag
import lorry.folder.items.dossiersigma.external.capsule.utilities.InitialPicture
import lorry.folder.items.dossiersigma.external.disk.IDiskRepository
import lorry.folder.items.dossiersigma.external.playing.IPlayingDataSource
import lorry.folder.items.dossiersigma.headless.domain.ColoredTag
import lorry.folder.items.dossiersigma.headless.domain.Item
import lorry.folder.items.dossiersigma.headless.domain.SigmaFolder
import lorry.folder.items.dossiersigma.headless.usecases.files.ChangePathUseCase
import lorry.folder.items.dossiersigma.headless.usecases.pictures.ChangingPictureUseCase
import lorry.folder.items.dossiersigma.ui.IndexBar.utilities.toIndexBarItemInfoList
import lorry.folder.items.dossiersigma.ui.bottomArea.BottomTools
import lorry.folder.items.dossiersigma.ui.bottomArea.TagInfos
import lorry.folder.items.dossiersigma.ui.bottomArea.Tool
import lorry.folder.items.dossiersigma.ui.bottomArea.Tools
import lorry.folder.items.dossiersigma.ui.bottomArea.Tools.DEFAULT
import lorry.folder.items.dossiersigma.ui.browser.IBrowser
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity.Companion.TAG
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SigmaViewModel @Inject constructor(
    val diskRepository: IDiskRepository,
    val changingPictureUseCase: ChangingPictureUseCase,
    val changePathUseCase: ChangePathUseCase,
    val playingDataSource: IPlayingDataSource,
    val base64DataSource: IBase64DataSource,
    val base64Embedder: IVideoInfoEmbedder,
    val bottomTools: BottomTools,
    val browser: IBrowser
) : ViewModel() {

    ////////////////
    // imageCache //
    ////////////////
    private val _imageCache = MutableStateFlow(mutableMapOf<String, Any?>())
    val imageCache: StateFlow<MutableMap<String, Any?>> = _imageCache

    fun setImageCacheValue(key: String, image: Any?) {
        if (image != null) {
            val newMap = _imageCache.value.toMutableMap()
            newMap[key] = image
            _imageCache.value = newMap

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

    val sortingCache = mutableMapOf<String, SortingCriterion>()

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

    //////////
    // mémo //
    //////////
    private val _isDisplayingMemoPalette = MutableStateFlow(false)
    val isDisplayingMemoPalette: StateFlow<Boolean> = _isDisplayingMemoPalette

    fun setIsDisplayingMemoPalette(isVisible: Boolean) {
        _isDisplayingMemoPalette.value = isVisible
    }

    private val _savedSelectedRange = MutableStateFlow<TextRange?>(null)
    val savedSelectedRange: StateFlow<TextRange?> = _savedSelectedRange

    fun setSavedSelectedRange(newSelection: TextRange?) {
        _savedSelectedRange.value = newSelection
    }

    /////////////////
    // text dialog //
    /////////////////
    private val _isTextDialogVisible = MutableStateFlow(false)
    val isTextDialogVisible: StateFlow<Boolean> = _isTextDialogVisible

    fun setIsTextDialogVisible(isVisible: Boolean) {
        _isTextDialogVisible.value = isVisible
    }

    ///////////////////
    // yes/no dialog //
    ///////////////////
    private val _isYesNoDialogVisible = MutableStateFlow(false)
    val isYesNoDialogVisible: StateFlow<Boolean> = _isYesNoDialogVisible

    fun setIsYesNoDialogVisible(isVisible: Boolean) {
        _isYesNoDialogVisible.value = isVisible
    }

    //////////////////////
    // move file dialog //
    //////////////////////
    private val _isMoveFileDialogVisible = MutableStateFlow(false)
    val isMoveFileDialogVisible: StateFlow<Boolean> = _isMoveFileDialogVisible

    fun setIsMoveFileDialogVisible(isVisible: Boolean) {
        _isMoveFileDialogVisible.value = isVisible
    }

    //////////////////////
    // tag infos dialog //
    //////////////////////
    private val _isTagInfosDialogVisible = MutableStateFlow(false)
    val isTagInfosDialogVisible: StateFlow<Boolean> = _isTagInfosDialogVisible

    fun setIsTagInfosDialogVisible(isVisible: Boolean) {
        _isTagInfosDialogVisible.value = isVisible
    }

    //////////////////////
    // Home item dialog //
    //////////////////////
    private val _isHomeItemDialogVisible = MutableStateFlow(false)
    val isHomeItemDialogVisible: StateFlow<Boolean> = _isHomeItemDialogVisible

    fun setIsHomeItemDialogVisible(isVisible: Boolean) {
        _isHomeItemDialogVisible.value = isVisible
    }

    /////////////////
    // File picker //
    /////////////////
    private val _isFilePickerVisible = MutableStateFlow(false)
    val isFilePickerVisible: StateFlow<Boolean> = _isFilePickerVisible

    fun setIsFilePickerVisible(isVisible: Boolean) {
        _isFilePickerVisible.value = isVisible
    }

    //////////////
    // settings //
    //////////////
    private val _isSettingsPageVisible = MutableStateFlow(false)
    val isSettingsPageVisible: StateFlow<Boolean> = _isSettingsPageVisible

    fun setIsSettingsPageVisible(visible: Boolean) {
        _isSettingsPageVisible.value = visible
    }

    fun toggleSettingsPageVisible() {
        _isSettingsPageVisible.value = !_isSettingsPageVisible.value
    }

    /////////////////
    // drag'n drop //
    /////////////////
    private val _dragTargetItem = MutableStateFlow<Item?>(null)
    val dragTargetItem: StateFlow<Item?> = _dragTargetItem

    fun setDragTargetItem(item: Item?) {
        _dragTargetItem.value = item
    }

    private val _dragState = MutableStateFlow<DragState?>(null)
    val dragState: StateFlow<DragState?> = _dragState

    fun beginDrag(tool: Tool, startOffset: Offset) {
        _dragState.value = DragState(tool, startOffset)
    }

    fun addDragOffset(delta: Offset) {
        _dragState.value?.let {
            _dragState.value = it.copy(offset = it.offset + delta)
        }
    }

    fun terminateDrag() {
        _dragState.value = null
    }

    private val _draggableStartPosition = MutableStateFlow<Offset?>(null)
    val draggableStartPosition: StateFlow<Offset?> = _draggableStartPosition

    fun setDraggableStartPosition(position: Offset?) {
        _draggableStartPosition.value = position
    }

    ///////////////////
    // tri des items //
    ///////////////////
    private val _sorting = MutableStateFlow(SortingCriterion.ByDateDesc)
    val sorting: StateFlow<SortingCriterion> = _sorting

    fun setSorting(sorting: SortingCriterion) {
        _sorting.value = sorting
    }

    val tools = bottomTools.currentContent.map {
        it?.tools?.value
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    ////////////////////
    // maj de l'image //
    ////////////////////
    private val _pictureUpdateId = MutableStateFlow(0)
    val pictureUpdateId: StateFlow<Int> = _pictureUpdateId

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

    val reloadTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentFolder: StateFlow<SigmaFolder> = combine(
        currentFolderPath,
        reloadTrigger,
        bottomTools.currentFlagId,
        sorting
    ) { path, _, currentFlagId, sorting ->
        Triple(path, currentFlagId, sorting)
    }.mapLatest { (path, currentFlagId, sorting) ->
        val folder = diskRepository.getSigmaFolder(path, sorting)


        clearAllCaches()
        folder.items.forEach { item ->
            val path = item.fullPath
            setImageCacheValue(path, item.picture)
            setFlagCacheValue(path, item.tag)
            setScaleCacheValue(path, item.scale)
            setMemoCacheValue(path, item.memo)
        }

        if (currentFlagId == null)
            folder
        else
            folder.copy(
                items = folder.items
                    .mapNotNull { item ->
                        if (item.tag?.id == currentFlagId) item else null
                    }
            )

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

    val modelFlow = combine(
        currentFolder,
        sorting
    ) { folder, sorting ->
        folder.items.toIndexBarItemInfoList(this)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
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
                bottomTools.setCurrentContent(Tools.FILE)
            else
                bottomTools.setCurrentContent(DEFAULT)
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

        val capsuleMgr = CapsuleComponent()
        capsuleMgr.save(
            CroppedPicture(pictureBitmap, base64Embedder),
            itemPath
        )

        if (!onlyCropped)
            capsuleMgr.save(
                InitialPicture(pictureBitmap, base64Embedder),
                itemPath)

        setImageCacheValue(itemPath, pictureBitmap)
    }

    fun goToFolder(folderPath: String, sorting: SortingCriterion? = null) {
        sortingCache[currentFolderPath.value] = this.sorting.value

        if (sorting != null)
            setSorting(sorting)
        else
            setSorting(sortingCache[folderPath] ?: SortingCriterion.ByDateDesc)

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

            bottomTools.setCurrentFlagId(null)
        }
    }

    init {
        bottomTools.viewModel = this
        viewModelScope.launch {
            bottomTools.progress.collect { p ->
                if (p == 0 || p == 100)
                    bottomTools.updateMovePasteText("Coller")
                else
                    bottomTools.updateMovePasteText("$p %")
            }
        }

        viewModelScope.launch {
            bottomTools.nasProgress.collect { copyProgress ->
                if (copyProgress == null)
                    return@collect

                if (copyProgress.progress == 0 || copyProgress.progress == 100) {
                    bottomTools.updateNASText("1 -> NAS")
                    bottomTools.updateAllNASText("Tous -> NAS")
                } else {
                    bottomTools.updateNASText(
                        "${copyProgress.fileIndex + 1}/${copyProgress.fileSize}: ${copyProgress.progress} %"
                    )
                    bottomTools.updateAllNASText(
                        "${copyProgress.fileIndex + 1}/${copyProgress.fileSize}: ${copyProgress.progress} %"
                    )
                }
            }
        }

        bottomTools.setCurrentContent(DEFAULT)
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
//
    }

    /**
     * se produit lors du drag'n drop d'une étiquette dans bottomTools
     * l'Item peut déjà contenir une étiquette -> modification
     * sinon -> ajout
     * @see BottomTools.BottomToolBar
     */
    fun assignColoredTagToItem(item: Item, tag: ColoredTag) {
//        println("DRAG assignColoredTagToItem, item = ${item.name}, tag = ${tag.title}")

        viewModelScope.launch {
            val capsuleMgr = CapsuleComponent()
            capsuleMgr.save(
                Flag(tag),
                item.fullPath)

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
                diskRepository.countFilesAndFolders(File(item.fullPath)).component2()
                    .toString()
            else formatFileSizeShort(diskRepository.getSize(File(item.fullPath)))

            infos
        }
    }

    fun formatFileSizeShort(bytes: Long): String {
        if (bytes < 1024) return "${bytes}B"
        val z = (63 - java.lang.Long.numberOfLeadingZeros(bytes)) / 10
        val value = bytes.toDouble() / (1L shl (z * 10))
        return String.format("%.1f%c", value, " KMGTPE"[z])
    }

    fun getClipboardText(context: Context): String? {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboard.primaryClip
        val text = clipData?.getItemAt(0)?.text?.toString()
        Log.d(TAG, "getClipboardText: $text")
        return text
    }
}

enum class SortingCriterion {
    ByDateDesc,
    ByNameAsc
}

fun StateFlow<MutableMap<String, ColoredTag>>.containsFlagAsValue(valueId: UUID): Boolean {
    return valueId in this.value.values.map { it.id }
}

data class DragState(
    val tool: Tool,
    val offset: Offset = Offset.Zero
)
