package lorry.folder.items.dossiersigma.ui.memoEditor

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSizeDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.viewModelScope
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.data.dataSaver.CompositeManager
import lorry.folder.items.dossiersigma.data.dataSaver.Memo
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity
import kotlin.random.Random

@Composable
fun SigmaActivity.MemoEditor(
    modifier: Modifier = Modifier,
    isRichText: State<Boolean>,
    richTextState: RichTextState,
) {
    val currentItemFlow = mainViewModel.selectedItem

    Column(
        modifier = modifier
            .width(500.dp)
            .height(400.dp)
            .zIndex(15f)
    ) {
        val selectedItemMemo by combine(
            currentItemFlow,
            mainViewModel.memoCache
        ) { item, cache ->
            if (item != null)
                cache[item.fullPath]
            else ""
        }.collectAsState(initial = "")

        LaunchedEffect(isRichText.value, selectedItemMemo) {
            if (isRichText.value) {
                richTextState.setHtml(selectedItemMemo ?: "")
                richTextState.selection = TextRange.Zero
            }
        }

        RichTextEditor(
            state = richTextState,
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 8.dp,
                        topEnd = 8.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    )
                )
                .background(Color.White)
                .height(300.dp)
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .border(
                    width = 1.dp,
                    color = Color.DarkGray,
                    shape = RoundedCornerShape(
                        topStart = 8.dp,
                        topEnd = 8.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    )
                ),
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(Color.DarkGray)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                EditorAction(
                    iconRes = R.drawable.bold,
                    active = true
                ) {
                    // Toggle a span style .
                    richTextState.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
                }
                EditorAction(
                    iconRes = R.drawable.underline,
                    active = true
                ) {
                    richTextState.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                }
                EditorAction(
                    iconRes = R.drawable.italic,
                    active = true
                ) {
                    richTextState.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
                }
                EditorAction(
                    iconRes = R.drawable.strikethrough,
                    active = true
                ) {
                    richTextState.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
                }
                EditorAction(
                    iconRes = R.drawable.leftalign,
                    active = true
                ) {
                    // Toggle a paragraph style.
                    richTextState.toggleParagraphStyle(
                        ParagraphStyle(
                            textAlign = TextAlign.Start
                        )
                    )
                }
                EditorAction(
                    iconRes = R.drawable.centeralign,
                    active = true
                ) {
                    // Toggle a paragraph style.
                    richTextState.toggleParagraphStyle(
                        ParagraphStyle(
                            textAlign = TextAlign.Center
                        )
                    )
                }
                EditorAction(
                    iconRes = R.drawable.rightalign,
                    active = true
                ) {
                    // Toggle a paragraph style.
                    richTextState.toggleParagraphStyle(
                        ParagraphStyle(
                            textAlign = TextAlign.End
                        )
                    )
                }
                EditorAction(
                    iconRes = R.drawable.textsize,
                    active = true
                ) {
                    val currentSpanStyle = richTextState.currentSpanStyle
                    val currentFontSize = currentSpanStyle.fontSize
                    val defaultFontSize = 16.sp

//                                            val newFontSize = baseSize + 1.sp

                    richTextState.toggleSpanStyle(
                        spanStyle = SpanStyle(fontSize = TextAutoSizeDefaults.MaxFontSize)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(Color.DarkGray)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = {
                    val onlyBr = Regex("^<br>$")
                    var editorContent = richTextState.toHtml()
                        .replace(onlyBr, "")

                    val nothingImportant = Regex("^\\s*$")
                    val nothingImportant2 =
                        Regex("^(<br>|\\s|<p>|</p>|&Tab;)*$")

                    if (editorContent.matches(nothingImportant)
                        || editorContent.matches((nothingImportant2))
                    )
                        editorContent = ""

                    val currentItem =
                        mainViewModel.selectedItem.value
                            ?: return@IconButton

                    mainViewModel.setSelectedItem(
                        currentItem.copy(memo = editorContent)
                    )

                    mainViewModel.setMemoCacheValue(
                        key = currentItem.fullPath,
                        memo = editorContent
                    )

                    mainViewModel.viewModelScope.launch(Dispatchers.IO) {
                        val compositeMgr =
                            CompositeManager(currentItem.fullPath ?: "")
                        compositeMgr.save(Memo(editorContent))
                        withContext(Dispatchers.Default) {
                            mainViewModel.setSelectedItem(null)
                        }
                    }

                    richTextState.clear()
                    mainViewModel.setIsDisplayingMemo(false)
                }) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(id = R.drawable.enregistrer),
                        tint = Color(0xFFd1495b),
                        contentDescription = null
                    )
                }

                EditorAction(
                    iconRes = R.drawable.palette,
                    active = true
                ) {
                    mainViewModel.setSavedSelectedRange(richTextState.selection)
                    richTextState.selection = TextRange(
                        start = richTextState.selection.start,
                        end = richTextState.selection.start
                    )
//                    val savedStart = mainViewModel.savedSelectedRange.value!!.start
//                    val saveFirstCharacter =  richTextState.tex
//                    richTextState.removeTextRange(TextRange(
//                        start = savedStart,
//                        end = savedStart + 1
//                    ))
//                    richTextState.addTextAfterSelection(" ")

                    mainViewModel.setIsRichTextFocused(false)
                    mainViewModel.setIsDisplayingMemoPalette(true)
                }

                EditorAction(
                    iconRes = R.drawable.paste,
                    active = true
                ) {
                    val clipboardContent = mainViewModel.getClipboardText(sigmaActivity)
                    if (clipboardContent == null)
                        return@EditorAction

                    richTextState.setHtml(clipboardContent)
                }

                EditorAction(R.drawable.clear, active = true) {
                                            richTextState.clear()
                }

                IconButton(onClick = {
                    richTextState.clear()
                    mainViewModel.setIsDisplayingMemo(false)
//
//                                            val item = mainViewModel.selectedItem.value
//                                            if (item == null)
//                                                return@IconButton
//
//                                            item?.memo = snapshot
//                                            mainViewModel.setMemoCacheValue(item?.fullPath ?: "", snapshot)
//
//                                            mainViewModel.viewModelScope.launch(Dispatchers.IO) {
//                                            val compositeMgr = CompositeManager(currentItem.value?.fullPath ?: "")
//                                            compositeMgr.save(Memo(snapshot))
//                                                withContext(Dispatchers.Default) {
//                                                    mainViewModel.setSelectedItem(null)
//                                                    mainViewModel.refreshCurrentFolder()
//                                                }
//                                            }
                }) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(id = R.drawable.exit),
                        tint = Color(0xFFd1495b),
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Composable
public fun SigmaActivity.EditorAction(
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

fun Int.hueToColor(saturation: Float = 1f, value: Float = 0.5f): Color = Color(
    ColorUtils.HSLToColor(floatArrayOf(this.toFloat(), saturation, value))
)