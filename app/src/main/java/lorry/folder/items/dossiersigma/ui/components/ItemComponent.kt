package lorry.folder.items.dossiersigma.ui.components

import android.content.Context
import android.graphics.Bitmap

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.KeyboardArrowRight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.R
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import lorry.folder.items.dossiersigma.ui.SigmaViewModel
import me.saket.cascade.CascadeDropdownMenu
import me.saket.cascade.rememberCascadeState

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
                bitmap = getBitmap(
                    context = context,
                    item = item
                ),
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
                        .align(CenterHorizontally)
                ) { Text(
                    text = "Un item",
                    modifier = Modifier,
                    fontSize = 18.sp
                ) }

                androidx.compose.material3.DropdownMenuItem(
                    text = { Text("Clipboard -> icône") },
                    leadingIcon = { Icons.AutoMirrored.Sharp.KeyboardArrowRight },
                    onClick = { 
                        viewModel.setPictureWithClipboard(item)
                        isMenuVisible = false
                    }
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

fun getBitmap(
    context: Context,
    item: Item
) : ImageBitmap{
    
    if (item.picture != null) return item.picture.asImageBitmap()
    
    if (item.isFile) 
            return (ContextCompat.getDrawable(context, R.drawable.file)?.toBitmap()?.asImageBitmap() ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).asImageBitmap())
    
    
    
    (ContextCompat.getDrawable(context, R.drawable.folder)?.toBitmap()?.asImageBitmap() ?:
    Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).asImageBitmap())
}