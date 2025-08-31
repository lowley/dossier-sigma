package lorry.folder.items.dossiersigma.ui.bottomArea

//region
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import coil.compose.AsyncImage
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.google.gson.Gson
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.external.base64.VideoInfoEmbedder
import lorry.folder.items.dossiersigma.external.capsule.CapsuleComponent
import lorry.folder.items.dossiersigma.external.capsule.utilities.CroppedPicture
import lorry.folder.items.dossiersigma.external.capsule.utilities.Flag
import lorry.folder.items.dossiersigma.external.capsule.utilities.InitialPicture
import lorry.folder.items.dossiersigma.external.capsule.utilities.Scale
import lorry.folder.items.dossiersigma.headless.domain.ColoredTag
import lorry.folder.items.dossiersigma.headless.domain.Item
import lorry.folder.items.dossiersigma.headless.moveToNasWorker.MoveToNASWorker
import lorry.folder.items.dossiersigma.headless.serviceVariants.moveToNAS.IMoveToNASComponent
import lorry.folder.items.dossiersigma.headless.serviceVariants.moveToNAS.ManifestEntry
import lorry.folder.items.dossiersigma.headless.services.MoveFileService
import lorry.folder.items.dossiersigma.headless.services.MoveToNASService
import lorry.folder.items.dossiersigma.ui.browser.changeState
import lorry.folder.items.dossiersigma.ui.browser.manageImageClick
import lorry.folder.items.dossiersigma.ui.browser.utilities.BrowserTarget
import lorry.folder.items.dossiersigma.ui.normal.imageAsAnyToTempUri
import lorry.folder.items.dossiersigma.ui.sigma.DragState
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity
import lorry.folder.items.dossiersigma.ui.sigma.SigmaColors
import lorry.folder.items.dossiersigma.ui.sigma.SigmaViewModel
import lorry.folder.items.dossiersigma.ui.sigma.SortingCriterion
import java.io.File
import java.util.UUID
import java.util.UUID.randomUUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

//endregion
/**
 * @startuml
 * (*) -> "BottomTools\n<color:red>  + Sticker" as A
 * A -> "Nouveau ColoredTag\n    dans flagCache" as B
 * B --> [  observeDefaultContent()] "MAJ BottomToolBar" as C
 *
 * C --> [  observation par BToolBar\n  de currentContextTools] "affichage des tags statiques" as D
 *
 * D --> [  si drag étiquette] "affichage tags mobiles\n<color:red> MOBILITE PASSIVE"
 * D --> [  filtrage\n clic sur flag] "MAJ BT.currentFlagId" as E
 *
 * E -> [clic = cancel] "BT.currentFlagId = null" as F
 * F -> [clic] E
 *
 * @enduml
 */
@Singleton
class BottomTools @Inject constructor(
    val moveToNASComponent: IMoveToNASComponent,
) {
    init {
        Tools.DEFAULT.bottomTools = this
        Tools.TAGS_MENU.bottomTools = this
        Tools.FILE.bottomTools = this
        Tools.MOVES.bottomTools = this
        Tools.COPY_FILE.bottomTools = this
        Tools.MOVE_FILE.bottomTools = this
        Tools.CROP.bottomTools = this
    }

    lateinit var viewModel: SigmaViewModel
    internal val defaultContent = BottomToolContent(emptyList(), "DEFAULT_CONTENT")
    private val _bottomToolsContent = MutableStateFlow<BottomToolContent?>(defaultContent)
    val currentContent: StateFlow<BottomToolContent?> = _bottomToolsContent

    fun setCurrentContent(tools: Tools) {
        setCurrentFlagId(null)
        _bottomToolsContent.value = when (tools) {
            Tools.DEFAULT -> defaultContent
            else -> tools.content(viewModel)
        }
    }

    //destiné à l'affichage par remontée dans MainActivity
    private val _currentTool = MutableStateFlow<Tool?>(null)
    val currentTool: StateFlow<Tool?> = _currentTool

    fun setCurrentTool(tool: Tool?) {
        _currentTool.value = tool
    }

    ////////////////////////
    // étiquette courante //
    ////////////////////////

    private val _currentFlagId = MutableStateFlow<UUID?>(null)
    val currentFlagId: StateFlow<UUID?> = _currentFlagId

    fun setCurrentFlagId(flagId: UUID?) {
        _currentFlagId.value = flagId
    }

    var movingItem: Item? = null
    var copyingItem: Item? = null
    var itemToMove: Item? = null

    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress

    /**
     * utilisé par
     * @see MoveFileService.copy
     */
    fun updateProgress(value: Int) {
        _progress.value = value
    }

    private val _movePasteText = MutableStateFlow("Coller")
    val movePasteText: StateFlow<String> = _movePasteText

    fun updateMovePasteText(value: String) {
        _movePasteText.value = value
    }

    private val _NASprogress = MutableStateFlow<OverallProgress?>(null)
    val nasProgress: StateFlow<OverallProgress?> = _NASprogress

    /**
     * utilisé par
     * @see MoveToNASService.copy
     */
    fun updateNASProgress(
        percentage: Int,
        fileIndex: Int,
        fileCount: Int
    ) {
        _NASprogress.value = OverallProgress(
            progress = percentage,
            fileIndex = fileIndex,
            fileSize = fileCount
        )
    }

    private val _copyNASText = MutableStateFlow("1 -> NAS")
    val copyNASText: StateFlow<String> = _copyNASText

    fun updateNASText(value: String) {
        _copyNASText.value = value
    }

    private val _copyAllNASText = MutableStateFlow("Tous -> NAS")
    val copyAllNASText: StateFlow<String> = _copyAllNASText

    fun updateAllNASText(value: String) {
        _copyAllNASText.value = value
    }

    @Composable
    fun BottomToolBar(
        activity: SigmaActivity
    ) {
        val content = currentContent.collectAsState().value
        val toolList = content?.tools?.collectAsState()?.value ?: emptyList()
        val modifier = Modifier.Companion
            .padding(vertical = 0.dp)

        Log.d(SigmaActivity.Companion.TAG, "Content: $content")
        Log.d(SigmaActivity.Companion.TAG, "BottomToolBar: ${toolList.size}")

        /*
//        @startuml
//        (*) -up-> "First Action"
//        -right-> "Second Action"
//        --> "Third Action"
//        -left-> (*)
        @enduml
         */

        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(SigmaColors.current.primary),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {

            toolList.forEach { tool ->
                //icône statique, toujours existante
                FixedSticker(
                    tool = tool,
                    activity = activity,
                )
            }
        }
    }

    fun observeDefaultContent(viewModel: SigmaViewModel) {
        this.viewModel = viewModel
        viewModel.viewModelScope.launch {
            // On combine les deux sources de données : le cache des tags et l'ID du tag sélectionné.
            // La lambda sera appelée si l'un ou l'autre change.
            combine(
                currentFlagId,
                viewModel.folderContentComponent.currentFolderFlow,
                viewModel.folderContentComponent.reloadTrigger
            ) { selectedId, currentFolder, _ ->
                val tags = currentFolder
                    ?.items
                    ?.mapNotNull { it.tag }
                    ?.distinctBy { it.id }
                    ?: emptyList()

                val tagTools = tags.map { tag ->
                    Tool(
                        text = { tag.title },
                        icon = R.drawable.etiquette,
                        tint = tag.color,
                        id = tag.id ?: randomUUID(),
                        onClick = { _, _ ->
                            // La logique est simplifiée : on change juste l'ID sélectionné.
                            // La recomposition se chargera de mettre à jour l'état "activated".
                            if (this.activated) {
                                setCurrentFlagId(null)
                            } else {
                                setCurrentFlagId(this.id)
                            }
                        },
                        // L'état "activé" est dérivé directement de la comparaison des IDs.
                        activated = selectedId != null && tag.id == selectedId
                    )
                }

                // 3. On combine les deux listes et on met à jour le singleton.
                defaultContent.updateTools(tagTools)

            }.collect() // Démarre la collecte du Flow combiné.
        }
    }
}

