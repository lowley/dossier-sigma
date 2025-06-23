package lorry.folder.items.dossiersigma.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.data.base64.Tags
import lorry.folder.items.dossiersigma.domain.usecases.browser.BrowserTarget
import lorry.folder.items.dossiersigma.ui.ITEMS_ORDERING_STRATEGY
import lorry.folder.items.dossiersigma.ui.MainActivity
import lorry.folder.items.dossiersigma.ui.SigmaViewModel
import java.io.File
import javax.inject.Inject
import kotlin.collections.set

class BottomTools @Inject constructor(
    val context: Context,
) {
    lateinit var viewModel: SigmaViewModel

    private val _bottomToolsContent = MutableStateFlow<BottomToolContent?>(null)
    val currentContent: StateFlow<BottomToolContent?> = _bottomToolsContent

    fun setCurrentContent(tools: Tools) {
        _bottomToolsContent.value = tools.content
    }

    //destiné à l'affichage par remontée dans MainActivity
    private val _currentTool = MutableStateFlow<Tool?>(null)
    val currentTool: StateFlow<Tool?> = _currentTool

    fun setCurrentTool(tool: Tool?) {
        _currentTool.value = tool
    }

    @Composable
    fun BottomToolBar(
        openDialog: MutableState<Boolean>,
        activity: MainActivity
    ) {
        if (currentContent == null)
            return

        val content by currentContent.collectAsState()
        val toolList by content?.tools?.collectAsState() ?: remember { mutableStateOf(emptyList()) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            toolList.forEach { tool ->
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
                        tint = Color(0xFFe9c46a)
                    )

                    Text(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .height(24.dp),
                        text = tool.text,
                        color = Color(0xFFe9c46a)
                    )
                }
            }
        }
    }
}

class BottomToolContent(
    var toolInit: List<Tool>
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
    val text: String,
    @DrawableRes val icon: Int,
    val onClick: suspend (SigmaViewModel, MainActivity) -> Any?
)

