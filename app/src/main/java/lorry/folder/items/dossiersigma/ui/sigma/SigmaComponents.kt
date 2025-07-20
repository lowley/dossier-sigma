package lorry.folder.items.dossiersigma.ui.sigma

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import lorry.folder.items.dossiersigma.R

@Composable
context(RowScope)
fun HomeButtonIcon(
    icon : Int,
    onTapAction: (Offset) -> Unit
){
    Icon(
        modifier = Modifier
            .size(50.dp)
            .padding(
                start = 15.dp,
                end = 5.dp
            )
            .align(Alignment.CenterVertically)
            .size(50.dp)
            .pointerInput(true) {
                detectTapGestures(
                    onTap = {
                        onTapAction(it)
                    }
                )
            },
        painter = painterResource(icon),
        tint = Color(0xFFe9c46a),
        contentDescription = null
    )
}