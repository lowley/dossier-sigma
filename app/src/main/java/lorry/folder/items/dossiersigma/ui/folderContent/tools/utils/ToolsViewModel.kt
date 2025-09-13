package lorry.folder.items.dossiersigma.ui.folderContent.tools.utils

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import lorry.folder.items.dossiersigma.headless.domain.Item
import lorry.folder.items.dossiersigma.ui.folderContent.tools.controller.OverallProgress
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.BottomToolContent
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.Tool
import javax.inject.Inject

@HiltViewModel
class ToolsViewModel @Inject constructor(
    val rawFeed: IRawFeed
): ViewModel(){

    /////////////////////////////////////////////////////////////////////////////
    // outil sélectionné, destiné à l'affichage par remontée dans MainActivity //
    /////////////////////////////////////////////////////////////////////////////
    val _currentTool = MutableStateFlow<Tool?>(null)

    ///////////////////////////////////
    // copie/déplacement de fichiers //
    ///////////////////////////////////
    var movingItem: Item? = null
    var copyingItem: Item? = null
    var itemToMove: Item? = null

}