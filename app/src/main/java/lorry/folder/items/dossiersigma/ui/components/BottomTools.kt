package lorry.folder.items.dossiersigma.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewModelScope
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.google.gson.Gson
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.data.base64.Tags
import lorry.folder.items.dossiersigma.domain.ColoredTag
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.domain.services.MoveFileService
import lorry.folder.items.dossiersigma.domain.services.MoveToNASService
import lorry.folder.items.dossiersigma.domain.usecases.browser.BrowserTarget
import lorry.folder.items.dossiersigma.ui.ITEMS_ORDERING_STRATEGY
import lorry.folder.items.dossiersigma.ui.MainActivity
import lorry.folder.items.dossiersigma.ui.SigmaViewModel
import lorry.folder.items.dossiersigma.ui.components.BottomTools.Companion.itemToMove
import lorry.folder.items.dossiersigma.ui.components.BottomTools.Companion.movingItem
import lorry.folder.items.dossiersigma.ui.containsFlagAsValue
import java.io.File
import java.util.UUID
import javax.inject.Inject

class BottomTools @Inject constructor(
    val context: Context,
) {
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

    companion object {
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
    }

    @Composable
    fun BottomToolBar(
        openDialog: MutableState<Boolean>,
        activity: MainActivity
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
                var offset by remember { mutableStateOf(Offset.Zero) }
                var iconSize = if (offset == Offset.Zero) 28.dp else 140.dp
                var iconYDelta = if (offset == Offset.Zero) 0 else 200

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
                            .size(28.dp),
                        painter = painterResource(id = tool.icon),
                        contentDescription = null,
                        tint = if (tool.isColoredIcon) Color.Unspecified else
                            (tool.tint ?: Color(0xFFe9c46a))
                    )

                    if (content?.name == "DEFAULT_CONTENT") {
                        val coloredTag = tool.toColoredTag(viewModel)
                        var draggableStartPosition = viewModel.draggableStartPosition.collectAsState()

                        Icon(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 10.dp)
                                .offset {
                                    IntOffset(
                                        offset.x.toInt(), offset.y.toInt()
//                                        - iconYDelta) 
                                    )
                                }
                                .size(iconSize)
                                .onGloballyPositioned {
                                    viewModel.setDraggableStartPosition(it.positionInRoot())
                                }
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = {
//                                            movingItem = viewModel.selectedItem.value
                                            viewModel.setDraggedTag(coloredTag)
//                                            println("DRAG start, ${coloredTag.title}")
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            offset += dragAmount
                                            val u = offset.x
                                            val v = offset.y
                                            val newOffset = offset.copy(
                                                y = offset.y - iconYDelta
                                            )
                                            val currentGlobalOffset =
                                                (draggableStartPosition.value ?: Offset.Zero) + offset

                                            viewModel.setDragOffset(offset)
//                                            println("DRAG offset: ${currentGlobalOffset.x}, ${currentGlobalOffset.y}")
                                        },
                                        onDragEnd = {
//                                            movingItem = null
                                            val target = viewModel.dragTargetItem.value
//                                            println("DRAG end, ${target?.name ?:"null"}")

                                            if (target != null) {
                                                viewModel.assignColoredTagToItem(target, coloredTag)
                                                viewModel.setDragOffset(null)
                                                viewModel.setDraggedTag(null)
                                                viewModel.setDragTargetItem(null)
                                            }
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
    val onClick: suspend (SigmaViewModel, MainActivity) -> Any?,
    val visible: suspend (SigmaViewModel, MainActivity) -> Boolean = { _, _ -> true },
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
            viewModel?.bottomTools?.defaultContent ?: BottomToolContent(emptyList(), "DEFAULT")
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
                        viewModel.selectedItem.value?.tag == null
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

                                    if (currentItem.isFile())
                                        viewModel.base64Embedder.appendFlagToFile(
                                            File(currentItem.fullPath), ColoredTag(
                                                title = tagInfos.title,
                                                color = tagInfos.color,
                                                id = newTool.id
                                            )
                                        )

                                    if (currentItem.isFolder())
                                        viewModel.diskRepository.insertTagToHtmlFile(
                                            currentItem, ColoredTag(
                                                title = tagInfos.title,
                                                color = tagInfos.color,
                                                id = newTool.id
                                            )
                                        )

//                                    if (currentItem != null && tagInfos != null) {
//                                        currentItem.tag = ColoredTag(
//                                            title = tagInfos.title,
//                                            color = tagInfos.color
//                                        )
//                                    }

                                    viewModel.refreshCurrentFolder()
                                }

                                viewModel.bottomTools.setCurrentContent(DEFAULT)
                                viewModel.setSelectedItem(null, true)
                            }

                            mainActivity.openTagInfosDialog.value = true
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
                        viewModel.selectedItem.value?.tag != null
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
                    text = { "Supprimer" },
                    icon = R.drawable.moins,
                    visible = { viewModel, mainActivity ->
                        viewModel.selectedItem.value?.tag != null
                    },
                    onClick = { viewModel, mainActivity ->
                        run {
                            val currentItem = viewModel.selectedItem.value
                            if (currentItem == null)
                                return@run

                            val tool = DEFAULT.content(viewModel)
                                .tools.value.firstOrNull { it.id == currentItem.tag?.id }

                            if (tool == null) {
                                println("problème, tool inexistant")
                                return@run
                            }
                            
                            if (viewModel.removeFlagCacheForKey(currentItem.fullPath) == null) {
                                println("problème, suppression de tag impossible")
                                return@run
                            }

                            if (currentItem.isFile())
                                viewModel.base64Embedder.removeFlagFromFile(File(currentItem.fullPath))
                            else {
                                //c'est un dossier
                                viewModel.diskRepository.removeTagFromHtml(currentItem.fullPath)
                            }

                            if (!viewModel.flagCache.containsFlagAsValue(tool.id))
                                DEFAULT.content(viewModel).removeTool(tool)

                            viewModel.setSelectedItem(null, true)
                            viewModel.refreshCurrentFolder()
                            viewModel.bottomTools.setCurrentContent(DEFAULT)

//                            viewModel.clearFlagCache()
//                            DEFAULT.content().updateTools(emptyList<Tool>())
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
                            viewModel.selectedItem.value?.tag != null
                        },
                    onClick =
                        { viewModel, mainActivity ->
                            run {
                                val files = viewModel.currentFolder.value.items

                                files.forEach {
                                    if (it.isFile())
                                        viewModel.base64Embedder.removeFlagFromFile(File(it.fullPath))
                                    else {
                                        //c'est un dossier
                                        viewModel.diskRepository.removeTagFromHtml(it.fullPath)
                                    }
                                }

                                viewModel.clearFlagCache()
                                DEFAULT.content().updateTools(emptyList<Tool>())

                                viewModel.setSelectedItem(null, true)
                                viewModel.refreshCurrentFolder()
                                viewModel.bottomTools.setCurrentContent(DEFAULT)
                            }
                        }
                )
            ),
            "TAGS_MENU"
        )
    }

    object TAGS : Tools() {
        override fun content(viewModel: SigmaViewModel?) = BottomToolContent(
            listOf(
                Tool(
                    text = { "TAGS" },
                    icon = R.drawable.move,
                    isColoredIcon = true,
                    onClick = { viewModel, mainActivity ->
//                        viewModel.bottomTools.setCurrentContent(MOVES)
                    }
                )
            ),
            "TAGS"
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
                        viewModel.bottomTools.setCurrentContent(MOVES)
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
                        viewModel.bottomTools.setCurrentContent(TAGS_MENU)
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
                        viewModel.bottomTools.setCurrentContent(CROP)
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
                        //viewModel.setSelectedItem(null)
                        viewModel.setDialogMessage("Nouveau nom du dossier")
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
                                    )
                                        .show()
                                    return@run
                                }

                                val newFullName = "${
                                    currentFolderPath
                                        .substringBeforeLast("/")
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

                            viewModel.bottomTools.setCurrentContent(DEFAULT)
                            viewModel.setSelectedItem(null, true)
                        }

                        mainActivity.openTextDialog.value = true
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

                            viewModel.bottomTools.setCurrentContent(DEFAULT)
                            viewModel.setSelectedItem(null, true)
                        }

                        mainActivity.openTextDialog.value = true
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

                                val parentPath = parent.fullPath
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
                                    )
                                        .show()
                            }

                            viewModel.bottomTools.setCurrentContent(DEFAULT)
                            viewModel.setSelectedItem(null, true)

                        }

                        mainActivity.openTextDialog.value = true

                    }
                ),
                ///////////////
                // supprimer //
                ///////////////
                Tool(
                    text = { "Supprimer" },
                    icon = R.drawable.corbeille,
                    onClick = { viewModel, mainActivity ->
                        val currentFolderPath = viewModel.selectedItem.value?.fullPath
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
                                val itemFullPath = item?.fullPath ?: ""
                                if (item == null)
                                    return@run

                                if (item.isFolder())
                                    File(item.fullPath).deleteRecursively()
                                else File(item.fullPath).delete()
                                viewModel.setSelectedItem(null, true)
                                viewModel.refreshCurrentFolder()

                                if (File(itemFullPath).exists())
                                    Toast.makeText(
                                        mainActivity,
                                        "Un problème lors de la suppression est survenu",
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                                else Toast.makeText(
                                    mainActivity,
                                    "Suppression effectuée",
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                            }

                            viewModel.bottomTools.setCurrentContent(DEFAULT)
                        }

                        mainActivity.openYesNoDialog.value = true
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
                        movingItem = viewModel.selectedItem.value
                        viewModel.bottomTools.setCurrentContent(MOVE_FILE)
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
                            itemToMove = viewModel.selectedItem.value

                            if (itemToMove == null)
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
                                        listOf<String>(
                                            itemToMove?.fullPath
                                                ?: ""
                                        )
                                    )
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


                        viewModel.bottomTools.setCurrentContent(DEFAULT)
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
                        viewModel.bottomTools.setCurrentContent(DEFAULT)
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
                        viewModel.bottomTools.setCurrentContent(DEFAULT)
                        val item = movingItem
                        val movingParent = item?.fullPath?.substringBeforeLast("/")

                        if (movingParent != null)
                            viewModel.goToFolder(movingParent)
                        movingItem = null
                        viewModel.setSelectedItem(null, true)
                        viewModel.refreshCurrentFolder()
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
                            itemToMove = viewModel.selectedItem.value
                            var dest = itemToMove

                            if (dest == null) {
                                itemToMove = viewModel.currentFolder.value
                                dest = itemToMove
                            }

                            //toast
                            println("MovingItem: choisir fichier destination")
                            //1.copie
                            val sourceFile = File(movingItem?.fullPath ?: "")
                            //créer service avec notification(avec avancement)
                            //dans le service: copie
                            //passer au service une lambda pour l'action de retour(2.+3.)

                            //Toast pour informer de déplacement:
                            //début copie, fin déplacement/échec

                            if (dest!!.isFile()) {
                                if (sourceFile.path.substringAfterLast("/")
                                    == dest.fullPath.substringAfterLast("/")
                                ) {
                                    mainActivity.openMoveFileDialog.value = true
                                    return@run
                                }
                            }

                            if (dest.isFolder()) {
                                if (movingItem == null)
                                    return@run
                                val isItemExists = viewModel.diskRepository.isFileOrFolderExists(
                                    dest.fullPath,
                                    movingItem!!
                                )
                                if (isItemExists) {
                                    mainActivity.openMoveFileDialog.value = true
                                    return@run
                                }
                            }

                            /**
                             * le fichier n'existe pas, on lance la copie,
                             * le reste est effectué dans
                             * @see MoveFileService.onStartCommand
                             */
                            val intent = Intent(mainActivity, MoveFileService::class.java).apply {
                                putExtra("source", movingItem?.fullPath ?: "")
                                putExtra("destination", dest.fullPath)
                                putExtra("addSuffix", "")
                            }
                            mainActivity.startService(intent)
                            viewModel.refreshCurrentFolder()
                            //2.vérif copie bien réalisée:
                            //dest existe
                            //tailles égales

                            //3.si ok: suppression source


                            //vm.diskRepository.copyFile(sourceFile, destinationFile)
//                        viewModel.bottomTools.setCurrentContent(DEFAULT, viewModel)
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
                        changeCrop(viewModel, mainActivity, ContentScale.None)
                    }
                ),

                Tool(
                    text = { "Rogner" },
                    icon = R.drawable.crop,
                    onClick = { viewModel, mainActivity ->
                        changeCrop(viewModel, mainActivity, ContentScale.Crop)
                    }
                ),

                Tool(
                    text = { "Remplir ⇅" },
                    icon = R.drawable.crop,
                    onClick = { viewModel, mainActivity ->
                        changeCrop(viewModel, mainActivity, ContentScale.FillHeight)
                    }
                ),

                Tool(
                    text = { "Remplir ⇿" },
                    icon = R.drawable.crop,
                    onClick = { viewModel, mainActivity ->
                        changeCrop(viewModel, mainActivity, ContentScale.FillWidth)
                    }
                ),

                Tool(
                    text = { "Etirer" },
                    icon = R.drawable.crop,
                    onClick = { viewModel, mainActivity ->
                        changeCrop(viewModel, mainActivity, ContentScale.Fit)
                    }
                ),

                Tool(
                    text = { "Dedans" },
                    icon = R.drawable.crop,
                    onClick = { viewModel, mainActivity ->
                        changeCrop(viewModel, mainActivity, ContentScale.Inside)
                    }
                ),

                Tool(
                    text = { "Manuel" },
                    icon = R.drawable.image,
                    isColoredIcon = false,
                    onClick = { viewModel, mainActivity ->
                        run {
                            val item = viewModel.selectedItem.value
                            var sourceBitmap: Bitmap? = null
                            if (item?.isFile() == true) {
                                val test = viewModel.base64Embedder.extractBase64FromFile(
                                    File(item.fullPath),
                                    tagSuffix = Tags.COVER
                                )
                                if (test == null)
                                    return@run

                                sourceBitmap = viewModel.base64Embedder.base64ToBitmap(test)
                            }

                            if (item?.isFolder() == true) {
                                sourceBitmap = viewModel.base64DataSource.extractImageFromHtml(
                                    item
                                        .fullPath + "/.folderPicture.html"
                                )
                            }

                            if (item == null || sourceBitmap == null)
                                return@run

//                            val sourceBitmap = viewModel.imageCache[viewModel.selectedItem.value?.fullPath ?: ""] as Bitmap

                            val sourceUri = bitmapToTempUri(mainActivity, sourceBitmap)
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

fun changeCrop(
    viewModel: SigmaViewModel, activity: MainActivity, scale:
    ContentScale
) {
    val item = viewModel.selectedItem.value ?: return
    viewModel.scaleCache[item.fullPath] = scale

    if (item.isFile() &&
        item.fullPath.endsWith(".mp4") ||
        item.fullPath.endsWith(".avi") ||
        item.fullPath.endsWith(".mpg") ||
        item.fullPath.endsWith(".html") ||
        item.fullPath.endsWith(".iso") ||
        item.fullPath.endsWith(".mkv")
    ) {
        viewModel.viewModelScope.launch {
            val file = File(item.fullPath)
            viewModel.base64Embedder.removeScale(file)
            viewModel.base64Embedder.appendScaleToFile(file, scale)
        }
    }

    if (item.isFolder()) {
        viewModel.viewModelScope.launch {
            val file = File(item.fullPath + "/.folderPicture.html")
            if (!file.exists())
                viewModel.diskRepository.createFolderHtmlFile(item)
            viewModel.diskRepository.removeScaleFromHtml(item.fullPath)
            viewModel.diskRepository.insertScaleToHtmlFile(item, scale)
            viewModel.refreshCurrentFolder()
        }
    }

    //viewModel.setSelectedItem(null)
    //viewModel.bottomTools.setCurrentContent(DEFAULT, viewModel)
}

@Composable
fun CustomTextDialog(
    text: String,
    openDialog: MutableState<Boolean>,
    onOk: (String) -> Unit
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
                    openDialog.value = false
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
                        openDialog.value = false
                    }
                ) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        onOk(editMessage.value)
                        openDialog.value = false
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
    openDialog: MutableState<Boolean>,
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
                    openDialog.value = false
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
                        openDialog.value = false
                        onOk(false)
                    }
                ) {
                    Text("Non")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        openDialog.value = false
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
    openDialog: MutableState<Boolean>,
    onOverwrite: () -> Unit,
    onCancel: () -> Unit,
    onCreateCopy: () -> Unit,
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
                    openDialog.value = false
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
                        openDialog.value = false
                        onCancel()
                    }
                ) {
                    Text("Abandonner")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        openDialog.value = false
                        onCreateCopy()
                    }
                ) {
                    Text("Créer une copie")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        openDialog.value = false
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
    openDialog: MutableState<Boolean>,
    onDatasCompleted:
    suspend (tagInfos: TagInfos?, viewModel: SigmaViewModel, activity: MainActivity) -> Unit,
    viewModel: SigmaViewModel,
    mainActivity: MainActivity
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
                    openDialog.value = false
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
                        openDialog.value = false
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

                        openDialog.value = false
                    }
                ) {
                    Text("Valider")
                }
            }
        }
    }
}

data class TagInfos(
    val title: String,
    val color: Color
)





