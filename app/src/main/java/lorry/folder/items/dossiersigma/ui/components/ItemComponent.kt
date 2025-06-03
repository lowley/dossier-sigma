package lorry.folder.items.dossiersigma.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.domain.SigmaFile
import lorry.folder.items.dossiersigma.domain.SigmaFolder
import lorry.folder.items.dossiersigma.domain.usecases.browser.BrowserTarget
import lorry.folder.items.dossiersigma.ui.ITEMS_ORDERING_STRATEGY
import lorry.folder.items.dossiersigma.ui.SigmaViewModel
import me.saket.cascade.rememberCascadeState
import java.io.File

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
    val pictureUpdateId by viewModel.pictureUpdateId.collectAsState()
    var contentScale by remember { mutableStateOf(ContentScale.Crop) }
    
    LaunchedEffect(item.fullPath, pictureUpdateId) {
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
                key(pictureUpdateId) {
                    ImageSection(
                        modifier = Modifier//.background(Color.Yellow)
                            .align(Alignment.BottomCenter)
                            .size(imageHeight + 15.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Transparent, RoundedCornerShape(8.dp)),
                        imageSource = imageSource.value ?: R.drawable.file,
                        contentScale = contentScale,
                        onTap = {
                            if (item.isFolder()) {
                                viewModel.goToFolder(
                                    item.fullPath,
                                    ITEMS_ORDERING_STRATEGY.DATE_DESC
                                )
                            }
                            if (item.isFile() &&
                                (item.name.endsWith(".mp4") ||
                                        item.name.endsWith(".mkv") ||
                                        item.name.endsWith(".mpg") ||
                                        item.name.endsWith(".avi"))
                            ) {
                                viewModel.playVideoFile(item.fullPath)
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

            }

            if (item is SigmaFolder) {

//                val fileCount = 15
//                val folderCount = 3

                val fileCount =
                    viewModel.diskRepository.countFilesAndFolders(File(item.fullPath)).component1()
                val folderCount =
                    viewModel.diskRepository.countFilesAndFolders(File(item.fullPath)).component2()

                val boxWidth = 30.dp

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .graphicsLayer {
                            shape = RoundedCornerShape(
                                topStart = 8.dp,
                                bottomEnd = 8.dp
                            )
                            clip = true
                            shadowElevation = 0f
                        }
                        .background(Color.Gray) // Rouge
                        .widthIn(min = boxWidth)
                    //.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 0.dp, top = 0.dp)
                            .width(boxWidth)
                    ) {
                        val textHeight = 20.dp

                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(0.dp)
                                .height(textHeight),
                            text = "$fileCount",
                            fontSize = 10.sp,
                            color = Color.White
                        )
                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(
                                    top = 0.dp, start = 0.dp, bottom = 4.dp, end = 0.dp
                                )
                                .height(textHeight),
                            text = "$folderCount",
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 6.dp, bottom = 20.dp)
                        .graphicsLayer {
                            rotationZ = -15f
                            shadowElevation = 4f
                            shape = RoundedCornerShape(4.dp)
                            clip = true
                        }
                        .background(
                            color = Color(0xFFD32F2F), // rouge tampons administratifs
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "DOSSIER",
                        fontSize = 11.sp,
                        color = Color.White,
                        letterSpacing = 1.sp,
                        lineHeight = 12.sp
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
                                    "mp4", "mpg", "avi", "mkv" -> painterResource(R.drawable.camera)
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
                    text = { Text("IAFD Film", color = Color(0xFFB0BEC5)) },
                    leadingIcon = {
                        Icon(
                            modifier = Modifier
                                .size(24.dp),
                            tint = Color(0xFF90CAF9),
                            painter = painterResource(R.drawable.loupe), contentDescription = null
                        )
                    },
                    onClick = {
                        viewModel.setSelectedItem(item)
                        viewModel.browserManager.openBrowser(item, BrowserTarget.IAFD_MOVIE)
                        itemIdWithVisibleMenu.value = ""
                    }
                )

                DropdownMenuItem(
                    text = { Text("IAFD Personne", color = Color(0xFFB0BEC5)) },
                    leadingIcon = {
                        Icon(
                            modifier = Modifier
                                .size(24.dp),
                            tint = Color(0xFF90CAF9),
                            painter = painterResource(R.drawable.loupe), contentDescription = null
                        )
                    },
                    onClick = {
                        viewModel.setSelectedItem(item)
                        viewModel.browserManager.openBrowser(item, BrowserTarget.IAFD_PERSON)
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
                        viewModel.setSelectedItem(item)
                        viewModel.browserManager.openBrowser(item, BrowserTarget.GOOGLE)
                        itemIdWithVisibleMenu.value = ""
                    }
                )

                DropdownMenuItem(
                    text = { Text("Adapter image", color = Color(0xFFB0BEC5)) },
                    leadingIcon = {
                        Icon(
                            modifier = Modifier
                                .size(24.dp),
                            tint = Color(0xFF90CAF9),
                            painter = painterResource(R.drawable.la_droite), contentDescription = null
                        )
                    },
                    onClick = {
                        contentScale = when(contentScale){
                            ContentScale.Crop -> ContentScale.Fit
                            ContentScale.Fit -> ContentScale.FillBounds
                            ContentScale.FillBounds -> ContentScale.FillWidth
                            ContentScale.FillWidth -> ContentScale.FillHeight
                            ContentScale.FillHeight -> ContentScale.Inside
                            ContentScale.Inside -> ContentScale.None
                            ContentScale.None -> ContentScale.Crop
                            else -> ContentScale.Crop
                        }
                    }
                )

//                DropdownMenuItem(
//                    text = { Text("Clipboard -> icône", color = Color(0xFFB0BEC5)) },
//                    leadingIcon = {
//                        Icon(
//                            modifier = Modifier
//                                .size(24.dp),
//                            tint = Color(0xFF90CAF9),
//                            painter = painterResource(R.drawable.presse_papiers),
//                            contentDescription = null
//                        )
//                    },
//                    onClick = {
//                        viewModel.setPicture(item, true)
//                        itemIdWithVisibleMenu.value = ""
//                    }
//                )
                
                LaunchedEffect(expanded) {
                    if (expanded) {
                        // Scroll to show the bottom menu items.
                        scrollState.scrollTo(scrollState.maxValue)
                    }
                }
            }
        }

        TextSection(
            modifier = Modifier
                .height(52.dp)
                .align(Alignment.CenterHorizontally),
            name = item.name
        )
    }
}

