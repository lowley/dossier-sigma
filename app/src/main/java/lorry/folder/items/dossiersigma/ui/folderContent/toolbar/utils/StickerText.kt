package lorry.folder.items.dossiersigma.ui.folderContent.toolbar.utils

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.Tool
import lorry.folder.items.dossiersigma.ui.sigma.SigmaColors

@Composable
context(BoxScope)
fun StickerText(
    tool: Tool
) {
    Text(
        modifier = Modifier.Companion
            .align(Alignment.Companion.BottomCenter),
        text = tool.text(),
        color = SigmaColors.current.onPrimary,
        fontSize = 12.sp
    )
}