class BottomToolContent(
    var toolInit: List<Tool>,
    val name: String

) {
    private val _tools = MutableStateFlow(toolInit)
    val tools: StateFlow<List<Tool>> = _tools

    fun updateTools(newTools: List<Tool>) {
        _tools.value = newTools
    }

    fun addTool(tool: Tool, index: Int) {
        val oldList = _tools.value
        val newList = oldList.toMutableList()
        newList.add(index, tool)
        _tools.value = newList
    }

    fun removeTool(tool: Tool) {
        _tools.value = _tools.value - tool
    }

    fun replaceTool(tool: Tool) {
        val oldList = _tools.value
        val newList = oldList.toMutableList().map {
            if (it.id == tool.id)
                tool
            else
                it
        }
        _tools.value = newList
    }
}

// Outil unique avec icône, texte, et un comportement.
data class Tool(
    val text: () -> String,
    @DrawableRes val icon: Int,
    val isColoredIcon: Boolean = false,
    val onClick: suspend Tool.(SigmaViewModel, SigmaActivity) -> Unit,
    val visible: suspend (SigmaViewModel, SigmaActivity) -> Boolean = { _, _ -> true },
    val tint: Color? = null,
    val id: UUID = randomUUID(),
    val activated: Boolean = false
) {
    fun isActivated() = activated

}

fun Tool.toColoredTag(viewModel: SigmaViewModel? = null): ColoredTag = ColoredTag(
    id = this.id,
    title = this.text(),
    color = this.tint ?: Color.Companion.Unspecified,
)

sealed class Tools {

    abstract fun content(viewModel: SigmaViewModel? = null): BottomToolContent
    lateinit var bottomTools: BottomTools

    object DEFAULT : Tools() {
        override fun content(viewModel: SigmaViewModel?) = bottomTools.defaultContent
    }

    object TAGS_MENU : Tools(

    ) {
        override fun content(viewModel: SigmaViewModel?) = BottomToolContent(
            listOf(
                /////////////
                // ajouter //
                /////////////
                Tool(
                    text = { "Ajouter" },
                    icon = R.drawable.plus,
                    visible = { viewModel, mainActivity ->
                        val currentFolder = viewModel.folderContentComponent.currentFolderFlow.value
                        val selectedFolder = viewModel.folderContentComponent
                            .folderCacheFlow.value[viewModel.selectedItemFullPath.value]?.folder
                        val selectedFolderTag = selectedFolder?.tag

                        selectedFolder != null && selectedFolderTag == null
                    },
                    onClick = { viewModel, mainActivity ->
                        run {
                            val currentItem = viewModel.selectedItem.value
                            if (currentItem == null)
                                return@run

                            //viewModel.setSelectedItem(null)
                            viewModel.setDialogMessage("Entrez les informations du drapeau")
                            viewModel.dialogTagLambda = { tagInfos, viewModel, mainActivity ->
                                run {
                                    if (tagInfos == null)
                                        return@run

                                    val capsuleMgr = CapsuleComponent()

                                    val newFlag = ColoredTag(
                                        title = tagInfos.title,
                                        color = tagInfos.color,
                                        id = randomUUID(),
                                    )
                                    capsuleMgr.save(
                                        Flag(newFlag),
                                        currentItem.fullPath
                                    )

                                    viewModel.folderContentComponent.reloadCurrentFolder()
                                }

                                bottomTools.setCurrentContent(DEFAULT)
                                viewModel.setSelectedItem(null, true)
                            }

                            viewModel.setIsTagInfosDialogVisible(true)
                        }
                    }
                ),
                //////////////
                // modifier //
                //////////////
                Tool(
                    text = { "Modifier" },
                    icon = R.drawable.modifier,
                    visible = { viewModel, mainActivity ->
                        val currentFolder = viewModel.folderContentComponent.currentFolderFlow.value
                        val selectedFolder = viewModel.folderContentComponent
                            .folderCacheFlow.value[viewModel.selectedItemFullPath.value]?.folder
                        val selectedFolderTag = selectedFolder?.tag

                        selectedFolderTag != null
                    },
                    onClick = { viewModel, mainActivity ->
//                        viewModel.setDialogMessage("Nom du dossier à créer")
//                        viewModel.dialogOnOkLambda = { newName, viewModel, mainActivity ->
//                            val currentFolderPath = viewModel.currentFolderPath.value
//                            val newFullName = "$currentFolderPath/$newName"
//                            if (!File(newFullName).exists()) {
//                                if (File(newFullName).mkdir()) {
//                                    Toast.makeText(mainActivity, "Répertoire créé", Toast.LENGTH_SHORT).show()
//                                    viewModel.refreshCurrentFolder()
//                                } else
//                                    Toast.makeText(
//                                        mainActivity,
//                                        "Un problème est survenu",
//                                        Toast.LENGTH_SHORT
//                                    )
//                                        .show()
//                            }
//                        }
//
//                        mainActivity.openTextDialog.value = true
                    }
                ),
                ///////////////
                // supprimer //
                ///////////////
                Tool(
                    text = { "item" },
                    icon = R.drawable.moins,
                    visible = { viewModel, mainActivity ->
                        val currentFolder = viewModel.folderContentComponent.currentFolderFlow.value
                        val selectedFolder = viewModel.folderContentComponent
                            .folderCacheFlow.value[viewModel.selectedItemFullPath.value]?.folder
                        val selectedFolderTag = selectedFolder?.tag

                        selectedFolderTag != null
                    },
                    onClick = { viewModel, mainActivity ->
                        run {
                            val currentItem = viewModel.selectedItem.value
                            if (currentItem == null)
                                return@run

                            val selectedFolder = viewModel.folderContentComponent
                                .folderCacheFlow.value[viewModel.selectedItemFullPath.value]?.folder
                            val selectedFolderTag = selectedFolder?.tag
                            val tool = DEFAULT.content(viewModel)
                                .tools.value.firstOrNull { it.id == selectedFolderTag?.id }

                            if (tool == null) {
                                println("problème, tool inexistant")
                                return@run
                            }

                            //inutile car refresh plus loin
//                            if (viewModel.removeFlagCacheForKey(currentItem.fullPath) == null) {
//                                println("problème, suppression de tag impossible")
//                                return@run
//                            }

                            val capsuleMgr = CapsuleComponent()
                            capsuleMgr.save(
                                Flag(null),
                                currentItem.fullPath
                            )

                            val none = viewModel.folderContentComponent
                                .folderCacheFlow
                                ?.value
                                ?.none { it.value?.folder?.tag?.id == tool.id } == true

                            if (none)
                                DEFAULT.content(viewModel).removeTool(tool)

                            viewModel.setSelectedItem(null, true)
                            viewModel.folderContentComponent.reloadCurrentFolder()
                            bottomTools.setCurrentContent(DEFAULT)

//                            viewModel.clearFlagCache()
//                            DEFAULT.content().updateTools(emptyList<Tool>())
                        }
                    }
                ),
                ///////////////
                // supprimer //
                ///////////////
                Tool(
                    text = { "étiquette" },
                    icon = R.drawable.moins,
                    visible = { viewModel, mainActivity ->
                        val selectedFolder = viewModel.folderContentComponent
                            .folderCacheFlow.value[viewModel.selectedItemFullPath.value]?.folder
                        val selectedFolderTag = selectedFolder?.tag

                        selectedFolderTag != null
                    },
                    onClick = { viewModel, mainActivity ->
                        run {
                            val selectedFolder = viewModel.folderContentComponent
                                .folderCacheFlow.value[viewModel.selectedItemFullPath.value]?.folder
                            val selectedFolderTag = selectedFolder?.tag

                            val tool = DEFAULT.content(viewModel)
                                .tools.value.firstOrNull { it.id == selectedFolderTag?.id }

                            if (tool == null) {
                                println("problème, tool inexistant")
                                return@run
                            }


                            val itemsWithThisTag = viewModel.folderContentComponent
                                ?.currentFolderFlow
                                ?.value
                                ?.items
                                ?.filter { item ->
                                   item?.tag?.id == tool.id
                                }

                            //on fait ça parce que par lazy loading au début de l'affichage
                            //du dossier de tous les items
//                            val itemsWithThisTag =
//                                viewModel.displayedItemsFlow.value.second.filter {
//                                    val capsuleMgr = CapsuleComponent()
//                                    val tagFile = capsuleMgr.getElement(
//                                        Flag.Companion,
//                                        it.fullPath
//                                    )
//
//                                    val tagCache = viewModel.flagCache.value[it.fullPath]
//
//                                    val tagFinal = tagCache ?: tagFile
//                                    tagFinal?.id == tool.id
//                                }

                            itemsWithThisTag?.forEach {
                                val capsuleMgr = CapsuleComponent()
                                capsuleMgr.save(Flag(null), it.fullPath)
                            }

                            //normalement toujours vrai
//                            if (!viewModel.flagCache.containsFlagAsValue(tool.id))
//                                DEFAULT.content(viewModel).removeTool(tool)

                            viewModel.setSelectedItem(null, true)
//                            viewModel.refreshCurrentFolder()
                            bottomTools.setCurrentContent(DEFAULT)

                            viewModel.folderContentComponent.reloadCurrentFolder()
                        }
                    }
                ),
                ////////////////////
                // supprimer tous //
                ////////////////////
                Tool(
                    text =
                        { "carnage" },
                    icon = R.drawable.moins,
                    visible =
                        { viewModel, mainActivity ->
                            val selectedFolder = viewModel.folderContentComponent
                                .folderCacheFlow.value[viewModel.selectedItemFullPath.value]?.folder
                            val selectedFolderTag = selectedFolder?.tag

                            selectedFolderTag != null
                        },
                    onClick =
                        { viewModel, mainActivity ->
                            run {
                                val selectedFolder = viewModel.folderContentComponent
                                    .folderCacheFlow.value[viewModel.selectedItemFullPath.value]?.folder
                                val selectedFolderTag = selectedFolder?.tag

                                val tool = DEFAULT.content(viewModel)
                                    .tools.value.firstOrNull { it.id == selectedFolderTag?.id }

                                if (tool == null) {
                                    println("problème, tool inexistant")
                                    return@run
                                }

                                val itemsWithThisTag = viewModel.folderContentComponent
                                    ?.currentFolderFlow
                                    ?.value
                                    ?.items

                                //on fait ça parce que par lazy loading au début de l'affichage
                                //du dossier de tous les items
//                            val itemsWithThisTag =
//                                viewModel.displayedItemsFlow.value.second.filter {
//                                    val capsuleMgr = CapsuleComponent()
//                                    val tagFile = capsuleMgr.getElement(
//                                        Flag.Companion,
//                                        it.fullPath
//                                    )
//
//                                    val tagCache = viewModel.flagCache.value[it.fullPath]
//
//                                    val tagFinal = tagCache ?: tagFile
//                                    tagFinal?.id == tool.id
//                                }

                                itemsWithThisTag?.forEach {
                                    val capsuleMgr = CapsuleComponent()
                                    capsuleMgr.save(Flag(null), it.fullPath)
                                }

                                //normalement toujours vrai
//                            if (!viewModel.flagCache.containsFlagAsValue(tool.id))
//                                DEFAULT.content(viewModel).removeTool(tool)

                                viewModel.setSelectedItem(null, true)
//                            viewModel.refreshCurrentFolder()
                                bottomTools.setCurrentContent(DEFAULT)

                                viewModel.folderContentComponent.reloadCurrentFolder()
                            }
                        }
                )
            ),
            "TAGS_MENU"
        )
    }

