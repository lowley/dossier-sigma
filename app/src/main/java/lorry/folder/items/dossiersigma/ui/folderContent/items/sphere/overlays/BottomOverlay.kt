package lorry.folder.items.dossiersigma.ui.folderContent.items.sphere.overlays

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lorry.folder.items.dossiersigma.ui.sigma.SigmaColors

context(ColumnScope)
@Composable
fun BottomOverlay(
    modifier: Modifier = Modifier,
    name: String
): IOverlayContent = object : IOverlayContent {

    context(BoxScope)
    @Composable
    override fun display(modifier: Modifier, name: String) {
        val shortcuts = name
            .substringBeforeLast(".")
            .substringAfter(".")
            .split(".")

        if (shortcuts.size != 1
            || shortcuts[0] == name
        )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.5f)) // <-- voile assombrissant
            )
            {

                Column(
                    modifier = Modifier.Companion
                        .matchParentSize()
                        .padding(top = 45.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    for (shortcut in shortcuts) {
                        Text(
                            text = shortcut,
                            color = SigmaColors.current.onPrimary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
    }
}