package lorry.folder.items.dossiersigma.ui.centralArea

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
import lorry.folder.items.dossiersigma.ui.memoEditor.MemoEditor
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity

@Composable
context(SigmaActivity, BoxScope)
fun Memo() {

    val richTextState = rememberRichTextState()

    val isRichText = mainViewModel.isDisplayingMemo.collectAsState()
    val isDisplayingPalette =
        mainViewModel.isDisplayingMemoPalette.collectAsState()

    if (isRichText.value) {
        MemoEditor(
            modifier = Modifier
                .align(Alignment.TopCenter),
            isRichText = isRichText,
            richTextState = richTextState
        )
    }

    if (isRichText.value && isDisplayingPalette.value) {
        val keyboardController = LocalSoftwareKeyboardController.current
        keyboardController?.hide()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(25f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Palette(
                    defaultColor = Color.Magenta,
                    buttonSize = 100.dp,
                    swatches = Presets.material(),
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