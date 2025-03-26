package lorry.folder.items.dossiersigma.ui.components

import android.content.Context
import android.graphics.Bitmap

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.KeyboardArrowRight
import androidx.compose.material.icons.sharp.AccountBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.R
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import lorry.folder.items.dossiersigma.domain.SigmaFile
import lorry.folder.items.dossiersigma.domain.SigmaFolder
import lorry.folder.items.dossiersigma.ui.SigmaViewModel
import me.saket.cascade.CascadeDropdownMenu
import me.saket.cascade.rememberCascadeState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign

@Composable
fun ItemComponent(modifier: Modifier, context: Context, viewModel: SigmaViewModel, item: Item) {
    var isMenuVisible by rememberSaveable { mutableStateOf(false) }
    val state = rememberCascadeState()
    var imageOffset by remember { mutableStateOf(DpOffset.Zero) }
    val density = LocalDensity.current
    val imageHeight = 150.dp
    val imageSource = remember(item) { getImage(context, item, viewModel) }

    Column(
        modifier = modifier
            .width(imageHeight)
            .height(imageHeight + 35.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier
                .width(imageHeight - 5.dp)
                .height(imageHeight - 20.dp)
            
        ) {
            ImageSection(
                imageSource = imageSource,
                onTap = {
                    if (item.isFolder()) {
                        //iewModel.goToFolderSafely(item.fullPath)
                        viewModel.goToFolder(item.fullPath)
                    }
                },
                onLongPress = { offset ->
                    imageOffset = DpOffset(offset.x.toInt().dp, offset.y.toInt().dp)
                    isMenuVisible = true
                })
        }


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
            ) {
                Text(
                    text = "Un item",
                    modifier = Modifier,
                    fontSize = 18.sp
                )
            }

            androidx.compose.material3.DropdownMenuItem(
                text = { Text("Ouvrir navigateur") },
                leadingIcon = { Icons.Sharp.AccountBox },
                onClick = {
                    viewModel.openBrowser(item)
                    isMenuVisible = false
                }
            )
            
            androidx.compose.material3.DropdownMenuItem(
                text = { Text("Clipboard -> icône") },
                leadingIcon = { Icons.AutoMirrored.Sharp.KeyboardArrowRight },
                onClick = {
                    viewModel.setPictureWithClipboard(item)
                    isMenuVisible = false
                }
            )
        }
        TextSection(item.name)
    }
}


fun getImage(
    context: Context,
    item: Item,
    viewModel: SigmaViewModel
): Any { // Retourne une valeur compatible avec Coil
    return when {
        item.picture != null -> item.picture // Utilise l'image en mémoire si disponible
        item is SigmaFile -> R.drawable.file // Icône de fichier
        item is SigmaFolder -> {
            val isPopulated = viewModel.changingPictureUseCase.isFolderPopulated(item)
            if (isPopulated) R.drawable.folder_full else R.drawable.folder_empty
        }

        else -> R.drawable.file // Valeur par défaut
    }
}

fun getImageBitmapFromDrawable(
    context: Context,
    drawable: Int
): ImageBitmap {
    return (ContextCompat.getDrawable(context, drawable)?.toBitmap()?.asImageBitmap()
        ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).asImageBitmap())


}

@Composable
fun TextSection(name: String) {
    Text(
        text = name,
        modifier = Modifier
            .fillMaxHeight()
            .padding(top = 5.dp),
        softWrap = true,
        textAlign = TextAlign.Center,
        lineHeight = 13.sp,
        maxLines = 3,
        fontSize = 12.sp,
        color = Color(0xFFDBBC00),
    )
}

@Composable
fun ImageSection(
    imageSource: Any,
    onTap: () -> Unit,
    onLongPress: (Offset) -> Unit
) {
    val imagePainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageSource) // Cela peut être une URL ou une ressource locale
            .crossfade(true) // Optionnel : transition fluide
            .build()
    )

    Image(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .pointerInput(true) {
                detectTapGestures(
                    onTap = { onTap() },
                    onLongPress = { onLongPress(it) }
                )
            },
        painter = imagePainter,
        contentDescription = null
    )
}