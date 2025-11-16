package lorry.folder.items.dossiersigma.ui.sigma

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.external.base64.IVideoInfoEmbedder
import lorry.folder.items.dossiersigma.external.capsule.CapsuleComponent
import lorry.folder.items.dossiersigma.external.capsule.utilities.CroppedPicture
import lorry.folder.items.dossiersigma.external.capsule.utilities.Flag
import lorry.folder.items.dossiersigma.external.capsule.utilities.InitialPicture
import lorry.folder.items.dossiersigma.external.disk.IDiskRepository
import lorry.folder.items.dossiersigma.external.playing.IPlayingDataSource
import lorry.folder.items.dossiersigma.basics.domain.ColoredTag
import lorry.folder.items.dossiersigma.basics.domain.Item
import lorry.folder.items.dossiersigma.basics.domain.SigmaPath
import lorry.folder.items.dossiersigma.headless.folderContentBack.IFolderContentBackComponent
import lorry.folder.items.dossiersigma.headless.usecases.pictures.ChangingPictureUseCase
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.ToolBarManager
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.Tool
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.toolbars.DEFAULT
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.toolbars.FILE
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.utils.IRawFeed
import lorry.folder.items.dossiersigma.ui.dialogs.TagInfos
import lorry.folder.items.dossiersigma.ui.settings.SettingsManager
import java.util.UUID
import javax.inject.Inject


