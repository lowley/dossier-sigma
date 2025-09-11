package lorry.folder.items.dossiersigma.ui.folderContent.tools.utils

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.headless.domain.Item
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.BottomToolContent
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.OverallProgress
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.Tool
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ToolsViewModel @Inject constructor(

): ViewModel(){

    ////////////////////////
    // étiquette courante //
    ////////////////////////

    val _currentFlagId = MutableStateFlow<UUID?>(null)

    /////////////////////////////////
    // différentes barres d'outils //
    /////////////////////////////////
    val defaultContent = BottomToolContent(emptyList(), "DEFAULT_CONTENT")
    val _bottomToolsContent = MutableStateFlow<BottomToolContent?>(defaultContent)

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
    val _progress = MutableStateFlow(0)
    val _movePasteText = MutableStateFlow("Coller")
    val _NASprogress = MutableStateFlow<OverallProgress?>(null)
    val _copyNASText = MutableStateFlow("1 -> NAS")
    val _copyAllNASText = MutableStateFlow("Tous -> NAS")





}