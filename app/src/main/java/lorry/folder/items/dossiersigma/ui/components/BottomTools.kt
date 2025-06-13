package lorry.folder.items.dossiersigma.ui.components

import android.content.Context
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.ui.SigmaViewModel
import java.io.File
import javax.inject.Inject

class BottomTools @Inject constructor(
    val context: Context,
) {
    lateinit var viewModel: SigmaViewModel

    private val _bottomToolsContent = MutableStateFlow<BottomToolContent?>(null)
    val currentContent: StateFlow<BottomToolContent?> = _bottomToolsContent

    fun setCurrentContent(content: BottomToolContent) {
        _bottomToolsContent.value = content
    }

    //destiné à l'affichage par remontée dans MainActivity
    private val _currentTool = MutableStateFlow<Tool?>(null)
    val currentTool: StateFlow<Tool?> = _currentTool

    fun setCurrentTool(tool: Tool?) {
        _currentTool.value = tool
    }

    @Composable
    fun BottomToolBar(
        openDialog: MutableState<Boolean>
    ) {
        if (currentContent == null)
            return

        val content by currentContent.collectAsState()
        val toolList by content?.tools?.collectAsState() ?: remember { mutableStateOf(emptyList()) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            toolList.forEach { tool ->
                Box(
                    modifier = Modifier
                        .width(130.dp)
                        .fillMaxHeight()
                        .clickable {
                            setCurrentTool(tool)
                            viewModel.setDialogMessage(tool.text)
                            openDialog.value = true
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
    val onClick: (String, SigmaViewModel, Context) -> Unit
)

sealed class Tools(
    val content: BottomToolContent
) {
    object EXPLORER_DEFAULT : Tools(
        BottomToolContent(
            listOf(
                Tool(
                    text = "Créer dossier",
                    icon = R.drawable.plus,
                    onClick = { newName, viewModel, context ->
                        val currentFolderPath = viewModel.currentFolderPath.value
                        val newFullName = "$currentFolderPath/$newName"
                        if (!File(newFullName).exists()) {
                            if (File(newFullName).mkdir()) {
                                Toast.makeText(context, "Répertoire créé", Toast.LENGTH_SHORT).show()
                                viewModel.refreshCurrentFolder()
                            } else
                                Toast.makeText(context, "Un problème est survenu", Toast.LENGTH_SHORT)
                                    .show()
                        }
                    }
                )
            )))

    object EXPLORER_FILE : Tools(
        BottomToolContent(
            toolInit = listOf(
                Tool(
                    text = "Copier",
                    icon = R.drawable.copier,
                    onClick = { newName, viewModel, context ->


                        //vm.diskRepository.copyFile(sourceFile, destinationFile)

                    }
                ),
                Tool(
                    text = "Déplacer",
                    icon = R.drawable.deplacer,
                    onClick = { newName, viewModel, context ->


                    }
                ),
                Tool(
                    text = "Renommer",
                    icon = R.drawable.renommer,
                    onClick = { newName, viewModel, context ->


                    }
                ),
                Tool(
                    text = "Supprimer",
                    icon = R.drawable.corbeille,
                    onClick = { newName, viewModel, context ->


                    }
                )
            )
        ))

    object EXPLORER_COPY_FILE : Tools(
        BottomToolContent(
            listOf(
                Tool(
                    text = "Annuler",
                    icon = R.drawable.annuler,
                    onClick = { newName, viewModel, context ->


                    }
                ),
                Tool(
                    text = "Coller",
                    icon = R.drawable.coller,
                    onClick = { newName, viewModel, context ->


                        //vm.diskRepository.copyFile(sourceFile, destinationFile)

                    }
                )
            )
        ))

    object EXPLORER_MOVE_FILE : Tools(
        BottomToolContent(
            listOf(
                Tool(
                    text = "Annuler",
                    icon = R.drawable.annuler,
                    onClick = { newName, viewModel, context ->


                    }
                ),
                Tool(
                    text = "Coller",
                    icon = R.drawable.coller,
                    onClick = { newName, viewModel, context ->


                        //vm.diskRepository.copyFile(sourceFile, destinationFile)

                    }
                )
            )
        ))

//    object SHORTCUTS: Tools(BottomToolContent(
//
//    ))

}

@Composable
fun CustomDialog(
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