    object FILE : Tools() {
        override fun content(viewModel: SigmaViewModel?) = BottomToolContent(
            toolInit = listOf(
                ///////////
                // moves //
                ///////////
                Tool(
                    text = { "Déplacements" },
                    icon = R.drawable.move,
                    isColoredIcon = true,
                    onClick = { viewModel, mainActivity ->
                        bottomTools.setCurrentContent(MOVES)
                    }
                ),
                ///////////////
                // tags menu //
                ///////////////
                Tool(
                    text = { "Etiquettes" },
                    icon = R.drawable.etiquette2,
                    isColoredIcon = true,
                    onClick = { viewModel, mainActivity ->
                        bottomTools.setCurrentContent(TAGS_MENU)
                    }
                ),
                //////////////////
                // image google //
                //////////////////
                Tool(
                    text = { "Google" },
                    icon = R.drawable.browser,
                    onClick = { viewModel, mainActivity ->
                        run {
                            val selectedItem = viewModel.selectedItem.value
                            if (selectedItem == null)
                                return@run

                            bottomTools.setCurrentContent(DEFAULT)

                            //le [[browserBody]] dépend de browserState (dataclass)
                            //ici il y a #[[browserModification]]
                            mainActivity.browser.changeState(
                                isOpen = true,
                                item = selectedItem,
                                target = BrowserTarget.GOOGLE,
                                onImageClicked = { url ->
                                    viewModel.viewModelScope.launch {
                                        manageImageClick(viewModel, url)
                                        viewModel.setSelectedItem(null, true)
                                    }

                                }
                            )
                        }
                    }
                ),
                //////////////
                // recadrer //
                //////////////
                Tool(
                    text = { "Placement" },
                    icon = R.drawable.recadrer2,
                    isColoredIcon = true,
                    onClick = { viewModel, mainActivity ->
                        bottomTools.setCurrentContent(CROP)
                    }
                ),
                //////////////
                // renommer //
                //////////////
                Tool(
                    text = { "Renommer" },
                    icon = R.drawable.renommer,
                    onClick = { viewModel, mainActivity ->
                        val currentFolderPath = viewModel.selectedItem.value?.fullPath
                        val currentItemName = currentFolderPath?.substringAfterLast("/") ?: ""
                        //viewModel.setSelectedItem(null)
                        viewModel.setDialogMessage("Nouveau nom du dossier")
                        viewModel.setDialogInitialText(currentItemName)
                        viewModel.dialogOnOkLambda = { newName, viewModel, mainActivity ->
                            run {
                                if (currentFolderPath == null || newName == currentFolderPath.substringAfterLast(
                                        "/"
                                    )
                                ) {
                                    Toast.makeText(
                                        mainActivity,
                                        "Le nouveau nom doît être différent de l'ancien",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@run
                                }

                                val newFullName = "${
                                    currentFolderPath.substringBeforeLast("/")
                                }/$newName"
                                println("NOM: $newFullName")
                                if (File(currentFolderPath).exists()) {
                                    if (File(currentFolderPath).renameTo(File(newFullName))) {
                                        Toast.makeText(
                                            mainActivity,
                                            "Renommage effectué",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        viewModel.folderContentComponent.reloadCurrentFolder()
                                    } else
                                        Toast.makeText(
                                            mainActivity,
                                            "Un problème lors du renommage est survenu",
                                            Toast.LENGTH_LONG
                                        )
                                            .show()
                                }
                            }

                            bottomTools.setCurrentContent(DEFAULT)
                            viewModel.setSelectedItem(null, true)
                        }

                        viewModel.setIsTextDialogVisible(true)
                    }
                ),
                /////////////////////
                // + dossier frère //
                /////////////////////
                Tool(
                    text = { "+ frère" },
                    icon = R.drawable.dossier,
                    onClick = { viewModel, mainActivity ->
                        val parent = viewModel.folderContentComponent
                            .currentFolderFlow.value
                        val items = parent?.items ?: emptyList()

                        if (parent == null)
                            return@Tool

                        //viewModel.setSelectedItem(null)
                        viewModel.setDialogMessage("Nouveau nom du dossier")
                        viewModel.dialogOnOkLambda = { newName, viewModel, mainActivity ->
                            run {
                                val parentPath = parent.fullPath
                                val children = items
                                    .map { item -> item.fullPath }
                                if (children.any { child -> child.substringAfterLast("/") == newName }
                                ) {
                                    Toast.makeText(
                                        mainActivity,
                                        "Un élément du dossier actuel porte le même nom",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    return@run
                                }

                                val newFullPath = "$parentPath/$newName"

                                if (File(newFullPath).mkdir()) {
                                    Toast.makeText(
                                        mainActivity,
                                        "Dossier créé",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    viewModel.folderContentComponent.reloadCurrentFolder()
                                } else
                                    Toast.makeText(
                                        mainActivity,
                                        "Un problème lors de la création  du dossier frère est survenu",
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                            }

                            bottomTools.setCurrentContent(DEFAULT)
                            viewModel.setSelectedItem(null, true)
                        }

                        viewModel.setIsTextDialogVisible(true)
                    }
                ),
                ////////////////////
                // + dossier fils //
                ////////////////////
                Tool(
                    text = { "+ fils" },
                    icon = R.drawable.dossier,
                    onClick = { viewModel, mainActivity ->
                        viewModel.setDialogMessage("Nouveau nom du dossier")
                        viewModel.dialogOnOkLambda = { newName, viewModel, mainActivity ->
                            run {
                                val selectedItemPath = viewModel.selectedItem.value?.fullPath
                                if (selectedItemPath == null)
                                    return@run

                                var children: List<String> = emptyList()

                                children = viewModel.diskRepository
                                    .getFolderItems(
                                        selectedItemPath,
                                        SortingCriterion.ByDateDesc
                                    )
                                    .map { item -> item.fullPath }

                                if (children.any { child -> child.substringAfterLast("/") == newName }
                                ) {
                                    Toast.makeText(
                                        mainActivity,
                                        "Un élément du dossier sélectionné porte le même nom",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    return@run
                                }

                                val newFullPath = "$selectedItemPath/$newName"

                                if (File(newFullPath).mkdir() &&
                                    File(newFullPath).exists()
                                ) {
                                    Toast.makeText(
                                        mainActivity,
                                        "Dossier créé",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    viewModel.folderContentComponent.reloadCurrentFolder()
                                } else
                                    Toast.makeText(
                                        mainActivity,
                                        "Un problème lors de la création  du dossier enfant est survenu",
                                        Toast.LENGTH_LONG
                                    ).show()
                            }

                            bottomTools.setCurrentContent(DEFAULT)
                            viewModel.setSelectedItem(null, true)

                        }

                        viewModel.setIsTextDialogVisible(true)

                    }
                ),
                ///////////////
                // supprimer //
                ///////////////
                Tool(
                    text = { "Supprimer" },
                    icon = R.drawable.corbeille,
                    onClick = { viewModel, mainActivity ->
                        val currentFolderPath = viewModel.selectedItemFullPath.value
                        //viewModel.setSelectedItem(null)
                        viewModel.setDialogMessage(
                            "Voulez-vous vraiment supprimer ce ${
                                if (viewModel
                                        .selectedItem.value?.isFile() != false
                                ) "fichier" else "dossier"
                            } ?"
                        )
                        viewModel.dialogYesNoLambda = { yesNo, viewModel, mainActivity ->
                            run {
                                if (!yesNo)
                                    return@run

                                val item = viewModel.selectedItem.value
                                val itemFullPath = viewModel.selectedItemFullPath.value
                                if (item == null)
                                    return@run

                                if (item.isFolder())
                                    File(item.fullPath).deleteRecursively()
                                else File(item.fullPath).delete()

                                viewModel.setSelectedItem(null, true)

                                if (File(itemFullPath).exists())
                                    Toast.makeText(
                                        mainActivity,
                                        "Un problème lors de la suppression est survenu",
                                        Toast.LENGTH_LONG
                                    ).show()
                                else Toast.makeText(
                                    mainActivity,
                                    "Suppression effectuée",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            viewModel.folderContentComponent.reloadCurrentFolder()
                            bottomTools.setCurrentContent(DEFAULT)
                        }

                        viewModel.setIsYesNoDialogVisible(true)
                    }

                ),
            ),
            "FILE"
        )
    }

    object MOVES : Tools() {
        override fun content(viewModel: SigmaViewModel?) = BottomToolContent(
            listOf(
                ////////////
                // copier //
                ////////////
                Tool(
                    text = { "Copier" },
                    icon = R.drawable.copier,
                    onClick = { viewModel, mainActivity ->


                        //vm.diskRepository.copyFile(sourceFile, destinationFile)
                    }
                ),
                //////////////
                // déplacer //
                //////////////
                Tool(
                    text = { "Déplacer" },
                    icon = R.drawable.deplacer,
                    onClick = { viewModel, mainActivity ->
                        bottomTools.movingItem = viewModel.selectedItem.value
                        bottomTools.setCurrentContent(MOVE_FILE)
                        viewModel.setSelectedItem(null, keepBottomToolsAsIs = true)
                    }
                ),
                /////////////////////
                // déplacement NAS //
                /////////////////////
                Tool(
                    text = {
                        val nasText = bottomTools.copyNASText.value
                        nasText
                    },
                    icon = R.drawable.deplacer,
                    onClick = { viewModel, mainActivity ->
                        run {
                            bottomTools.itemToMove = viewModel.selectedItem.value

                            if (bottomTools.itemToMove == null)
                                return@run

                            //toast
                            println("MovingItem: choisir fichier destination")

                            /**
                             * le fichier n'existe pas, on lance la copie,
                             * le reste est effectué dans
                             * @see MoveFileService.onStartCommand
                             */

                            //encode/decode en json

                            /////////////
                            // current //
                            /////////////

                            val picture = viewModel?.folderContentComponent
                                ?.folderCacheFlow?.value[viewModel.selectedItemFullPath.value]
                                ?.folder?.picture

//                            val picture =
//                                viewModel.imageCache.value[viewModel.selectedItemFullPath.value]
                            val picture64 = if (picture != null && picture is Bitmap)
                                viewModel.base64Embedder.bitmapToBase64(picture as Bitmap)
                            else null

                            val filesToTransfer = bottomTools.itemToMove?.fullPath?.let {
                                listOf(it to picture64)
                            } ?: emptyList()

                            //* aire des images enregistrées dans un fichier
                            //* pour transfert à CopieurTho2
                            val entries =
                                filesToTransfer.map<Pair<String, String?>, ManifestEntry> {
                                    ManifestEntry(fullPath = it.first, picture64 = it.second)
                                }

                            // 2) Écrire le JSON dans un fichier temporaire de cache interne
                            val manifestFile =
                                File(mainActivity.cacheDir, "transfer_manifest.json").apply {
                                    writeText(Gson().toJson(entries))
                                }

                            // 3) Obtenir l’URI de partage via FileProvider
                            val authority = "${mainActivity.packageName}.provider"
                            val contentUri =
                                FileProvider.getUriForFile(mainActivity, authority, manifestFile)
                            //* fin aire des images enregistrées dans un fichier

                            val nasDirectory =
                                mainActivity.settingsViewModel.settings.nasFolderFlow.firstOrNull()
                                    ?: ""

                            val req = MoveToNASWorker.request(
                                manifestPath = manifestFile.absolutePath,
                                target = nasDirectory,
                                manifestUri = contentUri.toString()
                            )

                            WorkManager.getInstance(mainActivity)
                                .enqueueUniqueWork(
                                    "move-to-nas",
                                    ExistingWorkPolicy.KEEP,
                                    req
                                )

//                            bottomTools.moveToNASComponent.startService(
//                                filesToTransfer = filesToTransfer,
//                                manifestUri = contentUri.toString(),
//                                nasDirectory = nasDirectory,
//                                changeBottomTools = { percentage: Int, index: Int, total: Int ->
//                                    bottomTools.updateNASProgress(
//                                        percentage = percentage,
//                                        fileIndex = index,
//                                        fileCount = total
//                                    )
//                                }
//                            )

                            ////////////
                            // legacy //
                            ////////////

//                            val intent = Intent(mainActivity, MoveToNASService::class.java).apply {
//                                putExtra(
//                                    "filesToTransfer", Gson().toJson(
//                                        listOf(
//                                            BottomTools.itemToMove?.fullPath ?: ""
//                                        )
//                                    )
//                                )
//                                putExtra(
//                                    "nasDirectory",
//                                    mainActivity.settingsViewModel.settingsManager.nasFolderFlow.firstOrNull()
//                                )
//                            }
//                            mainActivity.startService(intent)
                        }
                    }
                )
            ),
            "MOVES"
        )
    }

    object COPY_FILE : Tools() {
        override fun content(viewModel: SigmaViewModel?) = BottomToolContent(
            listOf(
                /////////////
                // annuler //
                /////////////
                Tool(
                    text = { "Annuler" },
                    icon = R.drawable.annuler,
                    onClick = { viewModel, mainActivity ->
                        bottomTools.setCurrentContent(DEFAULT)
                    }
                ),
                ////////////
                // coller //
                ////////////
                Tool(
                    text = { "Coller" },
                    icon = R.drawable.coller,
                    onClick = { viewModel, mainActivity ->
                        //vm.diskRepository.copyFile(sourceFile, destinationFile)
                        bottomTools.setCurrentContent(DEFAULT)
                    }
                )
            ),
            "COPY_FILE"
        )
    }

    object MOVE_FILE : Tools() {
        override fun content(viewModel: SigmaViewModel?) = BottomToolContent(
            listOf(
                /////////////
                // annuler //
                /////////////
                Tool(
                    text = { "Annuler" },
                    icon = R.drawable.annuler,
                    onClick = { viewModel, mainActivity ->
                        bottomTools.setCurrentContent(DEFAULT)
                        val item = bottomTools.movingItem
                        val movingParent = item?.fullPath?.substringBeforeLast("/")

                        if (movingParent != null)
                            viewModel.goToFolder(movingParent)
                        bottomTools.movingItem = null
                        viewModel.setSelectedItem(null, true)
//                        viewModel.refreshCurrentFolder()
                    }
                ),
                ////////////
                // coller //
                ////////////
                Tool(
                    text = {
                        val movePasteText = bottomTools.movePasteText.value
                        movePasteText
                    },
                    icon = R.drawable.coller,
                    onClick = { viewModel, mainActivity ->
                        run {
                            bottomTools.itemToMove = viewModel.selectedItem.value
                            var dest = bottomTools.itemToMove

                            if (dest == null) {
                                return@run
//                                bottomTools.itemToMove = viewModel.currentFolder.value
//                                dest = bottomTools.itemToMove
                            }

                            //toast
                            println("MovingItem: choisir fichier destination")
                            //1.copie
                            val sourceFile = File(bottomTools.movingItem?.fullPath ?: "")
                            //créer service avec notification(avec avancement)
                            //dans le service: copie
                            //passer au service une lambda pour l'action de retour(2.+3.)

                            //Toast pour informer de déplacement:
                            //début copie, fin déplacement/échec

                            if (dest!!.isFile()) {
                                if (sourceFile.path.substringAfterLast("/")
                                    == dest.fullPath.substringAfterLast("/")
                                ) {
                                    viewModel.setIsMoveFileDialogVisible(true)
                                    return@run
                                }
                            }

                            if (dest.isFolder()) {
                                if (bottomTools.movingItem == null)
                                    return@run
                                val isItemExists = viewModel.diskRepository.isFileOrFolderExists(
                                    dest.fullPath,
                                    bottomTools.movingItem!!
                                )
                                if (isItemExists) {
                                    viewModel.setIsMoveFileDialogVisible(true)
                                    return@run
                                }
                            }

                            /**
                             * le fichier n'existe pas, on lance la copie,
                             * le reste est effectué dans
                             * @see MoveFileService.onStartCommand
                             */
                            val intent = Intent(mainActivity, MoveFileService::class.java).apply {
                                putExtra("source", bottomTools.movingItem?.fullPath ?: "")
                                putExtra("destination", dest.fullPath)
                                putExtra("addSuffix", "")
                            }
                            mainActivity.startService(intent)
//                            viewModel.setSelectedItem(null, true)
                            viewModel.folderContentComponent.reloadCurrentFolder()
                            //2.vérif copie bien réalisée:
                            //dest existe
                            //tailles égales

                            //3.si ok: suppression source


                            //vm.diskRepository.copyFile(sourceFile, destinationFile)
//                        bottomTools.setCurrentContent(DEFAULT, viewModel)
//                        MovingItem = null
                        }
                    }
                )
            ),
            "MOVE_FILE"
        )
    }

    object CROP : Tools() {
        override fun content(viewModel: SigmaViewModel?) = BottomToolContent(
            toolInit = listOf(
                Tool(
                    text = { "Aucun" },
                    icon = R.drawable.crop,
                    onClick = { viewModel, mainActivity ->
                        changeCrop(viewModel, ContentScale.Companion.None)
                    }
                ),

                Tool(
                    text = { "Rogner" },
                    icon = R.drawable.crop,
                    onClick = { viewModel, mainActivity ->
                        changeCrop(viewModel, ContentScale.Companion.Crop)
                    }
                ),

                Tool(
                    text = { "Remplir ⇅" },
                    icon = R.drawable.crop,
                    onClick = { viewModel, mainActivity ->
                        changeCrop(viewModel, ContentScale.Companion.FillHeight)
                    }
                ),

                Tool(
                    text = { "Remplir ⇿" },
                    icon = R.drawable.crop,
                    onClick = { viewModel, mainActivity ->
                        changeCrop(viewModel, ContentScale.Companion.FillWidth)
                    }
                ),

                Tool(
                    text = { "Etirer" },
                    icon = R.drawable.crop,
                    onClick = { viewModel, mainActivity ->
                        changeCrop(viewModel, ContentScale.Companion.Fit)
                    }
                ),

                Tool(
                    text = { "Dedans" },
                    icon = R.drawable.crop,
                    onClick = { viewModel, mainActivity ->
                        changeCrop(viewModel, ContentScale.Companion.Inside)
                    }
                ),

                Tool(
                    text = { "Manuel" },
                    icon = R.drawable.image,
                    isColoredIcon = true,
                    onClick = { viewModel, mainActivity ->
                        run {
                            val item = viewModel.selectedItem.value
                            var sourceBitmap: Any? = null

                            if (item == null)
                                return@run

                            val capsuleMgr = CapsuleComponent()
                            sourceBitmap = capsuleMgr.getElement(
                                InitialPicture.Companion,
                                item.fullPath
                            )
                            val test = capsuleMgr.getElement(
                                CroppedPicture.Companion,
                                item.fullPath
                            )

                            if (sourceBitmap == null && test != null) {
                                capsuleMgr.save(
                                    InitialPicture(test, VideoInfoEmbedder()),
                                    item.fullPath
                                )
                                sourceBitmap = test
                            }

                            if (sourceBitmap == null)
                                return@run

                            val sourceUri = imageAsAnyToTempUri(mainActivity, sourceBitmap)
                            val destinationUri =
                                Uri.fromFile(
                                    File.createTempFile(
                                        "cropped_", ".jpg",
                                        mainActivity.cacheDir
                                    )
                                )

                            //le callback est dans MainActivity : onActivityResult (override)
                            UCrop.of(sourceUri, destinationUri)
                                .withAspectRatio(1f, 1f)
                                .withMaxResultSize(175, 175)
                                .start(mainActivity)
                        }
                    }
                ),
            ),
            "CROP"
        )
    }

//    object SHORTCUTS: Tools(BottomToolContent(
//
//    ))

}

fun changeCrop(viewModel: SigmaViewModel, scale: ContentScale) {
    val item = viewModel.selectedItem.value ?: return
    viewModel.setSelectedItem(item.copy(scale = scale))

    if (item.isFile() &&
        item.fullPath.endsWith(".mp4") ||
        item.fullPath.endsWith(".avi") ||
        item.fullPath.endsWith(".mpg") ||
        item.fullPath.endsWith(".html") ||
        item.fullPath.endsWith(".iso") ||
        item.fullPath.endsWith(".mkv")
    ) {
        viewModel.viewModelScope.launch {
            val capsuleMgr = CapsuleComponent()
            capsuleMgr.save(
                Scale(scale),
                item.fullPath
            )
        }
    }

    if (item.isFolder()) {
        viewModel.viewModelScope.launch {
            val file = File(item.fullPath + "/.folderPicture.html")
            if (!file.exists())
                viewModel.diskRepository.createFolderHtmlFile(item)

            val capsuleMgr = CapsuleComponent()
            capsuleMgr.save(
                Scale(scale),
                item.fullPath
            )
//            viewModel.refreshCurrentFolder()
        }
    }

//    viewModel.notifyPictureUpdated()
//    viewModel.setSelectedItem(null)
//    BottomTools.setCurrentContent(DEFAULT)
}

@Composable
fun CustomTextDialog(
    text: String,
    initialText: String,
    viewModel: SigmaViewModel,
    onOk: (String) -> Unit,
) {
    val editMessage = remember { mutableStateOf(initialText) }
    val focusRequester = remember { FocusRequester() }

    Box(
        modifier = Modifier.Companion
            .fillMaxSize()
            .background(
                color = contentColorFor(Color.Companion.White)
                    .copy(alpha = 0.6f)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    viewModel.setIsTextDialogVisible(false)
                }
            ),
        contentAlignment = Alignment.Companion.Center
    ) {
        Column(
            modifier = Modifier.Companion
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Companion.White)
                .padding(8.dp),
        ) {

            Text(
                modifier = Modifier.Companion,
                text = text,
                color = Color.Companion.Black
            )

            Spacer(modifier = Modifier.Companion.height(8.dp))

            TextField(
                modifier = Modifier
                    .focusRequester(focusRequester),
                value = editMessage.value,
                onValueChange = { editMessage.value = it },
                singleLine = true
            )

            Spacer(modifier = Modifier.Companion.height(8.dp))

            Row(
                modifier = Modifier.Companion.align(Alignment.Companion.End)
            ) {
                Button(
                    onClick = {
                        viewModel.setIsTextDialogVisible(false)
                    }
                ) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.Companion.width(8.dp))

                Button(
                    onClick = {
                        onOk(editMessage.value)
                        viewModel.setIsTextDialogVisible(false)
                        viewModel.setDialogInitialText("")
                    }
                ) {
                    Text("OK")
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        // après composition → demande le focus
        focusRequester.requestFocus()
    }
}

@Composable
fun CustomYesNoDialog(
    text: String,
    viewModel: SigmaViewModel,
    onOk: (Boolean) -> Unit
) {
    val editMessage = remember { mutableStateOf("") }

    Box(
        modifier = Modifier.Companion
            .fillMaxSize()
            .background(
                color = contentColorFor(Color.Companion.White)
                    .copy(alpha = 0.6f)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    viewModel.setIsYesNoDialogVisible(false)
                }
            ),
        contentAlignment = Alignment.Companion.Center
    ) {
        Column(
            modifier = Modifier.Companion
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                .background(Color.Companion.White)
                .padding(8.dp),
        ) {

            Text(
                modifier = Modifier.Companion,
                text = text,
                color = Color.Companion.Black
            )

            Spacer(modifier = Modifier.Companion.height(8.dp))

            Row(
                modifier = Modifier.Companion.align(Alignment.Companion.End)
            ) {
                Button(
                    onClick = {
                        viewModel.setIsYesNoDialogVisible(false)
                        onOk(false)
                    }
                ) {
                    Text("Non")
                }

                Spacer(modifier = Modifier.Companion.width(8.dp))

                Button(
                    onClick = {
                        viewModel.setIsYesNoDialogVisible(false)
                        onOk(true)
                    }
                ) {
                    Text("Oui")
                }
            }
        }
    }
}

@Composable
fun CustomMoveFileExistingDestinationDialog(
    text: String = "Le fichier existe déjà. Que voulez-vous faire?",
    onOverwrite: () -> Unit,
    onCancel: () -> Unit,
    onCreateCopy: () -> Unit,
    viewModel: SigmaViewModel,
) {
    val editMessage = remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    Box(
        modifier = Modifier.Companion
            .fillMaxSize()
            .background(
                color = contentColorFor(Color.Companion.White)
                    .copy(alpha = 0.6f)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    viewModel.setIsMoveFileDialogVisible(false)
                }
            ),
        contentAlignment = Alignment.Companion.Center
    ) {
        Column(
            modifier = Modifier.Companion
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                .background(Color.Companion.White)
                .padding(8.dp),
        ) {

            Text(
                modifier = Modifier.Companion,
                text = text,
                color = Color.Companion.Black
            )

            Spacer(modifier = Modifier.Companion.height(8.dp))

            Row(
                modifier = Modifier.Companion.align(Alignment.Companion.End)
            ) {
                Button(
                    onClick = {
                        viewModel.setIsMoveFileDialogVisible(false)
                        onCancel()
                    }
                ) {
                    Text("Abandonner")
                }

                Spacer(modifier = Modifier.Companion.width(8.dp))

                Button(
                    onClick = {
                        viewModel.setIsMoveFileDialogVisible(false)
                        onCreateCopy()
                    }
                ) {
                    Text("Créer une copie")
                }

                Spacer(modifier = Modifier.Companion.width(8.dp))

                Button(
                    onClick = {
                        viewModel.setIsMoveFileDialogVisible(false)
                        onOverwrite()
                    }
                ) {
                    Text("Ecraser")
                }
            }
        }
    }
}

@Composable
fun TagInfosDialog(
    text: String,
    onDatasCompleted:
    suspend (tagInfos: TagInfos?, viewModel: SigmaViewModel, activity: SigmaActivity) -> Unit,
    viewModel: SigmaViewModel,
    mainActivity: SigmaActivity
) {
    val editMessage = remember { mutableStateOf("") }
    var hexColor by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier.Companion
            .fillMaxSize()
            .background(
                color = contentColorFor(Color.Companion.White)
                    .copy(alpha = 0.6f)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    viewModel.setIsTagInfosDialogVisible(false)
                }
            ),
        contentAlignment = Alignment.Companion.Center
    ) {
        Column(
            modifier = Modifier.Companion
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                .background(Color.Companion.White)
                .padding(8.dp),
        ) {

            Text(
                modifier = Modifier.Companion,
                text = text,
                color = Color.Companion.Black
            )

            Spacer(modifier = Modifier.Companion.height(8.dp))

            //couleur + titre
            Box(
                modifier = Modifier.Companion
                    .size(200.dp)
                    .align(Alignment.Companion.CenterHorizontally)
            ) {
                val controller = rememberColorPickerController()

                HsvColorPicker(
                    modifier = Modifier.Companion
                        .fillMaxSize(),
                    controller = controller,
                    onColorChanged = { colorEnvelope: ColorEnvelope ->
                        val hexCode: String = colorEnvelope.hexCode
                        hexColor = hexCode
                    }
                )
            }

            Spacer(modifier = Modifier.Companion.height(8.dp))

            TextField(
                modifier = Modifier.Companion
                    .fillMaxWidth(),
                value = editMessage.value,
                onValueChange = { value: String -> editMessage.value = value },
                singleLine = true,
                label = { Text("Titre du drapeau") }
            )

            Spacer(modifier = Modifier.Companion.height(8.dp))

            Row(
                modifier = Modifier.Companion
            ) {
                Spacer(
                    modifier = Modifier.Companion
                        .weight(1f)
                )

                Button(
                    modifier = Modifier.Companion,
                    onClick = {
                        viewModel.setIsTagInfosDialogVisible(false)
                        viewModel.viewModelScope.launch {
                            onDatasCompleted(null, viewModel, mainActivity)
                        }
                    }
                ) {
                    Text("Annuler")
                }

                Button(
                    modifier = Modifier.Companion,
                    onClick = {
                        if (hexColor != null && editMessage.value != "")
                            viewModel.viewModelScope.launch {
                                onDatasCompleted(
                                    TagInfos(
                                        title = editMessage.value,
                                        Color("#$hexColor".toColorInt()),
                                    ), viewModel, mainActivity
                                )
                            }

                        viewModel.setIsTagInfosDialogVisible(false)
                    }
                ) {
                    Text("Valider")
                }
            }
        }
    }
}

@Composable
fun SigmaActivity.HomeItemDialog(
    message: String,
    homeItemInfos: StateFlow<HomeItemInfos?>,
    onDatasCompleted: (homeItemInfos: HomeItemInfos?) -> Unit,
    viewModel: SigmaViewModel,
) {
    var editText1 by remember { mutableStateOf(homeItemInfos.value?.oldTitle ?: "") }
    var editPath1 by remember { mutableStateOf(homeItemInfos.value?.path ?: "") }
    var editPicture1 by remember { mutableStateOf(homeItemInfos.value?.picture) }
    val homeInfos by homeItemInfos.collectAsState()
    val focusRequester = remember { FocusRequester() }

    Log.d(SigmaActivity.Companion.TAG, "HomeItemDialog: $homeInfos")
    Box(
        modifier = Modifier.Companion
            .width(600.dp)
            .background(
                color = contentColorFor(Color.Companion.White)
                    .copy(alpha = 0.6f)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    viewModel.setIsHomeItemDialogVisible(false)
                }
            ),
        contentAlignment = Alignment.Companion.Center
    ) {
        Column(
            modifier = Modifier.Companion
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                .background(Color.Companion.White)
                .padding(8.dp),
        ) {

            Text(
                modifier = Modifier.Companion,
                text = message,
                color = Color.Companion.Black
            )

            Spacer(modifier = Modifier.Companion.height(8.dp))

            TextField(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                value = if (homeInfos!!.newTitle == null) homeInfos!!.oldTitle!! else homeInfos!!.newTitle!!,
                onValueChange = { value: String ->
                    sigmaActivity.homeViewModel.setDialogHomeItemInfos(
                        sigmaActivity.homeViewModel.dialogHomeItemInfos.value?.copy(
                            newTitle = value
                        )
                    )
                },
                singleLine = true,
                label = { Text("Titre") }
            )

            Spacer(modifier = Modifier.Companion.height(8.dp))

            Row(
                modifier = Modifier.Companion
                    .fillMaxWidth()
            ) {
                TextField(
                    modifier = Modifier.Companion
                        .weight(1f)
                        .padding(end = 5.dp),
                    value = homeInfos!!.path!!,
                    onValueChange = { value: String ->
                        sigmaActivity.homeViewModel.setDialogHomeItemInfos(
                            sigmaActivity.homeViewModel.dialogHomeItemInfos.value?.copy(
                                path = value
                            )
                        )
                    },
                    singleLine = true,
                    label = { Text("Chemin") }
                )

                Button(
                    onClick = {
                        sigmaActivity.onFolderChosen = { path ->
                            if (path != null) {
                                sigmaActivity.homeViewModel.setDialogHomeItemInfos(
                                    sigmaActivity.homeViewModel.dialogHomeItemInfos.value?.copy(
                                        path = path
                                    )
                                )
                            }
                        }

                        mainViewModel.setIsFilePickerVisible(true)
                    }) {
                    Text("Choisir")
                }
            }

            Spacer(modifier = Modifier.Companion.height(8.dp))

            AsyncImage(
                modifier = Modifier.Companion
                    .size(100.dp)
                    .padding(10.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .border(
                        1.dp,
                        Color.Companion.Black,
                        androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    )
                    .pointerInput(true) {
                        detectTapGestures(
                            onTap = {
                                /**
                                 * @see lorry.folder.items.dossiersigma.ui.browser.BrowserOverlay
                                 * le Browser est un composable dans MainActivity
                                 * voir BrowserOverlay et son appel par MainActivity
                                 * le callback est un de ses paramètres d'appel
                                 */
                                sigmaActivity.browser.changeState(
                                    isOpen = true,
                                    item = mainViewModel.selectedItem.value,
                                    target = BrowserTarget.GOOGLE,
                                    onImageClicked = { url ->
                                        mainViewModel.viewModelScope.launch {
                                            val bitmap =
                                                mainViewModel.changingPictureUseCase.urlToBitmap(url)
                                                    ?: return@launch
                                            withContext(Dispatchers.Main) {
                                                mainViewModel.setIsHomeItemDialogVisible(true)
                                                sigmaActivity.homeViewModel.setDialogHomeItemInfos(
                                                    sigmaActivity.homeViewModel.dialogHomeItemInfos.value?.copy(
                                                        picture = bitmap
                                                    )
                                                )
                                            }
                                        }
                                    }
                                )

//                                sigmaActivity.onGotBrowserImage = { url ->
//                                    mainViewModel.viewModelScope.launch {
//                                        val bitmap =
//                                            mainViewModel.changingPictureUseCase.urlToBitmap(url)
//                                                ?: return@launch
//                                        withContext(Dispatchers.Main) {
//                                            mainViewModel.setIsHomeItemDialogVisible(true)
//                                            sigmaActivity.homeViewModel.setDialogHomeItemInfos(
//                                                sigmaActivity.homeViewModel.dialogHomeItemInfos.value?.copy(
//                                                    picture = bitmap
//                                                )
//                                            )
//                                        }
//                                    }
//                                }

                                mainViewModel.setIsHomeItemDialogVisible(false)
//                                mainViewModel.browserManager.openBrowserWithText("")
                            }
                        )
                    },
                model = homeInfos!!.picture,
                contentDescription = "Miniature",
                contentScale = ContentScale.Companion.Fit,
            )

            Spacer(modifier = Modifier.Companion.height(8.dp))

            Row(
                modifier = Modifier.Companion
            ) {
                Spacer(
                    modifier = Modifier.Companion
                        .weight(1f)
                )

                Button(
                    modifier = Modifier.Companion,
                    onClick = {
                        viewModel.setIsHomeItemDialogVisible(false)
                    }
                ) {
                    Text("Annuler")
                }

                Button(
                    modifier = Modifier.Companion
                        .padding(start = 5.dp),
                    onClick = {
                        if (homeInfos!!.newTitle != null && homeInfos!!.path != null) {
                            val newHomeItem = HomeItemInfos(
                                oldTitle = homeItemInfos.value?.oldTitle,
                                newTitle = homeInfos!!.newTitle,
                                path = homeInfos!!.path,
                                picture = homeInfos!!.picture,
                                index = homeItemInfos.value?.index
                                    ?: sigmaActivity.homeViewModel.homeItems.value.size
                            )

                            mainViewModel.viewModelScope.launch {
                                onDatasCompleted(newHomeItem)

                                val existingHomeItems = sigmaActivity.homeViewModel.homeItems.value
                                val newHomeItems = existingHomeItems.toMutableList()
                                    .map {
                                        if (it.title == homeInfos!!.newTitle) homeInfos!! else HomeItemInfos(
                                            oldTitle = it.title,
                                            newTitle = it.title,
                                            path = it.path,
                                            picture = it.picture,
                                            index = homeItemInfos.value?.index
                                                ?: sigmaActivity.homeViewModel.homeItems.value.size
                                        )
                                    }.toSet()

                                sigmaActivity.settingsViewModel.settings.saveHomeItems(
                                    newHomeItems
                                )
                            }

                            viewModel.setIsHomeItemDialogVisible(false)
                        } else
                            Toast.makeText(
                                sigmaActivity,
                                "Veuillez renseigner au moins le titre et le chemin du raccourci",
                                Toast.LENGTH_LONG
                            ).show()
                    }
                ) {
                    Text("Valider")
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        // après composition → demande le focus
        focusRequester.requestFocus()
    }
}

@Composable
fun SigmaActivity.FolderChooserDialog(
    modifier: Modifier,
    viewModel: SigmaViewModel,
    onDatasCompleted: (path: String?) -> Unit,
) {
    var path = remember { mutableStateOf("/storage/emulated/0") }
    var items = remember { mutableStateOf(listOf<Item>()) }

    LaunchedEffect(path.value) {
        mainViewModel.viewModelScope.launch {
            items.value = mainViewModel.diskRepository.getFolderItems(
                path.value,
                SortingCriterion.ByNameAsc
            )
        }
    }

    Column(
        modifier = modifier
            .width(600.dp)
            .height(400.dp)
            .background(Color.Companion.White)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = Color.Companion.Black,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
    ) {
        FileChooserToolbox(
            path = path,
        )

        Spacer(modifier = Modifier.Companion.height(8.dp))

        SelectedPathDisplay(path = path)

        Spacer(modifier = Modifier.Companion.height(8.dp))

        FileList(
            path = path,
            items = items,
        )

        Spacer(modifier = Modifier.Companion.height(8.dp))

        BottomToolbar2(
            modifier = Modifier.Companion,
            path = path,
            items = items,
            onDatasCompleted = onDatasCompleted,
            viewModel = viewModel
        )
    }
}

@Composable
fun ColumnScope.SelectedPathDisplay(
    path: MutableState<String>
) {
    Text(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .align(Alignment.Companion.CenterHorizontally),
        text = path.value.substringAfterLast("/"),
        textAlign = TextAlign.Companion.Center

    )
}

@Composable
fun ColumnScope.BottomToolbar2(
    modifier: Modifier,
    path: MutableState<String>,
    items: MutableState<List<Item>>,
    onDatasCompleted: (path: String?) -> Unit,
    viewModel: SigmaViewModel
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Spacer(
            modifier = Modifier.Companion
                .weight(1f)
        )

        Button(
            onClick = {
                viewModel.setIsFilePickerVisible(false)
            }
        ) {
            Text(text = "Abandonner")
        }

        Button(
            modifier = Modifier.Companion
                .padding(horizontal = 5.dp),
            onClick = {
                onDatasCompleted(path.value)
                viewModel.setIsFilePickerVisible(false)
            }
        ) {
            Text(text = "Choisir ${path.value.substringAfterLast("/").takeLast(20)}")
        }
    }
}

@Composable
fun FileChooserToolbox(
    path: MutableState<String>,
) {

    Row(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(top = 10.dp)
    ) {
        Button(
            modifier = Modifier.Companion
                .padding(horizontal = 5.dp)
                .width(IntrinsicSize.Min),
            onClick = {
                path.value = path.value.substringBeforeLast("/")
            }
        ) {
            Text(text = "Remonter")
        }

        Button(
            modifier = Modifier.Companion
                .padding(horizontal = 5.dp)
                .width(IntrinsicSize.Min),
            onClick = {
                path.value = "/storage/emulated/0/Download"
            }
        ) {
            Text(text = "Téléchargements")
        }

        Button(
            modifier = Modifier.Companion
                .padding(horizontal = 5.dp)
                .width(IntrinsicSize.Min),
            onClick = {
                path.value = "/storage/emulated/0"
            }
        ) {
            Text(text = "Stockage principal")
        }

        Button(
            modifier = Modifier.Companion
                .padding(horizontal = 5.dp)
                .width(IntrinsicSize.Min),
            onClick = {
                path.value = "/storage/emulated/0/Movies"
            }
        ) {
            Text(text = "Movies")
        }
    }
}

@Composable
fun ColumnScope.FileList(
    path: MutableState<String>,
    items: MutableState<List<Item>>
) {

    LazyColumn(
        modifier = Modifier.Companion
            .weight(1f)
            .padding(horizontal = 20.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = Color.Companion.Gray,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
    ) {
        val isEmpty = items.value.isEmpty()

        if (isEmpty)
            item {
                Box(
                    modifier = Modifier.Companion.fillParentMaxSize(),
                    contentAlignment = Alignment.Companion.Center
                ) {
                    Text(text = "Le répertoire est vide")
                }
            }
        else
            items(items.value.size) { index ->
                val item = items.value[index]

                ItemRow(path = path, item = item)
            }
    }
}

@Composable
fun ColumnScope.ItemRow(
    path: MutableState<String>,
    item: Item
) {

    Row(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .clickable {
                if (item.isFolder())
                    path.value = item.fullPath
            }
    ) {
        AsyncImage(
            modifier = Modifier.Companion
                .size(50.dp)
                .padding(end = 10.dp),
            model = if (item.isFile()) R.drawable.file else R.drawable.folder_empty,
            contentDescription = "Miniature",
            contentScale = ContentScale.Companion.Fit,

            )

        Text(
            modifier = Modifier.Companion
                .align(Alignment.Companion.CenterVertically),
            text = item.name
        )
    }
}


data class TagInfos(
    val title: String,
    val color: Color
)

data class HomeItemInfos(
    val oldTitle: String? = null,
    val newTitle: String? = null,
    val path: String?,
    val picture: Bitmap?,
    val index: Int
) {
    suspend fun toHomeItemInfosDTO(): HomeItemInfosDTO {
        val videoEmbedder = VideoInfoEmbedder()
        return HomeItemInfosDTO(
            oldTitle = oldTitle,
            newTitle = newTitle,
            path = path,
            picture = if (picture != null) videoEmbedder.bitmapToBase64(picture)
            else null,
            index = index
        )
    }
}

data class HomeItemInfosDTO(
    val oldTitle: String? = null,
    val newTitle: String? = null,
    val path: String?,
    val picture: String?,
    val index: Int
) {
    suspend fun toHomeItemInfos(): HomeItemInfos {
        val videoEmbedder = VideoInfoEmbedder()
        return HomeItemInfos(
            oldTitle = oldTitle,
            newTitle = newTitle,
            path = path,
            picture = if (picture != null) videoEmbedder.base64ToBitmap(picture)
            else null,
            index = index
        )
    }
}

@Composable
context(BottomTools, RowScope)
fun FixedSticker(
    modifier: Modifier = Modifier,
    tool: Tool,
    activity: SigmaActivity,
) {
    Box(
        modifier = modifier
            .width(85.dp)
            .fillMaxHeight()
            .clickable {
                setCurrentTool(tool)
                viewModel.viewModelScope.launch {
                    tool.onClick(tool, viewModel, activity)
                }
            }
    ) {
        var globalOffset: Offset = Offset.Zero
        //icône statique, toujours existante
        StickerIcon(
            modifier = Modifier
                .padding(top = 0.dp)
                .align(Alignment.TopCenter)
                .onGloballyPositioned { layoutCoordinates ->
                    val localOffset = layoutCoordinates.positionInRoot()
                    globalOffset = layoutCoordinates.localToRoot(Offset.Zero)
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            viewModel.beginDrag(tool, globalOffset)
                        },
                        onDrag = { change: PointerInputChange, dragAmount: Offset ->
                            viewModel.addDragOffset(dragAmount)
                        },
                        onDragEnd = {
                            val target = viewModel.dragTargetItem.value

                            if (target != null) {
                                viewModel.assignColoredTagToItem(
                                    target,
                                    tool.toColoredTag()
                                )
                            }

                            viewModel.terminateDrag()
                        },
                        onDragCancel = {},
                    )
                },
            iconRes = tool.icon,
            iconTint = if (tool.isColoredIcon) Color.Companion.Unspecified else
                (tool.tint ?: Color(0xFFe9c46a)),
            ringColor = if (tool.isColoredIcon) Color.Companion.Unspecified else
                (tool.tint ?: Color(0xFFe9c46a)),
            ringWidth = 2.dp,
            iconSize = 28.dp,
            ringSize = 33.dp,
            isRingEnabled = tool.activated
        )

        StickerText(
            tool = tool
        )
    }
}

@Composable
context(BottomTools, BoxScope)
fun MobileSticker(
    dragState: DragState,
    activity: SigmaActivity,
) {
    val tool: Tool = dragState.tool
    val offset: Offset = dragState.offset

    Box(
        modifier = Modifier
            .width(85.dp)
            .fillMaxHeight()
            .clickable {
                setCurrentTool(tool)
                viewModel.viewModelScope.launch {
                    tool.onClick(tool, viewModel, activity)
                }
            }
    ) {
        StickerIcon(
            modifier = Modifier
                .offset {
                    IntOffset(
                        offset.x.roundToInt() - 60,
                        offset.y.roundToInt() - 70
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                        },
                        onDragEnd = {}
                    )

                },
            iconRes = tool.icon,
            iconTint = if (tool.isColoredIcon) Color.Companion.Unspecified else
                (tool.tint ?: Color(0xFFe9c46a)),
            ringColor = if (tool.isColoredIcon) Color.Companion.Unspecified else
                (tool.tint ?: Color(0xFFe9c46a)),
            ringWidth = 2.dp,
            ringSize = 85.dp,
            iconSize = 70.dp,
            isRingEnabled = true,
        )

        StickerText(
            tool = tool
        )
    }
}

@Composable
fun StickerIcon(
    modifier: Modifier = Modifier,
    iconRes: Int,
    ringColor: Color,
    ringWidth: Dp,
    iconTint: Color, // Permet de garder la couleur originale de l'icône
    ringSize: Dp,
    iconSize: Dp,
    isRingEnabled: Boolean,
) {

// Le Box sert de conteneur pour dessiner la bordure autour.
    Box(
        modifier = if (isRingEnabled) modifier
// Étape 1 : Appliquer une bordure.
            .border(
                width = ringWidth,
                color = ringColor,
                shape = CircleShape // Essentiel pour que la bordure soit un anneau.
            )
// Étape 2 : Ajouter un padding INTERNE égal à l'épaisseur de l'anneau.
// Cela "pousse" le contenu (l'icône) vers l'intérieur pour ne pas qu'il soit sous la bordure.
            .padding(ringWidth)
// Étape 3 (Optionnel mais recommandé) : Donner une taille fixe au conteneur.
            .size(ringSize)
        else modifier
            .border(
                width = ringWidth,
                color = Color.Companion.Transparent,
                shape = CircleShape // Essentiel pour que la bordure soit un anneau.
            )
            .padding(ringWidth)
            .size(ringSize),
        contentAlignment = Alignment.Companion.Center // S'assure que l'icône est bien centrée.
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = "Icône avec un anneau",
            // L'icône prend toute la place disponible à l'intérieur du padding.
            modifier = Modifier.Companion.size(iconSize),
            tint = iconTint
        )
    }
}

@Composable
context(BoxScope)
fun StickerText(
    tool: Tool
) {
    Text(
        modifier = Modifier
            .align(Alignment.BottomCenter),
        text = tool.text(),
        color = SigmaColors.current.onPrimary,
        fontSize = 12.sp
    )
}

data class OverallProgress(
    val progress: Int,
    val fileIndex: Int,
    val fileSize: Int
)