sealed class Tools(
    val content: BottomToolContent
) {
    object DEFAULT : Tools(
        content = BottomToolContent(
            listOf(
                ///////////////
                // + dossier //
                ///////////////
                Tool(
                    text = "dossier",
                    icon = R.drawable.plus,
                    onClick = { viewModel, mainActivity ->
                        viewModel.setDialogMessage("Nom du dossier à créer")
                        viewModel.dialogOnOkLambda = { newName, viewModel, mainActivity ->
                            val currentFolderPath = viewModel.currentFolderPath.value
                            val newFullName = "$currentFolderPath/$newName"
                            if (!File(newFullName).exists()) {
                                if (File(newFullName).mkdir()) {
                                    Toast.makeText(mainActivity, "Répertoire créé", Toast.LENGTH_SHORT).show()
                                    viewModel.refreshCurrentFolder()
                                } else
                                    Toast.makeText(
                                        mainActivity,
                                        "Un problème est survenu",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                            }
                        }

                        mainActivity.openTextDialog.value = true
                    }
                ),
                ////////////////////////
                // supprimer totalité //
                ////////////////////////
//                Tool(
//                    text = "tout",
//                    icon = R.drawable.corbeille,
//                    onClick = { viewModel, mainActivity ->
//                        
//                    }
//                )
            )))

    object FILE : Tools(
        BottomToolContent(
            toolInit = listOf(
                //////////////////
                // image google //
                //////////////////
                Tool(
                    text = "Google",
                    icon = R.drawable.image_nb,
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
                //////////
                // crop //
                //////////
                Tool(
                    text = "Placement",
                    icon = R.drawable.image_nb,
                    onClick = { viewModel, mainActivity ->
                        viewModel.bottomTools.setCurrentContent(Tools.CROP)
                    }
                ),
                ////////////
                // copier //
                ////////////
                Tool(
                    text = "Copier",
                    icon = R.drawable.copier,
                    onClick = { viewModel, mainActivity ->


                        //vm.diskRepository.copyFile(sourceFile, destinationFile)
                    }
                ),
                //////////////
                // déplacer //
                //////////////
                Tool(
                    text = "Déplacer",
                    icon = R.drawable.deplacer,
                    onClick = { viewModel, mainActivity ->


                    }
                ),
                //////////////
                // renommer //
                //////////////
                Tool(
                    text = "Renommer",
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
                            viewModel.setSelectedItem(null)
                        }

                        mainActivity.openTextDialog.value = true
                    }
                ),
                /////////////////////
                // + dossier frère //
                /////////////////////
                Tool(
                    text = "+ frère",
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
                            viewModel.setSelectedItem(null)
                        }

                        mainActivity.openTextDialog.value = true
                    }
                ),
                ////////////////////
                // + dossier fils //
                ////////////////////
                Tool(
                    text = "+ fils",
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
                            viewModel.setSelectedItem(null)

                        }

                        mainActivity.openTextDialog.value = true

                    }
                ),
                ///////////////
                // supprimer //
                ///////////////
                Tool(
                    text = "Supprimer",
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
                                viewModel.setSelectedItem(null)
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
            )
        ))

    object COPY_FILE : Tools(
        BottomToolContent(
            listOf(
                /////////////
                // annuler //
                /////////////
                Tool(
                    text = "Annuler",
                    icon = R.drawable.annuler,
                    onClick = { viewModel, mainActivity ->


                        viewModel.bottomTools.setCurrentContent(DEFAULT)
                    }
                ),
                ////////////
                // coller //
                ////////////
                Tool(
                    text = "Coller",
                    icon = R.drawable.coller,
                    onClick = { viewModel, mainActivity ->


                        //vm.diskRepository.copyFile(sourceFile, destinationFile)
                        viewModel.bottomTools.setCurrentContent(DEFAULT)
                    }
                )
            )
        ))

    object MOVE_FILE : Tools(
        BottomToolContent(
            listOf(
                /////////////
                // annuler //
                /////////////
                Tool(
                    text = "Annuler",
                    icon = R.drawable.annuler,
                    onClick = { viewModel, mainActivity ->


                        viewModel.bottomTools.setCurrentContent(DEFAULT)
                    }
                ),
                ////////////
                // coller //
                ////////////
                Tool(
                    text = "Coller",
                    icon = R.drawable.coller,
                    onClick = { viewModel, mainActivity ->


                        //vm.diskRepository.copyFile(sourceFile, destinationFile)
                        viewModel.bottomTools.setCurrentContent(DEFAULT)
                    }
                )
            )
        ))
    object CROP : Tools(
        BottomToolContent(
            toolInit = listOf(
                Tool(
                    text = "Aucun",
                    icon = R.drawable.crop,
                    onClick = { viewModel, mainActivity ->
                        changeCrop(viewModel, mainActivity, ContentScale.None)
                    }
                ),
                
                Tool(
                    text = "Rogner",
                    icon = R.drawable.crop,
                    onClick = { viewModel, mainActivity ->
                        changeCrop(viewModel, mainActivity, ContentScale.Crop)
                    }
                ),

                Tool(
                    text = "Remplir ⇅",
                    icon = R.drawable.crop,
                    onClick = { viewModel, mainActivity ->
                        changeCrop(viewModel, mainActivity, ContentScale.FillHeight)
                    }
                ),

                Tool(
                    text = "Remplir ⇿",
                    icon = R.drawable.crop,
                    onClick = { viewModel, mainActivity ->
                        changeCrop(viewModel, mainActivity, ContentScale.FillWidth)
                    }
                ),

                Tool(
                    text = "Etirer",
                    icon = R.drawable.crop,
                    onClick = { viewModel, mainActivity ->
                        changeCrop(viewModel, mainActivity, ContentScale.Fit)
                    }
                ),

                Tool(
                    text = "Dedans",
                    icon = R.drawable.crop,
                    onClick = { viewModel, mainActivity ->
                        changeCrop(viewModel, mainActivity, ContentScale.Inside)
                    }
                ),

                Tool(
                    text = "Manuel",
                    icon = R.drawable.image,
                    onClick = { viewModel, mainActivity ->
                        run {
                            val item = viewModel.selectedItem.value
                            var sourceBitmap: Bitmap? = null
                            if (item?.isFile() == true) {
                                val test = viewModel.base64Embedder.extractBase64FromMp4(File(item.fullPath),
                                    tagSuffix = Tags.COVER)
                                if (test == null)
                                    return@run
                                
                                sourceBitmap = viewModel.base64Embedder.base64ToBitmap(test)
                            }
                            
                            if (item?.isFolder() == true) {
                                sourceBitmap = viewModel.base64DataSource.extractImageFromHtml(item
                                    .fullPath+"/.folderPicture.html")
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
            )
        )
    )

//    object SHORTCUTS: Tools(BottomToolContent(
//
//    ))

}

fun changeCrop(viewModel: SigmaViewModel, activity: MainActivity, scale:
ContentScale) {
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
            viewModel.base64Embedder.removeEmbeddedContentScale(file)
            viewModel.base64Embedder.appendContentScaleToMp4(file, scale)
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
    //viewModel.bottomTools.setCurrentContent(DEFAULT)
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






