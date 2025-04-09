package lorry.folder.items.dossiersigma.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.KeyboardArrowRight
import androidx.compose.material.icons.sharp.AccountBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.domain.SigmaFile
import lorry.folder.items.dossiersigma.domain.SigmaFolder
import lorry.folder.items.dossiersigma.ui.ITEMS_ORDERING_STRATEGY
import lorry.folder.items.dossiersigma.ui.SigmaViewModel
import me.saket.cascade.CascadeDropdownMenu
import me.saket.cascade.rememberCascadeState

@Composable
fun ItemComponent(
    modifier: Modifier,
    viewModel: SigmaViewModel,
    item: Item,
    imageCache: MutableMap<String, Any?>,
    itemIdWithVisibleMenu: MutableState<String>
) {
    val state = rememberCascadeState()
    var imageOffset by remember { mutableStateOf(DpOffset.Zero) }
    val density = LocalDensity.current
    val imageHeight = 130.dp
    val imageSource = remember(item.fullPath) { mutableStateOf<Any?>(null) }
    

    LaunchedEffect(item.fullPath) {
        if (imageCache.containsKey(item.fullPath)) {
            imageSource.value =
                imageCache.getValue(item.fullPath)
            imageCache[item.fullPath] = imageSource.value
        } else
            imageSource.value = getImage(item, viewModel)
    }

    Box(
        modifier = modifier//.background(Color.Blue)
            .width(imageHeight)
            .height(imageHeight + 35.dp),
        
    ) {
        imageSource.value?.let { bitmap ->
            ImageSection(
                modifier = Modifier//.background(Color.Yellow)
                    .align(Alignment.TopCenter)
                    .width(imageHeight + 15.dp)
                    .height(imageHeight)
                    .clip(RoundedCornerShape(8.dp)) // ⬅️ déplacer ici
                    .border(1.dp, Color.Transparent, RoundedCornerShape(8.dp)),
                imageSource = imageSource.value!!,
                onTap = {
                    if (item.isFolder()) {
                        viewModel.goToFolder(item.fullPath, ITEMS_ORDERING_STRATEGY.DATE_DESC)
                    }
                    if (item.isFile() && item.name.endsWith(".mp4")) {
                        viewModel.playMP4File(item.fullPath)
                    }
                    if (item.isFile() && item.name.endsWith(".html")) {
                        viewModel.playHtmlFile(item.fullPath)
                    }
                },
                onLongPress = { offset ->
                    imageOffset = DpOffset(offset.x.toInt().dp, offset.y.toInt().dp)
                    itemIdWithVisibleMenu.value = item.id
                })
        }

        CascadeDropdownMenu(
            state = state,
            modifier = Modifier,
            expanded = itemIdWithVisibleMenu.value == item.id,
            onDismissRequest = { itemIdWithVisibleMenu.value = "" },
            offset = with(density) {
                DpOffset(imageOffset.x, (-imageHeight / 2))
            }) {

            DropdownMenuHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(CenterHorizontally)
            ) {
                Text(
                    text = when (item.isFile()) {
                        true -> "Fichier ${item.name.substringAfterLast(".")}"
                        false -> "Dossier"
                    },
                    modifier = Modifier,
                    fontSize = 18.sp
                )
            }

            androidx.compose.material3.DropdownMenuItem(
                text = { Text("Ouvrir navigateur") },
                leadingIcon = { Icons.Sharp.AccountBox },
                onClick = {
                    viewModel.openBrowser(item)
                    itemIdWithVisibleMenu.value = ""
                }
            )

            androidx.compose.material3.DropdownMenuItem(
                text = { Text("Clipboard -> icône") },
                leadingIcon = { Icons.AutoMirrored.Sharp.KeyboardArrowRight },
                onClick = {
                    viewModel.setPicture(item, true)
                    itemIdWithVisibleMenu.value = ""
                }
            )
        }
        TextSection(
            modifier = Modifier
                //.background(Color.Cyan),
                .align(Alignment.BottomCenter),
            name =  item.name)
        
    }
}

suspend fun getImage(
    item: Item,
    viewModel: SigmaViewModel
): Any { // Retourne une valeur compatible avec Coil
    return withContext(Dispatchers.Default) {
        var result: Any? = null
        result = when {
            item.picture != null -> item.picture // Utilise l'image en mémoire si disponible
            item is SigmaFile -> R.drawable.file // Icône de fichier
            item is SigmaFolder -> {
                var hasPictureFile = viewModel.diskRepository.hasPictureFile(item)

                if (!hasPictureFile) {
                    val isPopulated = viewModel.changingPictureUseCase.isFolderPopulated(item)
                    if (isPopulated) R.drawable.folder_full else R.drawable.folder_empty
                } else {
                    //une image est présente pour ce répertoire
                    try {
                        var picture: Bitmap? = null
                        picture =
                            viewModel.base64DataSource.extractImageFromHtml("${item.fullPath}/.folderPicture.html")
                        if (picture != null) {
                            item.copy(picture = picture)
                            picture
                        } else R.drawable.file
                    } catch (e: Exception) {
                        println("Erreur lors de la lecture de html pour le répertoire ${item.name}, ${e.message}")
                    }
                }
            }

            else -> R.drawable.file // Valeur par défaut
        }

        return@withContext result
    }
}

@Composable
fun TextSection(name: String, modifier: Modifier) {
    Text(
        text = name,
        modifier = modifier
//            .fillMaxHeight()
            .height(35.dp)
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
    modifier: Modifier,
    imageSource: Any,
    onTap: () -> Unit,
    onLongPress: (Offset) -> Unit
) {
    val imagePainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageSource) // Cela peut être une URL ou une ressource locale
            //.crossfade(true) // Optionnel : transition fluide
            .build()
    )

    AsyncImage(
        model = imageSource,
        contentDescription = "Miniature",
        contentScale = if (imageSource is Int) ContentScale.Fit else ContentScale.FillWidth,
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(true) {
                detectTapGestures(
                    onTap = { onTap() },
                    onLongPress = { onLongPress(it) }
                )
            },
    )
}