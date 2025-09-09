package lorry.folder.items.dossiersigma.UI.browser

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import lorry.folder.items.dossiersigma.headless.domain.Item
import lorry.folder.items.dossiersigma.UI.browser.utilities.BrowserState
import lorry.folder.items.dossiersigma.UI.browser.utilities.BrowserTarget

@HiltViewModel
class BrowserViewModel @Inject constructor(

) : ViewModel() {

    ///////////////////
    // browser state //
    ///////////////////
    private val _state: MutableStateFlow<BrowserState> = MutableStateFlow<BrowserState>(
        BrowserState())
    val state: StateFlow<BrowserState> = _state

    fun open(item: Item, target: BrowserTarget) {
        _state.update { it.copy(isOpen = true, item = item, target = target) }
    }

    fun close() {
        _state.update { it.copy(isOpen = false, item = null, target = null) }
    }

    fun changeState(
        isOpen: Boolean = _state.value.isOpen,
        item: Item? = _state.value.item,
        target: BrowserTarget? = _state.value.target,
        canGoBack: Boolean = _state.value.canGoBack,
        canGoForward: Boolean = _state.value.canGoForward,
        onImageClicked: (String) -> Unit = _state.value.onImageClicked,
    ){
        _state.update { it.copy(
            isOpen = isOpen,
            item = item,
            target = target,
            canGoBack = canGoBack,
            canGoForward = canGoForward,
            onImageClicked = onImageClicked
        ) }


    }


}