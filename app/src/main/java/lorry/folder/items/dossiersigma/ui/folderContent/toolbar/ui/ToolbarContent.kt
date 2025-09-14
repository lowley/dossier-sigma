package lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ToolbarContent(
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