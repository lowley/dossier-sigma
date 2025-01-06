package lorry.folder.items.dossiersigma.ui.components

import android.R.attr.onClick
import android.content.Context

import android.provider.CalendarContract
import android.view.Gravity
import android.view.MenuItem
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.twotone.ArrowForward
import androidx.compose.material.icons.twotone.ArrowForward
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.R
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.NonCancellable.children
import lorry.folder.items.dossiersigma.ui.SigmaViewModel
import me.saket.cascade.CascadeDropdownMenu
import me.saket.cascade.rememberCascadeState
import kotlin.math.roundToInt

@Composable
public fun ItemComponent(context: Context, viewModel: SigmaViewModel, item: Item) {
    var isMenuVisible by rememberSaveable { mutableStateOf(false) }
    val state = rememberCascadeState()
    var imageOffset by remember { mutableStateOf(DpOffset.Zero) }
    val density = LocalDensity.current
    val imageHeight = 120.dp
    
    Column(
        modifier = Modifier
            .width(imageHeight)
            .height(165.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(imageHeight)
            
        ) {
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(1f)
                    .pointerInput(true) {
                        detectTapGestures(
                            onLongPress = {
                                imageOffset = DpOffset(it.x.toDp(), it.y.toDp())
                                isMenuVisible = true
                            }
                        )
                    },
                painter = if (item.isFile) painterResource(R.drawable.file)
                else painterResource(R.drawable.folder),
                contentDescription = null
            )

            CascadeDropdownMenu(
                state = state,
                modifier = Modifier,
                expanded = isMenuVisible,
                onDismissRequest = { isMenuVisible = false },
                offset = with(density) {
                    DpOffset(imageOffset.x, (-imageHeight / 2))
                }) {

                DropdownMenuHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                ) { Text(
                    text = "Un item",
                    modifier = Modifier,
                    fontSize = 18.sp
                ) }

                androidx.compose.material3.DropdownMenuItem(
                    text = { Text("Clipboard -> icône") },
                    leadingIcon = { Icons.AutoMirrored.Sharp.KeyboardArrowRight },
                    onClick = {}
                )
            }
        }
        
        Text(
            modifier = Modifier
                .fillMaxHeight()
                .align(alignment = CenterHorizontally),
            softWrap = true,
            lineHeight = 13.sp,
            maxLines = 3,
            fontSize = 12.sp,
            color = Color.Black,
            text = item.name
        )
    }
}