suspend fun getImage(
    item: Item,
    viewModel: SigmaViewModel
): Any { // Retourne une valeur compatible avec Coil

    var result: Any? = null
    result = when {
        item.picture != null -> item.picture // Utilise l'image en mémoire si disponible
        item is SigmaFile -> {
            if (item.fullPath.endsWith("mp4") ||
                item.fullPath.endsWith("mkv") ||
                item.fullPath.endsWith("avi") ||
                item.fullPath.endsWith("mpg")
            ) {

                val image64 = viewModel.base64Embedder.extractBase64FromMp4(File(item.fullPath))

                if (image64 == null)
                    R.drawable.file
                else {
                    val picture = viewModel.base64Embedder.base64ToBitmap(image64)

                    //item.copy(picture = picture)
                    picture ?: R.drawable.file
                }
            } else R.drawable.file
        }

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

    return result
}

@Composable
fun TextSection(name: String, modifier: Modifier) {
    Text(
        text = name,
        modifier = modifier
//            .fillMaxHeight()
            .height(52.dp)
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
    onLongPress: (Offset) -> Unit,
    contentScale: ContentScale
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
    ) {
        // 🎨 Trame de fond inclinée
        Canvas(modifier = Modifier.matchParentSize()) {
            val spacing = 12.dp.toPx()
            val strokeWidth = 1.dp.toPx()
            val color = Color(0xFF456990)

            for (i in -size.height.toInt()..size.width.toInt() step spacing.toInt()) {
                drawLine(
                    color = color,
                    start = Offset(i.toFloat(), 0f),
                    end = Offset(i + size.height, size.height),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = color,
                    start = Offset(i.toFloat(), size.height),
                    end = Offset(i + size.height, 0f),
                    strokeWidth = strokeWidth
                )
            }
        }

        AsyncImage(
            model = imageSource,
            contentDescription = "Miniature",
            contentScale = contentScale,
            modifier = Modifier
                .background(Color.Transparent)
                .matchParentSize()
                .pointerInput(true) {
                    detectTapGestures(
                        onTap = { onTap() },
                        onLongPress = { onLongPress(it) }
                    )
                },
        )
    }
}