package lorry.folder.items.dossiersigma.UI.memo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.elixer.palette.Presets
import com.elixer.palette.composables.Palette
import com.elixer.palette.constraints.HorizontalAlignment
import com.elixer.palette.constraints.VerticalAlignment
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.UI.sigma.SigmaActivity
import javax.inject.Inject

class MemoComponent @Inject constructor(): IMemoComponent {

    //////////////////////
    // isDisplayingMemo //
    //////////////////////
    val _isDisplayingMemo = MutableStateFlow(false)
    override val isDisplayingMemo: StateFlow<Boolean> = _isDisplayingMemo

    fun setIsDisplayingMemo(isVisible: Boolean) {
        _isDisplayingMemo.value = isVisible
    }

    override fun isDisplayed() = isDisplayingMemo.value

    override fun closeMemo() {
        _isDisplayingMemo.value = false
    }

    override fun toggleIsDisplayed() {
        setIsDisplayingMemo(!isDisplayingMemo.value)
    }


    ////////////
    // zoneUI //
    ////////////
    @Composable
    context(SigmaActivity, BoxScope)
    override fun Render(){

        val richTextState = rememberRichTextState()

        val isDisplayingMemo = this@MemoComponent.isDisplayingMemo.collectAsState()
        val isDisplayingPalette =
            mainViewModel.isDisplayingMemoPalette.collectAsState()

        if (isDisplayingMemo.value) {
            MemoEditor(
                modifier = Modifier.Companion
                    .align(Alignment.Companion.TopCenter),
                isRichText = isDisplayingMemo,
                richTextState = richTextState,
                closeMemo = this@MemoComponent::closeMemo
            )
        }

        if (isDisplayingMemo.value && isDisplayingPalette.value) {
            val keyboardController = LocalSoftwareKeyboardController.current
            keyboardController?.hide()

            Column(
                modifier = Modifier.Companion
                    .fillMaxSize()
                    .zIndex(25f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Companion.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Companion.CenterVertically
                ) {
                    Palette(
                        defaultColor = Color.Companion.Magenta,
                        buttonSize = 100.dp,
                        swatches = Presets.Companion.material(),
                        innerRadius = 400f,
                        strokeWidth = 120f,
                        spacerRotation = 5f,
                        spacerOutward = 2f,
                        verticalAlignment = VerticalAlignment.Middle,
                        horizontalAlignment = HorizontalAlignment.Center,
                        onColorSelected = { color ->
                            mainViewModel.setIsDisplayingMemoPalette(false)
                            val saved =
                                mainViewModel.savedSelectedRange.value
                                    ?: return@Palette
                            richTextState.selection = saved
                            richTextState.addSpanStyle(
                                SpanStyle(
                                    color = color
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}