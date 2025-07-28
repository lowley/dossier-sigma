package lorry.folder.items.dossiersigma.ui.bottomArea

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewModelScope
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
import lorry.folder.items.dossiersigma.data.base64.VideoInfoEmbedder
import lorry.folder.items.dossiersigma.data.dataSaver.CompositeManager
import lorry.folder.items.dossiersigma.data.dataSaver.CroppedPicture
import lorry.folder.items.dossiersigma.data.dataSaver.Flag
import lorry.folder.items.dossiersigma.data.dataSaver.InitialPicture
import lorry.folder.items.dossiersigma.data.dataSaver.Scale
import lorry.folder.items.dossiersigma.domain.ColoredTag
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.domain.services.MoveFileService
import lorry.folder.items.dossiersigma.domain.services.MoveToNASService
import lorry.folder.items.dossiersigma.domain.usecases.browser.BrowserTarget
import lorry.folder.items.dossiersigma.ui.components.imageAsAnyToTempUri
import lorry.folder.items.dossiersigma.ui.components.manageImageClick
import lorry.folder.items.dossiersigma.ui.sigma.SortingCriterion
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity
import lorry.folder.items.dossiersigma.ui.sigma.SigmaViewModel
import lorry.folder.items.dossiersigma.ui.sigma.containsFlagAsValue
import java.io.File
import java.util.UUID
import kotlin.collections.get

