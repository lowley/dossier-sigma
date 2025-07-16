package lorry.folder.items.dossiersigma.ui.components

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.google.gson.Gson
import com.jaiselrahman.filepicker.activity.FilePickerActivity
import com.jaiselrahman.filepicker.model.MediaFile
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
import lorry.folder.items.dossiersigma.ui.sigma.ITEMS_ORDERING_STRATEGY
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity.Companion.TAG
import lorry.folder.items.dossiersigma.ui.sigma.SigmaViewModel
import lorry.folder.items.dossiersigma.ui.sigma.containsFlagAsValue
import java.io.File
import java.util.UUID


object BottomTools {
    lateinit var viewModel: SigmaViewModel

    private val _bottomToolsContent = MutableStateFlow<BottomToolContent?>(null)
    val currentContent: StateFlow<BottomToolContent?> = _bottomToolsContent
    internal val defaultContent = BottomToolContent(emptyList(), "DEFAULT_CONTENT")

    fun setCurrentContent(tools: Tools) {
        _bottomToolsContent.value = tools.content(viewModel)
    }

    //destiné à l'affichage par remontée dans MainActivity
    private val _currentTool = MutableStateFlow<Tool?>(null)
    val currentTool: StateFlow<Tool?> = _currentTool

    fun setCurrentTool(tool: Tool?) {
        _currentTool.value = tool
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

    private val _NASprogress = MutableStateFlow(0)
    val nasProgress: StateFlow<Int> = _NASprogress

    /**
     * utilisé par
     * @see MoveToNASService.copy
     */
    fun updateNASProgress(value: Int) {
        _NASprogress.value = value
    }

    private val _copyNASText = MutableStateFlow("1 -> NAS")
    val copyNASText: StateFlow<String> = _copyNASText

    fun updateMoveNASText(value: String) {
        _copyNASText.value = value
    }

    @Composable
    fun BottomToolBar(
        activity: SigmaActivity
    ) {

        val content by currentContent.collectAsState()
        val toolList by content?.tools?.collectAsState() ?: remember { mutableStateOf(emptyList()) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            toolList.forEach { tool ->
                val offset by viewModel.dragOffset.collectAsState()
                var iconSize = if (offset == Offset.Zero || offset == null) 28.dp else 140.dp
                var iconYDelta = if (offset == Offset.Zero) 0f else 200f

                Box(
                    modifier = Modifier
                        .width(85.dp)
                        .fillMaxHeight()
                        .clickable {
                            setCurrentTool(tool)
                            viewModel.viewModelScope.launch {
                                tool.onClick(viewModel, activity)
                            }
                        }
                ) {
                    Icon(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp)
                            .size(28.dp)
                            .onGloballyPositioned {
                                viewModel.setDraggableStartPosition(it.positionInRoot())
                            },
                        painter = painterResource(id = tool.icon),
                        contentDescription = null,
                        tint = if (tool.isColoredIcon) Color.Unspecified else
                            (tool.tint ?: Color(0xFFe9c46a))
                    )

                    if (content?.name == "DEFAULT_CONTENT") {
                        val coloredTag = tool.toColoredTag(viewModel)

                        Icon(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 10.dp)
                                .offset {
                                    IntOffset(
                                        offset?.x?.toInt() ?: 0, offset?.y?.toInt() ?: 0
                                    )
                                }
                                .size(iconSize)
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = {
//                                            movingItem = viewModel.selectedItem.value
                                            viewModel.setDraggedTag(coloredTag)
//                                            println("DRAG start, ${coloredTag.title}")
                                            val adjustment = Offset(x = 0f, y = 0 - iconYDelta)
                                            viewModel.addToDragOffset(adjustment)
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            viewModel.addToDragOffset(dragAmount)

//                                            println("DRAG offset: ${currentGlobalOffset.x}, ${currentGlobalOffset.y}")
                                        },
                                        onDragEnd = {
//                                            movingItem = null
                                            val target = viewModel.dragTargetItem.value
//                                            println("DRAG end, ${target?.name ?:"null"}")

                                            if (target != null) {
                                                viewModel.assignColoredTagToItem(target, coloredTag)

                                            }

                                            viewModel.setDragOffset(Offset.Zero)
                                            viewModel.setDraggedTag(null)
                                            viewModel.setDragTargetItem(null)
                                        },
                                    )
                                },
                            painter = painterResource(id = tool.icon),
                            contentDescription = null,
                            tint = if (tool.isColoredIcon) Color.Unspecified else
                                (tool.tint ?: Color(0xFFe9c46a))
                        )
                    }

                    Text(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .height(24.dp),
                        text = tool.text(viewModel),
                        color = Color(0xFFe9c46a),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    fun observeDefaultContent(viewModel: SigmaViewModel) {
        viewModel.viewModelScope.launch {
            viewModel.flagCache.collect { tagsMap ->
                if (tagsMap.isEmpty()) {
                    defaultContent.updateTools(emptyList())
                    return@collect
                }
                println(" BottomTools: collect de tagsMap, ${tagsMap.size}")
                val tagsSet = tagsMap.values.toSet()
                val newTools = tagsSet.map { tag ->
                    Tool(
                        text = { tag.title },
                        icon = R.drawable.etiquette,
                        tint = tag.color,
                        id = tag.id ?: UUID.randomUUID(),
                        onClick = { vm, activity ->
                            // Action au clic
                        }
                    )
                }
                defaultContent.updateTools(newTools)
            }
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
}

// Outil unique avec icône, texte, et un comportement.
data class Tool(
    val text: @Composable (vm: SigmaViewModel) -> String,
    @DrawableRes val icon: Int,
    val isColoredIcon: Boolean = false,
    val onClick: suspend (SigmaViewModel, SigmaActivity) -> Unit,
    val visible: suspend (SigmaViewModel, SigmaActivity) -> Boolean = { _, _ -> true },
    val tint: Color? = null,
    val id: UUID = UUID.randomUUID()
)

@Composable
fun Tool.toColoredTag(viewModel: SigmaViewModel): ColoredTag = ColoredTag(
    id = this.id,
    title = this.text(viewModel),
    color = this.tint ?: Color.Unspecified,
)


sealed class Tools() {
    abstract fun content(viewModel: SigmaViewModel? = null): BottomToolContent

    object DEFAULT : Tools() {
        override fun content(viewModel: SigmaViewModel?) =
            BottomTools.defaultContent
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
                                            //filtre des items


                                        },
                                        visible = { viewModel, mainActivity ->
                                            true
                                        },
                                        tint = tagInfos?.color ?: Color.Unspecified
                                    )

                                    DEFAULT.content(viewModel).addTool(newTool, 0)

                                    if (tagInfos == null)
                                        return@run

                                    val compositeMgr = CompositeManager(currentItem.fullPath)

                                    val newFlag = ColoredTag(
                                        title = tagInfos.title,
                                        color = tagInfos.color,
                                        id = newTool.id
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
                                val tagFile = compositeMgr.getElement(Flag)
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
                             * @see BrowserOverlay
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
                                        ITEMS_ORDERING_STRATEGY.DATE_DESC
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
                        val nasText by BottomTools.copyNASText.collectAsState()
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
                            //1.copie
//                            val sourceFile = File(movingItem?.fullPath ?: "")

//                            if (dest?.isFolder() == true) {
//                                if (movingItem == null)
//                                    return@run
//                                val isItemExists = viewModel.diskRepository.isFileOrFolderExists(
//                                    dest.fullPath,
//                                    movingItem!!
//                                )
//                                if (isItemExists) {
//                                    mainActivity.openMoveFileDialog.value = true
//                                    return@run
//                                }
//                            }

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
                    text = { vm ->
                        val movePasteText by BottomTools.movePasteText.collectAsState()
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
                        changeCrop(viewModel, ContentScale.None)
                    }
                ),

                Tool(
                    text = { "Rogner" },
                    icon = R.drawable.crop,
                    onClick = { viewModel, mainActivity ->
                        changeCrop(viewModel, ContentScale.Crop)
                    }
                ),

                Tool(
                    text = { "Remplir ⇅" },
                    icon = R.drawable.crop,
                    onClick = { viewModel, mainActivity ->
                        changeCrop(viewModel, ContentScale.FillHeight)
                    }
                ),

                Tool(
                    text = { "Remplir ⇿" },
                    icon = R.drawable.crop,
                    onClick = { viewModel, mainActivity ->
                        changeCrop(viewModel, ContentScale.FillWidth)
                    }
                ),

                Tool(
                    text = { "Etirer" },
                    icon = R.drawable.crop,
                    onClick = { viewModel, mainActivity ->
                        changeCrop(viewModel, ContentScale.Fit)
                    }
                ),

                Tool(
                    text = { "Dedans" },
                    icon = R.drawable.crop,
                    onClick = { viewModel, mainActivity ->
                        changeCrop(viewModel, ContentScale.Inside)
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
                            sourceBitmap = compositeMgr.getElement(InitialPicture)
                            val test = compositeMgr.getElement(CroppedPicture)

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
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = contentColorFor(Color.White)
                    .copy(alpha = 0.6f)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    viewModel.setIsTextDialogVisible(false)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White)
                .padding(8.dp),
        ) {

            Text(
                modifier = Modifier,
                text = text,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = editMessage.value,
                onValueChange = { editMessage.value = it },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.align(Alignment.End)
            ) {
                Button(
                    onClick = {
                        viewModel.setIsTextDialogVisible(false)
                    }
                ) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(8.dp))

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
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = contentColorFor(Color.White)
                    .copy(alpha = 0.6f)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    viewModel.setIsYesNoDialogVisible(false)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White)
                .padding(8.dp),
        ) {

            Text(
                modifier = Modifier,
                text = text,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.align(Alignment.End)
            ) {
                Button(
                    onClick = {
                        viewModel.setIsYesNoDialogVisible(false)
                        onOk(false)
                    }
                ) {
                    Text("Non")
                }

                Spacer(modifier = Modifier.width(8.dp))

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
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = contentColorFor(Color.White)
                    .copy(alpha = 0.6f)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    viewModel.setIsMoveFileDialogVisible(false)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White)
                .padding(8.dp),
        ) {

            Text(
                modifier = Modifier,
                text = text,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.align(Alignment.End)
            ) {
                Button(
                    onClick = {
                        viewModel.setIsMoveFileDialogVisible(false)
                        onCancel()
                    }
                ) {
                    Text("Abandonner")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        viewModel.setIsMoveFileDialogVisible(false)
                        onCreateCopy()
                    }
                ) {
                    Text("Créer une copie")
                }

                Spacer(modifier = Modifier.width(8.dp))

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
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = contentColorFor(Color.White)
                    .copy(alpha = 0.6f)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    viewModel.setIsTagInfosDialogVisible(false)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White)
                .padding(8.dp),
        ) {

            Text(
                modifier = Modifier,
                text = text,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            //couleur + titre
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                val controller = rememberColorPickerController()

                HsvColorPicker(
                    modifier = Modifier
                        .fillMaxSize(),
                    controller = controller,
                    onColorChanged = { colorEnvelope: ColorEnvelope ->
                        val hexCode: String = colorEnvelope.hexCode
                        hexColor = hexCode
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = editMessage.value,
                onValueChange = { value: String -> editMessage.value = value },
                singleLine = true,
                label = { Text("Titre du drapeau") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
            ) {
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                )

                Button(
                    modifier = Modifier,
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
                    modifier = Modifier,
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

    Log.d(TAG, "HomeItemDialog: $homeInfos")
    Box(
        modifier = Modifier
            .width(600.dp)
            .background(
                color = contentColorFor(Color.White)
                    .copy(alpha = 0.6f)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    viewModel.setIsHomeItemDialogVisible(false)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White)
                .padding(8.dp),
        ) {

            Text(
                modifier = Modifier,
                text = message,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = if (homeInfos!!.newTitle == null) homeInfos!!.oldTitle!! else homeInfos!!.newTitle!!,
                onValueChange = { value: String ->
                    mainActivity.homeViewModel.setDialogHomeItemInfos(
                        mainActivity.homeViewModel.dialogHomeItemInfos.value?.copy(
                            newTitle = value
                        )
                    )
                },
                singleLine = true,
                label = { Text("Titre") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                TextField(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 5.dp),
                    value = homeInfos!!.path!!,
                    onValueChange = { value: String ->
                        mainActivity.homeViewModel.setDialogHomeItemInfos(
                            mainActivity.homeViewModel.dialogHomeItemInfos.value?.copy(
                                path = value
                            )
                        )
                    },
                    singleLine = true,
                    label = { Text("Chemin") }
                )

                Button(
                    onClick = {
                        mainActivity.onFolderChoosed = { path ->
                            if (path != null) {
                                mainActivity.homeViewModel.setDialogHomeItemInfos(
                                    mainActivity.homeViewModel.dialogHomeItemInfos.value?.copy(
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

            Spacer(modifier = Modifier.height(8.dp))

            AsyncImage(
                modifier = Modifier
                    .size(100.dp)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                    .pointerInput(true) {
                        detectTapGestures(
                            onTap = {
                                /**
                                 * @see BrowserOverlay
                                 * le Browser est un composable dans MainActivity
                                 * voir BrowserOverlay et son appel par MainActivity
                                 * le callback est un de ses paramètres d'appel
                                 */
                                mainActivity.onGotBrowserImage = { url ->
                                    mainViewModel.viewModelScope.launch {
                                        val bitmap =
                                            mainViewModel.changingPictureUseCase.urlToBitmap(url)
                                                ?: return@launch
                                        withContext(Dispatchers.Main) {
                                            mainViewModel.setIsHomeItemDialogVisible(true)
                                            mainActivity.homeViewModel.setDialogHomeItemInfos(
                                                mainActivity.homeViewModel.dialogHomeItemInfos.value?.copy(
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
                contentScale = ContentScale.Fit,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
            ) {
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                )

                Button(
                    modifier = Modifier,
                    onClick = {
                        viewModel.setIsHomeItemDialogVisible(false)
                    }
                ) {
                    Text("Annuler")
                }

                Button(
                    modifier = Modifier
                        .padding(start = 5.dp),
                    onClick = {
                        if (homeInfos!!.newTitle != null && homeInfos!!.path != null) {
                            val newHomeItem = HomeItemInfos(
                                oldTitle = homeItemInfos.value?.oldTitle,
                                newTitle = homeInfos!!.newTitle,
                                path = homeInfos!!.path,
                                picture = homeInfos!!.picture,
                            )

                            mainViewModel.viewModelScope.launch {
                                onDatasCompleted(newHomeItem)

                                val existingHomeItems = mainActivity.homeViewModel.homeItems.value
                                val newHomeItems = existingHomeItems.toMutableList()
                                    .map { if (it.title == homeInfos!!.newTitle) homeInfos!! else HomeItemInfos(
                                        oldTitle = it.title,
                                        newTitle = it.title,
                                        path = it.path,
                                        picture = it.picture,
                                    ) }.toSet()

                                mainActivity.settingsViewModel.settingsManager.saveHomeItems(newHomeItems)
                            }

                            viewModel.setIsHomeItemDialogVisible(false)
                        } else
                            Toast.makeText(
                                mainActivity,
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
                ITEMS_ORDERING_STRATEGY.NAME_ASC
            )
        }
    }

    Column(
        modifier = modifier
            .width(600.dp)
            .height(400.dp)
            .background(Color.White)
            .clip(RoundedCornerShape(8.dp))
            .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(8.dp))
    ) {
        FileChooserToolbox(path = path)

        Spacer(modifier = Modifier.height(8.dp))

        SelectedPathDisplay(path = path)

        Spacer(modifier = Modifier.height(8.dp))

        FileList(
            path = path,
            items = items,
        )

        Spacer(modifier = Modifier.height(8.dp))

        BottomToolbar(
            modifier = Modifier,
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
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.CenterHorizontally),
        text = path.value.substringAfterLast("/"),
        textAlign = TextAlign.Center

    )
}

@Composable
fun ColumnScope.BottomToolbar(
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
            modifier = Modifier
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
            modifier = Modifier
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    ) {
        Button(
            modifier = Modifier
                .padding(horizontal = 5.dp)
                .width(IntrinsicSize.Min),
            onClick = {
                path.value = path.value.substringBeforeLast("/")
            }
        ) {
            Text(text = "Remonter")
        }

        Button(
            modifier = Modifier
                .padding(horizontal = 5.dp)
                .width(IntrinsicSize.Min),
            onClick = {
                path.value = "/storage/emulated/0/Download"
            }
        ) {
            Text(text = "Téléchargements")
        }

        Button(
            modifier = Modifier
                .padding(horizontal = 5.dp)
                .width(IntrinsicSize.Min),
            onClick = {
                path.value = "/storage/emulated/0"
            }
        ) {
            Text(text = "Stockage principal")
        }

        Button(
            modifier = Modifier
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
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(width = 1.dp, color = Color.Gray, shape = RoundedCornerShape(8.dp))
    ) {
        val isEmpty = items.value.isEmpty()

        if (isEmpty)
            item {
                Box(
                    modifier = Modifier.fillParentMaxSize(),
                    contentAlignment = Alignment.Center
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .clickable {
                if (item.isFolder())
                    path.value = item.fullPath
            }
    ) {
        AsyncImage(
            modifier = Modifier
                .size(50.dp)
                .padding(end = 10.dp),
            model = if (item.isFile()) R.drawable.file else R.drawable.folder_empty,
            contentDescription = "Miniature",
            contentScale = ContentScale.Fit,

            )

        Text(
            modifier = Modifier
                .align(Alignment.CenterVertically),
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
){
    suspend fun toHomeItemInfosDTO(): HomeItemInfosDTO {
        val videoEmbedder = VideoInfoEmbedder()
        return HomeItemInfosDTO(
            oldTitle = oldTitle,
            newTitle = newTitle,
            path = path,
            picture = if (picture != null) videoEmbedder.bitmapToBase64(picture)
                else null
        )
    }
}

data class HomeItemInfosDTO(
    val oldTitle: String? = null,
    val newTitle: String? = null,
    val path: String?,
    val picture: String?,
){
    suspend fun toHomeItemInfos(): HomeItemInfos {
        val videoEmbedder = VideoInfoEmbedder()
        return HomeItemInfos(
            oldTitle = oldTitle,
            newTitle = newTitle,
            path = path,
            picture = if (picture != null) videoEmbedder.base64ToBitmap(picture)
            else null
        )
    }
}


