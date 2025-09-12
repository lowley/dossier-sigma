package lorry.folder.items.dossiersigma.ui.folderContent.tools.ui

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
import lorry.folder.items.dossiersigma.ComponentWithViewModel
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
import lorry.folder.items.dossiersigma.ui.browser.changeState
import lorry.folder.items.dossiersigma.ui.browser.manageImageClick
import lorry.folder.items.dossiersigma.ui.browser.utilities.BrowserTarget
import lorry.folder.items.dossiersigma.ui.folderContent.tools.controller.IBottomComponent
import lorry.folder.items.dossiersigma.ui.items.utils.imageAsAnyToTempUri
import lorry.folder.items.dossiersigma.ui.sigma.DragState
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity
import lorry.folder.items.dossiersigma.ui.sigma.SigmaColors
import lorry.folder.items.dossiersigma.ui.sigma.SigmaViewModel
import lorry.folder.items.dossiersigma.ui.sigma.SortingCriterion
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.get
import kotlin.math.roundToInt

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
@Suppress("CONTEXT_RECEIVERS_DEPRECATED")
@Singleton
class BottomTools @Inject constructor(
    val moveToNASComponent: IMoveToNASComponent,
    val component: IBottomComponent
): ComponentWithViewModel<SigmaViewModel>() {


//    val frontViewModel: FolderContentFrontViewModel by lazy {
//        ViewModelProvider(owner)[FolderContentFrontViewModel::class.java]
//    }

    init {
        Tools.DEFAULT.bottomTools = this
        Tools.TAGS_MENU.bottomTools = this
        Tools.FILE.bottomTools = this
        Tools.MOVES.bottomTools = this
        Tools.COPY_FILE.bottomTools = this
        Tools.MOVE_FILE.bottomTools = this
        Tools.CROP.bottomTools = this
    }

    @Composable
    fun BottomToolBar(
        activity: SigmaActivity,
        beginDrag: (Tool, Offset) -> Unit,
        terminateDrag: () -> Unit,
        setDragTargetItem: (Item?) -> Unit,
        addDragOffset: (Offset) -> Unit,
        dragTargetItem: StateFlow<Item?>
    ) {
        val content = component.currentContent.collectAsState().value
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
                    beginDrag = beginDrag,
                    terminateDrag = terminateDrag,
                    setDragTargetItem = setDragTargetItem,
                    addDragOffset = addDragOffset,
                    dragTargetItem = dragTargetItem
                )
            }
        }
    }



    @Composable
    context(BoxScope)
    fun MobileSticker(
        dragState: DragState,
        activity: SigmaActivity,
    ) {
        val tool: Tool = dragState.tool
        val offset: Offset = dragState.offset

        Box(
            modifier = Modifier.Companion
                .width(85.dp)
                .fillMaxHeight()
                .clickable {
                    component.setCurrentTool(tool)
                    viewModel.viewModelScope.launch {
                        tool.onClick(tool, viewModel, activity)
                    }
                }
        ) {
            StickerIcon(
                modifier = Modifier.Companion
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

sealed class Tools {

    abstract fun content(viewModel: SigmaViewModel? = null): BottomToolContent
    lateinit var bottomTools: BottomTools

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
                modifier = Modifier.Companion
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