object BottomTools {
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

        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(60.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            toolList.forEach { tool ->
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
                    Tag(
                        modifier = Modifier
                            .align(Alignment.Companion.TopCenter)
                            .padding(top = 0.dp)
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
                        iconSize = 25.dp,
                        ringSize = 38.dp,
                        isRingEnabled = tool.activated
                    )

                    Text(
                        modifier = modifier
                            .align(Alignment.Companion.BottomCenter),
                        text = tool.text(),
                        color = Color(0xFFe9c46a),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    fun observeDefaultContent(viewModel: SigmaViewModel) {
        this.viewModel = viewModel
        viewModel.viewModelScope.launch {
            // On combine les deux sources de données : le cache des tags et l'ID du tag sélectionné.
            // La lambda sera appelée si l'un ou l'autre change.
            combine(
                viewModel.flagCache,
                currentFlagId,
                viewModel.currentFolderPath,
                viewModel.reloadTrigger
            ) { tagsMap, selectedId, _, _ ->
//                val currentContentNow = currentContent.value
//                if (currentContentNow?.name != "DEFAULT_CONTENT")
//                    return@combine

                // 2. On transforme les tags du cache en outils dynamiques
                val uniqueTags = tagsMap.values.distinctBy { it.id }

                val tagTools = uniqueTags.map { tag ->
                    Tool(
                        text = { tag.title },
                        icon = R.drawable.etiquette,
                        tint = tag.color,
                        id = tag.id ?: UUID.randomUUID(),
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
    val id: UUID = UUID.randomUUID(),
    val activated: Boolean = false
) {
    fun isActivated() = activated


}

fun Tool.toColoredTag(viewModel: SigmaViewModel? = null): ColoredTag = ColoredTag(
    id = this.id,
    title = this.text(),
    color = this.tint ?: Color.Companion.Unspecified,
)


sealed class Tools() {
    abstract fun content(viewModel: SigmaViewModel? = null): BottomToolContent

    object DEFAULT : Tools() {
        override fun content(viewModel: SigmaViewModel?) = BottomTools.defaultContent
    }

    object TAGS_MENU : Tools() {
        override fun content(viewModel: SigmaViewModel?) = BottomToolContent(
            listOf(
                /////////////
                // ajouter //
                /////////////
                Tool(
                    text = { "Ajouter" },
                    icon = R.drawable.plus,
                    visible = { viewModel, mainActivity ->
                        viewModel.flagCache.value[viewModel.selectedItemFullPath.value] != null
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

                                    val newTool = Tool(
                                        text = { tagInfos?.title ?: "" },
                                        icon = R.drawable.etiquette,
                                        isColoredIcon = false,
                                        onClick = { viewModel, mainActivity ->
                                        },
                                        visible = { viewModel, mainActivity ->
                                            true
                                        },
                                        tint = tagInfos?.color ?: Color.Companion.Unspecified
                                    )

                                    //attention
                                    //le cache est lu par observeDefaultContent qui l'ajoutera
//                                    DEFAULT.content(viewModel).addTool(newTool, 0)

                                    if (tagInfos == null)
                                        return@run

                                    val compositeMgr = CompositeManager(currentItem.fullPath)

                                    val newFlag = ColoredTag(
                                        title = tagInfos.title,
                                        color = tagInfos.color,
                                        id = newTool.id,
                                    )
                                    compositeMgr.save(Flag(newFlag))

                                    viewModel.setFlagCacheValue(
                                        currentItem.fullPath,
                                        newFlag
                                    )

//                                    if (currentItem != null && tagInfos != null) {
//                                        currentItem.tag = newFlag
//                                    }

//                                    viewModel.refreshCurrentFolder()
                                }

                                BottomTools.setCurrentContent(DEFAULT)
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
                        viewModel.flagCache.value[viewModel.selectedItemFullPath.value] != null
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
                        viewModel.flagCache.value[viewModel.selectedItemFullPath.value] != null
                    },
                    onClick = { viewModel, mainActivity ->
                        run {
                            val currentItem = viewModel.selectedItem.value
                            if (currentItem == null)
                                return@run

                            val currentTag =
                                viewModel.flagCache.value[viewModel.selectedItemFullPath.value]
                            val tool = DEFAULT.content(viewModel)
                                .tools.value.firstOrNull { it.id == currentTag?.id }

                            if (tool == null) {
                                println("problème, tool inexistant")
                                return@run
                            }

                            if (viewModel.removeFlagCacheForKey(currentItem.fullPath) == null) {
                                println("problème, suppression de tag impossible")
                                return@run
                            }

                            val compositeMgr = CompositeManager(currentItem.fullPath)
                            compositeMgr.save(Flag(null))

                            if (!viewModel.flagCache.containsFlagAsValue(tool.id))
                                DEFAULT.content(viewModel).removeTool(tool)

                            viewModel.setSelectedItem(null, true)
//                            viewModel.refreshCurrentFolder()
                            BottomTools.setCurrentContent(DEFAULT)

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
                        viewModel.flagCache.value[viewModel.selectedItemFullPath.value] != null
                    },
                    onClick = { viewModel, mainActivity ->
                        run {
                            val currentItem = viewModel.selectedItem.value ?: return@run
                            val currentTag =
                                viewModel.flagCache.value[viewModel.selectedItemFullPath.value]

                            val tool = DEFAULT.content(viewModel)
                                .tools.value.firstOrNull { it.id == currentTag?.id }

                            if (tool == null) {
                                println("problème, tool inexistant")
                                return@run
                            }

                            //on fait ça parce que par lazy loading au début de l'affichage
                            //du dossier de tous les items
                            val itemsWithThisTag = viewModel.currentFolder.value.items.filter {
                                val compositeMgr = CompositeManager(it.fullPath)
                                val tagFile = compositeMgr.getElement(Flag.Companion)
                                val tagCache = viewModel.flagCache.value[it.fullPath]

                                val tagFinal = tagCache ?: tagFile
                                tagFinal?.id == tool.id
                            }

                            itemsWithThisTag.forEach {
                                if (viewModel.removeFlagCacheForKey(it.fullPath) == null) {
                                    println("problème, suppression de tag impossible")
                                    return@run
                                }

                                val compositeMgr = CompositeManager(it.fullPath)
                                compositeMgr.save(Flag(null))

                            }

                            //normalement toujours vrai
                            if (!viewModel.flagCache.containsFlagAsValue(tool.id))
                                DEFAULT.content(viewModel).removeTool(tool)

                            viewModel.setSelectedItem(null, true)
//                            viewModel.refreshCurrentFolder()
                            BottomTools.setCurrentContent(DEFAULT)
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
                            viewModel.flagCache.value[viewModel.selectedItemFullPath.value] != null
                        },
                    onClick =
                        { viewModel, mainActivity ->
                            run {
                                val files = viewModel.currentFolder.value.items

                                files.forEach {
                                    val compositeMgr = CompositeManager(it.fullPath)
                                    compositeMgr.save(Flag(null))
                                }

                                viewModel.clearFlagCache()
                                DEFAULT.content().updateTools(emptyList<Tool>())

                                viewModel.setSelectedItem(null, true)
//                                viewModel.refreshCurrentFolder()
                                BottomTools.setCurrentContent(DEFAULT)
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
                        BottomTools.setCurrentContent(MOVES)
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
                        BottomTools.setCurrentContent(TAGS_MENU)
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

                            /**
                             * @see lorry.folder.items.dossiersigma.ui.components.BrowserOverlay
                             * le Browser est un composable dans MainActivity
                             * voir BrowserOverlay et son appel par MainActivity
                             * le callback est un de ses paramètres d'appel
                             */
                            mainActivity.onGotBrowserImage = { url ->
                                viewModel.viewModelScope.launch {
                                    manageImageClick(viewModel, url)
                                    //génère des problèmes dans manageImageClick
//                            mainViewModel.setSelectedItem(null)
                                    BottomTools.setCurrentContent(DEFAULT)
                                    viewModel.setSelectedItem(null, true)
//                                        mainViewModel.refreshCurrentFolder()
                                }
                            }

                            viewModel.browserManager.openBrowser(
                                selectedItem, BrowserTarget.GOOGLE
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
                        BottomTools.setCurrentContent(CROP)
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
                                        viewModel.refreshCurrentFolder()
                                    } else
                                        Toast.makeText(
                                            mainActivity,
                                            "Un problème lors du renommage est survenu",
                                            Toast.LENGTH_LONG
                                        )
                                            .show()
                                }
                            }

                            BottomTools.setCurrentContent(DEFAULT)
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
                        val parent = viewModel.currentFolder.value
                        //viewModel.setSelectedItem(null)
                        viewModel.setDialogMessage("Nouveau nom du dossier")
                        viewModel.dialogOnOkLambda = { newName, viewModel, mainActivity ->
                            run {
                                val parentPath = parent.fullPath
                                val children = parent.items
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
                                    viewModel.refreshCurrentFolder()
                                } else
                                    Toast.makeText(
                                        mainActivity,
                                        "Un problème lors de la création  du dossier frère est survenu",
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                            }

                            BottomTools.setCurrentContent(DEFAULT)
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
                        val parent = viewModel.currentFolder.value
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
                                    viewModel.refreshCurrentFolder()
                                } else
                                    Toast.makeText(
                                        mainActivity,
                                        "Un problème lors de la création  du dossier enfant est survenu",
                                        Toast.LENGTH_LONG
                                    ).show()
                            }

                            BottomTools.setCurrentContent(DEFAULT)
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

                            viewModel.refreshCurrentFolder()
                            BottomTools.setCurrentContent(DEFAULT)
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
                        BottomTools.movingItem = viewModel.selectedItem.value
                        BottomTools.setCurrentContent(MOVE_FILE)
                        viewModel.setSelectedItem(null, keepBottomToolsAsIs = true)
                    }
                ),
                /////////////////////
                // déplacement NAS //
                /////////////////////
                Tool(
                    text = {
                        val nasText = BottomTools.copyNASText.value
                        nasText
                    },
                    icon = R.drawable.deplacer,
                    onClick = { viewModel, mainActivity ->
                        run {
                            BottomTools.itemToMove = viewModel.selectedItem.value

                            if (BottomTools.itemToMove == null)
                                return@run

                            //toast
                            println("MovingItem: choisir fichier destination")

                            /**
                             * le fichier n'existe pas, on lance la copie,
                             * le reste est effectué dans
                             * @see MoveFileService.onStartCommand
                             */

                            //encode/decode en json
                            val intent = Intent(mainActivity, MoveToNASService::class.java).apply {
                                putExtra(
                                    "filesToTransfer", Gson().toJson(
                                        listOf(
                                            BottomTools.itemToMove?.fullPath ?: ""
                                        )
                                    )
                                )
                                putExtra(
                                    "nasDirectory",
                                    mainActivity.settingsViewModel.settingsManager.nasFolderFlow.firstOrNull()
                                )
                            }
                            mainActivity.startService(intent)
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
                        BottomTools.setCurrentContent(DEFAULT)
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
                        BottomTools.setCurrentContent(DEFAULT)
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
                        BottomTools.setCurrentContent(DEFAULT)
                        val item = BottomTools.movingItem
                        val movingParent = item?.fullPath?.substringBeforeLast("/")

                        if (movingParent != null)
                            viewModel.goToFolder(movingParent)
                        BottomTools.movingItem = null
                        viewModel.setSelectedItem(null, true)
//                        viewModel.refreshCurrentFolder()
                    }
                ),
                ////////////
                // coller //
                ////////////
                Tool(
                    text = {
                        val movePasteText = BottomTools.movePasteText.value
                        movePasteText
                    },
                    icon = R.drawable.coller,
                    onClick = { viewModel, mainActivity ->
                        run {
                            BottomTools.itemToMove = viewModel.selectedItem.value
                            var dest = BottomTools.itemToMove

                            if (dest == null) {
                                BottomTools.itemToMove = viewModel.currentFolder.value
                                dest = BottomTools.itemToMove
                            }

                            //toast
                            println("MovingItem: choisir fichier destination")
                            //1.copie
                            val sourceFile = File(BottomTools.movingItem?.fullPath ?: "")
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
                                if (BottomTools.movingItem == null)
                                    return@run
                                val isItemExists = viewModel.diskRepository.isFileOrFolderExists(
                                    dest.fullPath,
                                    BottomTools.movingItem!!
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
                                putExtra("source", BottomTools.movingItem?.fullPath ?: "")
                                putExtra("destination", dest.fullPath)
                                putExtra("addSuffix", "")
                            }
                            mainActivity.startService(intent)
//                            viewModel.setSelectedItem(null, true)
                            viewModel.refreshCurrentFolder()
                            //2.vérif copie bien réalisée:
                            //dest existe
                            //tailles égales

                            //3.si ok: suppression source


                            //vm.diskRepository.copyFile(sourceFile, destinationFile)
//                        BottomTools.setCurrentContent(DEFAULT, viewModel)
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

                            val compositeMgr = CompositeManager(item.fullPath)
                            sourceBitmap = compositeMgr.getElement(InitialPicture.Companion)
                            val test = compositeMgr.getElement(CroppedPicture.Companion)

                            if (sourceBitmap == null && test != null) {
                                compositeMgr.save(InitialPicture(test, VideoInfoEmbedder()))
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
    viewModel.setScaleCacheValue(item.fullPath, scale)
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
            val compositeMgr = CompositeManager(item.fullPath)
            compositeMgr.save(Scale(scale))
        }
    }

    if (item.isFolder()) {
        viewModel.viewModelScope.launch {
            val file = File(item.fullPath + "/.folderPicture.html")
            if (!file.exists())
                viewModel.diskRepository.createFolderHtmlFile(item)

            val compositeMgr = CompositeManager(item.fullPath)
            compositeMgr.save(Scale(scale))
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
                    }
                ) {
                    Text("OK")
                }
            }
        }
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
                    .fillMaxWidth(),
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
                                 * @see lorry.folder.items.dossiersigma.ui.components.BrowserOverlay
                                 * le Browser est un composable dans MainActivity
                                 * voir BrowserOverlay et son appel par MainActivity
                                 * le callback est un de ses paramètres d'appel
                                 */
                                sigmaActivity.onGotBrowserImage = { url ->
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

                                mainViewModel.setIsHomeItemDialogVisible(false)
                                mainViewModel.browserManager.openBrowserWithText("")
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

                                sigmaActivity.settingsViewModel.settingsManager.saveHomeItems(
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
        FileChooserToolbox(path = path)

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
fun FileChooserToolbox(path: MutableState<String>) {

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
fun ColumnScope.FileList(path: MutableState<String>, items: MutableState<List<Item>>) {

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
fun ColumnScope.ItemRow(path: MutableState<String>, item: Item) {

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
fun Tag(
    modifier: Modifier = Modifier.Companion,
    iconRes: Int,
    ringColor: Color,
    ringWidth: Dp = 2.dp,
    iconTint: Color = Color.Companion.Unspecified, // Permet de garder la couleur originale de l'icône
    ringSize: Dp = 33.dp,
    iconSize: Dp = 28.dp,
    isRingEnabled: Boolean
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

data class OverallProgress(
    val progress: Int,
    val fileIndex: Int,
    val fileSize: Int
)