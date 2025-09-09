package lorry.folder.items.dossiersigma.ui.memo

import androidx.compose.ui.text.TextRange
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MemoViewModel @Inject constructor() : ViewModel() {

    //////////////////////
    // isDisplayingMemo //
    //////////////////////
    val _isDisplayingMemo = MutableStateFlow(false)

    fun setIsDisplayingMemo(isVisible: Boolean) {
        _isDisplayingMemo.value = isVisible
    }

    ///////////////////////
    // affichage palette //
    ///////////////////////
    private val _isDisplayingMemoPalette = MutableStateFlow(false)
    val isDisplayingMemoPalette: StateFlow<Boolean> = _isDisplayingMemoPalette

    fun setIsDisplayingMemoPalette(isVisible: Boolean) {
        _isDisplayingMemoPalette.value = isVisible
    }

    ///////////////////////
    // gestion sélection //
    ///////////////////////
    private val _savedSelectedRange = MutableStateFlow<TextRange?>(null)
    val savedSelectedRange: StateFlow<TextRange?> = _savedSelectedRange

    fun setSavedSelectedRange(newSelection: TextRange?) {
        _savedSelectedRange.value = newSelection
    }
}