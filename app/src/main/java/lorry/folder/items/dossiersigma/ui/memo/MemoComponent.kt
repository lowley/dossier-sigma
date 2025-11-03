package lorry.folder.items.dossiersigma.ui.memo

import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.elixer.palette.Presets
import com.elixer.palette.composables.Palette
import com.elixer.palette.constraints.HorizontalAlignment
import com.elixer.palette.constraints.VerticalAlignment
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.basics.domain.Item
import lorry.folder.items.dossiersigma.headless.folderContentBack.IFolderContentBackComponent
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity.Companion.TAG

class MemoComponent @AssistedInject constructor(
    @Assisted val memoViewModel: MemoViewModel,
    private val folderContentComponent: IFolderContentBackComponent,
    @ApplicationContext private val context: Context
): IMemoComponent {

    @AssistedFactory
    interface Factory{
        fun create(viewModel: MemoViewModel): MemoComponent
    }

    override val isDisplayingMemo: StateFlow<Boolean> = memoViewModel._isDisplayingMemo

    override fun isDisplayed() = isDisplayingMemo.value

    override fun closeMemo() {
        memoViewModel._isDisplayingMemo.value = false
    }

    override fun toggleIsDisplayed() {
        memoViewModel.setIsDisplayingMemo(!isDisplayingMemo.value)
    }


    ////////////
    // zoneUI //
    ////////////
    @Composable
    context(BoxScope)
    override fun Render(
        selectedItem: Item?,
        setSelectedItem: (Item?) -> Unit) {

        val richTextState = rememberRichTextState()

        val isDisplayingMemo = this@MemoComponent.isDisplayingMemo.collectAsState()
        val isDisplayingPalette =
            memoViewModel.isDisplayingMemoPalette.collectAsState()

        if (isDisplayingMemo.value) {
            MemoEditor(
                modifier = Modifier.Companion
                    .align(Alignment.Companion.TopCenter),
                isRichText = isDisplayingMemo,
                richTextState = richTextState,
                closeMemo = this@MemoComponent::closeMemo,
                memoViewModel = memoViewModel,
                selectedItem = selectedItem,
                setSelectedItem = setSelectedItem,
                reloadCurrentFolder = folderContentComponent::reloadCurrentFolder,
                context = context
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
                            memoViewModel.setIsDisplayingMemoPalette(false)
                            val saved =
                                memoViewModel.savedSelectedRange.value
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

    companion object{
        fun getClipboardText(context: Context): String? {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = clipboard.primaryClip
            val text = clipData?.getItemAt(0)?.text?.toString()
            Log.d(TAG, "getClipboardText: $text")
            return text
        }

        @Composable
        public fun EditorAction(
            @DrawableRes iconRes: Int,
            active: Boolean,
            onClick: () -> Unit,
        ) {
            IconButton(onClick = onClick) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(id = iconRes),
                    tint = if (active) Color.White else Color.Black,
                    contentDescription = null
                )
            }
        }
    }
}