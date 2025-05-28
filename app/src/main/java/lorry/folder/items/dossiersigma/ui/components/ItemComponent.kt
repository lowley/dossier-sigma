package lorry.folder.items.dossiersigma.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
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
import me.saket.cascade.rememberCascadeState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer

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
    val imageHeight = 160.dp
    val imageSource = remember(item.fullPath) { mutableStateOf<Any?>(null) }


    LaunchedEffect(item.fullPath) {
        if (imageCache.containsKey(item.fullPath)) {
            imageSource.value =
                imageCache.getValue(item.fullPath)
            imageCache[item.fullPath] = imageSource.value
        } else
            imageSource.value = getImage(item, viewModel)
    }

    Column() {
        Box(
            modifier = Modifier//.background(Color.Blue)
                .width(imageHeight)
                .height(imageHeight),

            ) {
            var expanded by remember { mutableStateOf(false) }
            val scrollState = rememberScrollState()

            imageSource.value?.let { bitmap ->
                ImageSection(
                    modifier = Modifier//.background(Color.Yellow)
                        .align(Alignment.BottomCenter)
                        .width(imageHeight + 15.dp)
                        .height(imageHeight)
                        .clip(RoundedCornerShape(8.dp)) // ⬅️ déplacer ici
                        .border(1.dp, Color.Transparent, RoundedCornerShape(8.dp)),
                    imageSource = imageSource.value ?: R.drawable.file,
                    onTap = {
                        if (item.isFolder()) {
                            viewModel.goToFolder(item.fullPath, ITEMS_ORDERING_STRATEGY.DATE_DESC)
                        }
                        if (item.isFile() &&
                            (item.name.endsWith(".mp4") ||
                                    item.name.endsWith(".mkv") ||
                                    item.name.endsWith(".avi"))
                        ) {
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

            if (item is SigmaFolder) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 6.dp, bottom = 6.dp)
                        .graphicsLayer {
                            rotationZ = -20f
                        }
//                    .background(
//                        color = Color(0xAA000000),
//                        shape = RoundedCornerShape(4.dp)
//                    )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "DOSSIER",
                        fontSize = 10.sp,
                        color = Color.White,
                    )
                }
            }

            DropdownMenu(
                expanded = itemIdWithVisibleMenu.value == item.id,
                onDismissRequest = { itemIdWithVisibleMenu.value = "" },
                offset = with(density) {
                    DpOffset(imageOffset.x, (-imageHeight / 2))
                },
                containerColor = Color(0xFF111A2C),
                scrollState = scrollState
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item.name.substringBeforeLast("."),
                            color = Color(0xFFE57373),
                            fontSize = 10.sp,
                            lineHeight = 11.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = when (item) {
                                is SigmaFolder -> painterResource(R.drawable.dossier)
                                is SigmaFile -> when (item.name.substringAfterLast(".")) {
                                    "mp4" -> painterResource(R.drawable.mp4)
                                    "html" -> painterResource(R.drawable.html)
                                    else -> painterResource(R.drawable.fichier)
                                }

                                else -> painterResource(R.drawable.fichier)
                            },
                            tint = Color(0xFFE1AFAF),
                            contentDescription = null
                        )
                    },
                    onClick = {}
                )

                Divider(
                    modifier = Modifier.padding(vertical = 0.dp, horizontal = 5.dp),
                    thickness = 1.dp,
                    color = Color(0xFFB48E98)
                )

                DropdownMenuItem(
                    text = { Text("Adult Film Database", color = Color(0xFFB0BEC5)) },
                    leadingIcon = {
                        Icon(
                            modifier = Modifier
                                .size(24.dp),
                            tint = Color(0xFF90CAF9),
                            painter = painterResource(R.drawable.web_nb), contentDescription = null
                        )
                    },
                    onClick = {
                        viewModel.openBrowser(item, isGoogle = false)
                        itemIdWithVisibleMenu.value = ""
                    }
                )

                DropdownMenuItem(
                    text = { Text("Google", color = Color(0xFFB0BEC5)) },
                    leadingIcon = {
                        Icon(
                            modifier = Modifier
                                .size(24.dp),
                            tint = Color(0xFF90CAF9),
                            painter = painterResource(R.drawable.web_nb), contentDescription = null
                        )
                    },
                    onClick = {
                        viewModel.openBrowser(item, isGoogle = true)
                        itemIdWithVisibleMenu.value = ""
                    }
                )

                DropdownMenuItem(
                    text = { Text("Clipboard -> icône", color = Color(0xFFB0BEC5)) },
                    leadingIcon = {
                        Icon(
                            modifier = Modifier
                                .size(24.dp),
                            tint = Color(0xFF90CAF9),
                            painter = painterResource(R.drawable.presse_papiers),
                            contentDescription = null
                        )
                    },
                    onClick = {
                        viewModel.setPicture(item, true)
                        itemIdWithVisibleMenu.value = ""
                    }
                )
                LaunchedEffect(expanded) {
                    if (expanded) {
                        // Scroll to show the bottom menu items.
                        scrollState.scrollTo(scrollState.maxValue)
                    }
                }
            }

//        CascadeDropdownMenu(
//            state = state,
//            modifier = Modifier,
//            expanded = itemIdWithVisibleMenu.value == item.id,
//            onDismissRequest = { itemIdWithVisibleMenu.value = "" },
//            offset = with(density) {
//                DpOffset(imageOffset.x, (-imageHeight / 2))
//            }) {
//
//            DropdownMenuHeader(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .align(CenterHorizontally)
//            ) {
//                Text(
//                    text = when (item.isFile()) {
//                        true -> "Fichier ${item.name.substringAfterLast(".")}"
//                        false -> "Dossier"
//                    },
//                    modifier = Modifier,
//                    fontSize = 18.sp
//                )
//            }
//        }

            
        }

        TextSection(
            modifier = Modifier
                .height(35.dp)
                .align(Alignment.CenterHorizontally),
            name = item.name
        )
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
        contentScale = if (imageSource is Int) ContentScale.FillBounds else ContentScale.FillWidth,
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