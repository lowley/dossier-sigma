package lorry.folder.items.dossiersigma.ui.folderContentFront

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.headless.domain.Item
import lorry.folder.items.dossiersigma.ui.folderContentFront.utils.Tool
import lorry.folder.items.dossiersigma.ui.sigma.DragState
import javax.inject.Inject

@HiltViewModel
class FolderContentFrontViewModel @Inject constructor() : ViewModel() {

    /////////////////
    // drag'n drop //
    /////////////////
    val _dragTargetItem = MutableStateFlow<Item?>(null)

    val _dragState = MutableStateFlow<DragState?>(null)

    val _draggableStartPosition = MutableStateFlow<Offset?>(null)
    val draggableStartPosition: StateFlow<Offset?> = _draggableStartPosition



}