@HiltViewModel
class SigmaViewModel @Inject constructor(
    val diskRepository: IDiskRepository,
    val changingPictureUseCase: ChangingPictureUseCase,
    val playingDataSource: IPlayingDataSource,
    val base64Embedder: IVideoInfoEmbedder,
    val folderContentComponent: IFolderContentBackComponent,
    val settingsManager: SettingsManager,
    val rawFeed: IRawFeed
) : ViewModel() {

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

    val tools = rawFeed.currentContent.map {
        it?.tools?.value
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000),
        initialValue = null
    )

    ////////////////////
    // maj de l'image //
    ////////////////////

    val currentMemo: StateFlow<String?> =
        folderContentComponent.currentFolderFlow
            .map { it?.memo }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = null
            )

    companion object {
        private val _refreshRequested = MutableSharedFlow<Unit>(replay = 0)
        val refreshRequested = _refreshRequested.asSharedFlow()

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        fun requestRefresh() {
            scope.launch(Dispatchers.Main) {
                _refreshRequested.emit(Unit)
            }
        }
    }

    init {
        viewModelScope.launch {
            refreshRequested.collect {
                folderContentComponent.reloadCurrentFolder()
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
                rawFeed.setCurrentContent(FILE)
            else
                rawFeed.setCurrentContent(DEFAULT)
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

        val itemPath = selectedItemFullPath.value

        if (itemPath == null)
            return

        var pictureBitmap =
            if (newPicture is String) withContext(Dispatchers.IO) {
                changingPictureUseCase.urlToBitmap(newPicture)
            }
            else newPicture as Bitmap

        if (pictureBitmap == null)
            return

        val capsuleMgr = CapsuleComponent()
        capsuleMgr.save(
            CroppedPicture(pictureBitmap, base64Embedder),
            itemPath
        )

        if (!onlyCropped)
            capsuleMgr.save(
                InitialPicture(pictureBitmap, base64Embedder),
                itemPath
            )

        folderContentComponent.reloadCurrentFolder()
    }

    fun goToFolder(folderPath: SigmaPath, sorting: SortingCriterion? = null) {
        folderContentComponent.manuallyInvalidateItems()
        folderContentComponent.setFastPath(folderPath)

        DEFAULT.content().updateTools(emptyList<Tool>())

        viewModelScope.launch(Dispatchers.Main) {

            if (folderPath == folderContentComponent.currentFolderFlow
                    .value?.fullPath
            )
                folderContentComponent.reloadCurrentFolder()
            else {
                //lèvera un event dans le service si différent
                settingsManager.saveCurrentPath(folderPath)
                folderContentComponent.addFolderPathToHistory(folderPath)
            }

            rawFeed.setCurrentFlagId(null)
        }
    }

    fun goToLastDifferentFolder(countToDelete: Int = 1) {
        val history = folderContentComponent.folderPathHistory.value
        if (history.size < 1 + countToDelete)
            return

        // avant-dernier élément la première fois
        val folderPath = history[history.size - 1 - countToDelete]

        viewModelScope.launch(Dispatchers.Main) {

            if (folderPath == folderContentComponent.currentFolderFlow
                    .value?.fullPath
            ) {
                //on répète car pas intéressant si même répertoire
                goToLastDifferentFolder(countToDelete + 1)
                return@launch
            } else {
                folderContentComponent.manuallyInvalidateItems()
                folderContentComponent.setFastPath(folderPath)

                DEFAULT.content().updateTools(emptyList<Tool>())

                //lèvera un event dans le service si différent
                settingsManager.saveCurrentPath(folderPath)
                folderContentComponent.removeNElementsFromHistory(countToDelete)

                rawFeed.setCurrentFlagId(null)
            }
        }
    }

    init {
        viewModelScope.launch {
            rawFeed.progress.collect { p ->
                if (p == 0 || p == 100)
                    rawFeed.updateMovePasteText("Coller")
                else
                    rawFeed.updateMovePasteText("$p %")
            }
        }

        viewModelScope.launch {
            rawFeed.nasProgress.collect { copyProgress ->
                if (copyProgress == null)
                    return@collect

                if (copyProgress.progress == 0 || copyProgress.progress == 100) {
                    rawFeed.updateNASText("1 -> NAS")
                    rawFeed.updateAllNASText("Tous -> NAS")
                } else {
                    rawFeed.updateNASText(
                        "${copyProgress.fileIndex + 1}/${copyProgress.fileSize}: ${copyProgress.progress} %"
                    )
                    rawFeed.updateAllNASText(
                        "${copyProgress.fileIndex + 1}/${copyProgress.fileSize}: ${copyProgress.progress} %"
                    )
                }
            }
        }

        rawFeed.setCurrentContent(DEFAULT)
    }

//    init {
//        val initialDirectoryPath = "/storage/emulated/0/Movies"
//        goToFolder(initialDirectoryPath, ITEMS_ORDERING_STRATEGY.DATE_DESC)
//    }

    fun playVideoFile(videoFullPath: SigmaPath) {
        viewModelScope.launch(Dispatchers.IO) {
            playingDataSource.playFile(videoFullPath, "video/mp4")
        }
    }

    fun playHtmlFile(htmlFullPath: SigmaPath) {
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
     * @see ToolBarManager.ToolBar
     */
    fun assignColoredTagToItem(item: Item, tag: ColoredTag) {
//        println("DRAG assignColoredTagToItem, item = ${item.name}, tag = ${tag.title}")

        viewModelScope.launch {
            val capsuleMgr = CapsuleComponent()
            capsuleMgr.save(
                Flag(tag),
                item.fullPath
            )

            folderContentComponent.reloadCurrentFolder()
        }
    }

    fun sendAriane() {

//        write {
//            log("ceci est un petit <b>pas</b> pour l' <b>homme</b>")
//        }

//        Logger().log("test")
//
//        val event = RichLogEvent(
//            timestampMillis = LocalDateTime.now().getLong(ChronoField.SECOND_OF_DAY),
//            tag = TagType("PAS"),
//            level = LevelType("INFO"),
//            message = MessageType("Ariane tisse sa toile")
//        )
//
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val socket = Socket("127.0.0.1", 7777)
//                socket.use { socket ->
//                    val writer = socket.getOutputStream().bufferedWriter(Charsets.UTF_8)
//
//                    val payload = Gson().toJson(event)
//                    writer.write(payload)
//                    writer.write("\n")
//                    writer.flush()
//                }
//            } catch (ex: Exception) {
//                ex.printStackTrace()
//                val msg = ex.message
//                println(msg)
//            }
//        }
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

