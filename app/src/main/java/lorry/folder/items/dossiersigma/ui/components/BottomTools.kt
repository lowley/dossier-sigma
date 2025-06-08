package lorry.folder.items.dossiersigma.ui.components

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.ui.SigmaViewModel
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class BottomTools @Inject constructor(
    val context: Context,
    val viewModel: SigmaViewModel,
) {
    private val _bottomToolsContent = MutableStateFlow(null)
    val bottomToolContent: StateFlow<BottomToolContent?> = _bottomToolsContent

    @Composable
    fun BottomToolBar(bottomToolContent: BottomToolContent?) {
        bottomToolContent?.let { content ->
            val toolList by content.tools.collectAsState()
            Row {
                toolList.forEach { tool ->
                    IconButton(onClick = tool.onClick) {
                        Icon(
                            painter = painterResource(id = tool.icon),
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
    
    
    
}

class BottomToolContent{
    private val _tools = MutableStateFlow(emptyList<Tool>())
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
    val onClick: () -> Unit
)