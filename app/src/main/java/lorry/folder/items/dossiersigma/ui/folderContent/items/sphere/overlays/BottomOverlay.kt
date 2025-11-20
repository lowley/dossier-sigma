package lorry.folder.items.dossiersigma.ui.folderContent.items.sphere.overlays

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

context(BoxScope)
@Composable
fun BottomOverlay(
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier
            .fillMaxSize()
            .align(Alignment.Center),
        text = "Bottom Overlay"
    )
}