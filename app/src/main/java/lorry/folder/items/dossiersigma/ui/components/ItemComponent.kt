package lorry.folder.items.dossiersigma.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.lifecycle.viewModelScope
import coil.request.ImageRequest
import com.pointlessapps.rt_editor.utils.RichTextValueSnapshot
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.data.dataSaver.CompositeData
import lorry.folder.items.dossiersigma.data.dataSaver.CroppedPicture
import lorry.folder.items.dossiersigma.data.dataSaver.Flag
import lorry.folder.items.dossiersigma.data.dataSaver.InitialPicture
import lorry.folder.items.dossiersigma.data.dataSaver.Scale
import lorry.folder.items.dossiersigma.domain.ColoredTag
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.domain.SigmaFile
import lorry.folder.items.dossiersigma.domain.SigmaFolder
import lorry.folder.items.dossiersigma.domain.usecases.browser.BrowserTarget
import lorry.folder.items.dossiersigma.ui.MainActivity
import lorry.folder.items.dossiersigma.ui.SigmaViewModel
import lorry.folder.items.dossiersigma.ui.components.Tools.DEFAULT
import java.io.File
import java.io.FileOutputStream

@Composable
fun ItemComponent(
    modifier: Modifier,
    viewModel: SigmaViewModel,
    item: Item,
    imageCache: MutableMap<String, Any?>,
    scaleCache: MutableMap<String, ContentScale>,
    flagCache: StateFlow<MutableMap<String, ColoredTag>>,
    memoCache: StateFlow<MutableMap<String, RichTextValueSnapshot>>,
    context: MainActivity,
) {
    var imageOffset by remember { mutableStateOf(DpOffset.Zero) }
    val density = LocalDensity.current
    val imageHeight = 160.dp
    val imageSource = remember(item.fullPath) { mutableStateOf<Any?>(null) }
    val pictureUpdateId by viewModel.pictureUpdateId.collectAsState()
    var contentScale by remember { mutableStateOf(ContentScale.Crop) }

    val coloredTag by flagCache.collectAsState()
    val tag = coloredTag[item.fullPath]

    val dragOffset by viewModel.dragOffset.collectAsState()
//    val draggedTag by viewModel.draggedTag.collectAsState()
    val draggableStartPosition by viewModel.draggableStartPosition.collectAsState()
    val bounds = remember { mutableStateOf<Rect?>(null) }

    val isHovered = remember(draggableStartPosition, dragOffset) {
        if (dragOffset == null || draggableStartPosition == null)
            return@remember false

        val new = bounds.value?.contains(draggableStartPosition!! + dragOffset!!) == true
//        if (new) 
//            println("DRAG survolé: item ${item.name}") 
//            else println("DRAG sorti: item ${item.name}")
        new
    }

    LaunchedEffect(isHovered) {
        if (isHovered)
            viewModel.setDragTargetItem(item)
        else
            viewModel.setDragTargetItem(null)
    }

    LaunchedEffect(item.fullPath, pictureUpdateId) {
        val composite = item.getComposite()

        ///////////
        // image //
        ///////////
        val cached = imageCache[item.fullPath]
        if (cached != null) {
            imageSource.value = cached
        } else {
            val result = getImage(item = item, viewModel = viewModel, context = context, composite = composite)
            imageCache[item.fullPath] = null
            imageCache[item.fullPath] = result
            imageSource.value = result
        }

        ///////////
        // scale //
        ///////////
        val scaleCached = scaleCache[item.fullPath]
        contentScale = scaleCached ?: composite?.getScale() ?: ContentScale.Crop
        scaleCache[item.fullPath] = contentScale

        //////////
        // flag //
        //////////
        val flagCached = flagCache.value[item.fullPath]
        item.tag = flagCached ?: composite?.getFlag().also {
            if (it != null) {
                viewModel.setFlagCacheValue(item.fullPath, it)
            }
        }
        
        //////////
        // memo //
        //////////
        val memoCached = memoCache.value[item.fullPath]
        item.memo = memoCached ?: composite?.getMemo().also {
            if (it != null) {
                viewModel.setMemoCacheValue(item.fullPath, it)
            }
        }
    }


    LaunchedEffect(item.fullPath) {
        
    }

    Column() {
        val shape1 = RoundedCornerShape(8.dp)
        val selectedItemFullPath by viewModel.selectedItemFullPath.collectAsState()

        val modifierWithBorder = Modifier
            .clip(shape1)
            .background(Color.Transparent)
            .then(
                if (item.fullPath == selectedItemFullPath)
                    Modifier.dashedBorder(
                        color = Color(0xFFDBBC00),
                        strokeWidth = 2.dp,
                        cornerRadius = 8.dp,
                        dashLength = 10.dp,
                        gapLength = 10.dp
                    )
                else (if (tag != null) {
                    Modifier.border(2.dp, tag.color, shape1)
                } else Modifier)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        if (selectedItemFullPath != null) {
                            viewModel.setSelectedItem(null, true)
                            BottomTools.setCurrentContent(DEFAULT)
                            return@detectTapGestures
                        }

                        if (item.isFolder()) {
                            viewModel.goToFolder(
                                item.fullPath
                            )
                        }

                        if (item.isFile() &&
                            (item.name.endsWith(".mp4") ||
                                    item.name.endsWith(".mkv") ||
                                    item.name.endsWith(".mpg") ||
                                    item.name.endsWith(".iso") ||
                                    item.name.endsWith(".avi"))
                        ) {
                            viewModel.playVideoFile(item.fullPath)
                        }
                        if (item.isFile() && item.name.endsWith(".html")) {
                            viewModel.playHtmlFile(item.fullPath)
                        }
                    },
                    onLongPress = { offset ->
//                                        imageOffset = DpOffset(offset.x.toInt().dp, offset.y.toInt().dp)
//                                        viewModel.setIsContextMenuVisible(true)
                        viewModel.setSelectedItem(item, true)
                        BottomTools.setCurrentContent(Tools.FILE)
                    })
            }

        key(item.tag?.id) {
            Box(
                modifier = modifierWithBorder
                    .width(imageHeight)
                    .height(imageHeight)
                    .onGloballyPositioned {
                        val pos = it.positionInRoot()
                        bounds.value = Rect(
                            offset = pos,
                            size = Size(
                                it.size.width.toFloat(),
                                it.size.height.toFloat()
                            )
                        )
                    }
                    .then(
                        if (isHovered) Modifier.border(2.dp, Color.Black)
                        else Modifier
                    )
            ) {
                var expanded by remember { mutableStateOf(false) }
                val scrollState = rememberScrollState()

                imageSource.value?.let { bitmap ->
                    key(pictureUpdateId) {
                        ImageSection(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color.Transparent, RoundedCornerShape(8.dp)),
                            imageSource = imageSource.value as Bitmap? ?: vectorDrawableToBitmap(
                                context, R.drawable.file
                            ),
                            contentScale = contentScale,
                            item = item,
                            selectedItemFullPath = selectedItemFullPath
                        )
                    }
                }


                val infoSup = produceState<String?>(initialValue = null, item) {
                    value = viewModel.getInfoSup(item)
                }.value

                val infoInf = produceState<String?>(initialValue = null, item) {
                    value = viewModel.getInfoInf(item)
                }.value


                if (infoSup == null || infoInf == null) {
//                        CircularProgressIndicator()
                } else {
                    val boxWidth = 40.dp //30

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
                            .background(tag?.color ?: Color.Gray)
                            .widthIn(min = boxWidth)
                            .clickable {
                                viewModel.setIsDisplayingMemo(!viewModel.isDisplayingMemo.value)
                            }
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
                                text = infoSup,
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
                                text = infoInf,
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                if (item.isFolder())
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
                                color = Color(0xFFCCFF00), // rouge tampons administratifs
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "DOSSIER",
                            fontSize = 11.sp,
                            color = Color(0xFF0047AB),
                            letterSpacing = 1.sp,
                            lineHeight = 12.sp
                        )
                    }

                val selectedItem by viewModel.selectedItem.collectAsState()
                val isContextMenuVisible = context.isContextMenuVisible

                DropdownMenu(
                    expanded = selectedItem?.id == item.id && context.isContextMenuVisible.value,
                    onDismissRequest = {
                        viewModel.setIsContextMenuVisible(false)
                    },
                    offset = with(density) {
                        DpOffset(imageOffset.x, (-imageHeight / 2))
                    },
                    containerColor = Color(0xFF111A2C),
                    scrollState = scrollState
                ) {
                    val modifierItem = Modifier.height(25.dp)

                    DropdownMenuItem(
                        modifier = modifierItem,
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
                                modifier = Modifier.size(20.dp),
                                painter = when (item) {
                                    is SigmaFolder -> painterResource(R.drawable.dossier)
                                    is SigmaFile -> when (item.name.substringAfterLast(".")) {
                                        "mp4", "mpg", "avi", "mkv", "iso" -> painterResource(R.drawable.camera)
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

                    val itemfontSizes = 14.sp

                    ////////////
                    // Google //
                    ////////////
                    DropdownMenuItem(
                        modifier = modifierItem,
                        text = {
                            Text(
                                "Google", color = Color(0xFFB0BEC5),
                                fontSize = itemfontSizes,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier
                                    .size(15.dp),
                                tint = Color(0xFF90CAF9),
                                painter = painterResource(R.drawable.web_nb), contentDescription = null
                            )
                        },
                        onClick = {
                            /**
                             * @see BrowserOverlay
                             * le Browser est un composable dans MainActivity
                             * voir BrowserOverlay et son appel par MainActivity
                             * le callback est un de ses paramètres d'appel
                             */
                            viewModel.browserManager.openBrowser(item, BrowserTarget.GOOGLE)
                            viewModel.setIsContextMenuVisible(false)
                        }
                    )

                    //////////
                    // crop //
                    //////////
                    DropdownMenuItem(
                        modifier = modifierItem,
                        text = {
                            Text(
                                text = when (contentScale) {
                                    ContentScale.Crop -> "Rogner"
                                    ContentScale.None -> "Aucun"
                                    ContentScale.Inside -> "Dedans"
                                    ContentScale.FillBounds -> "Remplir"
                                    ContentScale.FillHeight -> "Remplir hauteur"
                                    ContentScale.FillWidth -> "Remplir largeur"
                                    ContentScale.Fit -> "Etirer"
                                    else -> "???"
                                },
                                color = Color(0xFFB0BEC5),
                                fontSize = itemfontSizes,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier
                                    .size(15.dp),
                                tint = Color(0xFF90CAF9),
                                painter = painterResource(R.drawable.la_droite), contentDescription = null
                            )
                        },
                        onClick = {
                            contentScale = when (contentScale) {
                                ContentScale.Crop -> ContentScale.Fit
                                ContentScale.Fit -> ContentScale.FillBounds
                                ContentScale.FillBounds -> ContentScale.FillWidth
                                ContentScale.FillWidth -> ContentScale.FillHeight
                                ContentScale.FillHeight -> ContentScale.Inside
                                ContentScale.Inside -> ContentScale.None
                                ContentScale.None -> ContentScale.Crop
                                else -> ContentScale.Crop
                            }

                            scaleCache[item.fullPath] = contentScale

                            if (item.isFile() &&
                                item.fullPath.endsWith(".mp4") ||
                                item.fullPath.endsWith(".avi") ||
                                item.fullPath.endsWith(".mpg") ||
                                item.fullPath.endsWith(".html") ||
                                item.fullPath.endsWith(".iso") ||
                                item.fullPath.endsWith(".mkv")
                            ) {
                                viewModel.viewModelScope.launch {
                                    val file = File(item.fullPath)
                                    viewModel.base64Embedder.removeScale(file)
                                    viewModel.base64Embedder.appendScaleToFile(file, contentScale)
                                }
                            }

                            if (item.isFolder()) {
                                viewModel.viewModelScope.launch {
                                    val file = File(item.fullPath + "/.folderPicture.html")
                                    if (!file.exists())
                                        viewModel.diskRepository.createFolderHtmlFile(item)
                                    viewModel.diskRepository.removeScaleFromHtml(item.fullPath)
                                    viewModel.diskRepository.insertScaleToHtmlFile(item, contentScale)
                                }
                            }

                            viewModel.setSelectedItem(null, true)
                            viewModel.setIsContextMenuVisible(false)
                            BottomTools.setCurrentContent(DEFAULT)
                        }
                    )

                    /////////////////
                    // crop manuel //
                    /////////////////
                    DropdownMenuItem(
                        modifier = modifierItem,
                        text = {
                            Text(
                                text = "Couper manuellement",
                                color = Color(0xFFB0BEC5),
                                fontSize = itemfontSizes,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier
                                    .size(15.dp),
                                tint = Color(0xFF90CAF9),
                                painter = painterResource(R.drawable.crop), contentDescription = null
                            )
                        },
                        onClick = {
                            val sourceBitmap = imageSource.value as Bitmap

                            val sourceUri = bitmapToTempUri(context, sourceBitmap)
                            val destinationUri =
                                Uri.fromFile(
                                    File.createTempFile(
                                        "cropped_", ".jpg",
                                        context.cacheDir
                                    )
                                )

                            //le callback est dans MainActivity : onActivityResult (override)
                            UCrop.of(sourceUri, destinationUri)
                                .withAspectRatio(1f, 1f)
                                .withMaxResultSize(175, 175)
                                .start(context)
                        }
                    )

//                DropdownMenuItem(
//                    text = {
//                        Text(
//                            text = "Renommer",
//                            color = Color(0xFFB0BEC5)
//                        )
//                    },
//                    leadingIcon = {
//                        Icon(
//                            modifier = Modifier
//                                .size(24.dp),
//                            tint = Color(0xFF90CAF9),
//                            painter = painterResource(R.drawable.corbeille), contentDescription = null
//                        )
//                    },
//                    onClick = {
//                        File(item.fullPath).deleteRecursively()
//                        expandedAddition = false
//                        viewModel.goToFolder(
//                            item.fullPath.substringBeforeLast("/"),
//                            ITEMS_ORDERING_STRATEGY.DATE_DESC
//                        )
//                    }
//                )

                    /////////////////////
                    // + dossier frère //
                    /////////////////////
                    DropdownMenuItem(
                        modifier = modifierItem,
                        text = {
                            Text(
                                text = "Créer dossier frère",
                                color = Color(0xFFB0BEC5),
                                fontSize = itemfontSizes,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier
                                    .size(15.dp),
                                tint = Color(0xFF90CAF9),
                                painter = painterResource(R.drawable.plus), contentDescription = null
                            )
                        },
                        onClick = {
                            viewModel.setSelectedItem(null, true)
                            viewModel.setIsContextMenuVisible(false)
                            viewModel.setDialogMessage("Ajouter un dossier frère")
                            viewModel.dialogOnOkLambda = { name, viewModel, context ->
                                File(item.fullPath.substringBeforeLast("/") + "/$name").mkdir()
                                viewModel.refreshCurrentFolder()
                                BottomTools.setCurrentContent(DEFAULT)
                            }

                            context.openTextDialog.value = true
                        }
                    )

                    ////////////////////
                    // + dossier fils //
                    ////////////////////
                    if (item.isFolder()) {
                        DropdownMenuItem(
                            modifier = modifierItem,
                            text = {
                                Text(
                                    text = "Créer dossier fils",
                                    color = Color(0xFFB0BEC5),
                                    fontSize = itemfontSizes,
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    modifier = Modifier
                                        .size(15.dp),
                                    tint = Color(0xFF90CAF9),
                                    painter = painterResource(R.drawable.plus), contentDescription = null
                                )
                            },
                            onClick = {
                                viewModel.setIsContextMenuVisible(false)
                                viewModel.setSelectedItem(null, true)
                                viewModel.setDialogMessage("Ajouter un dossier fils")
                                viewModel.dialogOnOkLambda = { name, viewModel, context ->
                                    File(item.fullPath + "/$name").mkdir()
                                    viewModel.refreshCurrentFolder()
                                    BottomTools.setCurrentContent(DEFAULT)
                                }

                                context.openTextDialog.value = true
                            }
                        )
                    }

                    ///////////////
                    // supprimer //
                    ///////////////
                    DropdownMenuItem(
                        modifier = modifierItem,
                        text = {
                            Text(
                                text = "Supprimer",
                                color = Color(0xFFB0BEC5),
                                fontSize = itemfontSizes,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier
                                    .size(15.dp),
                                tint = Color(0xFF90CAF9),
                                painter = painterResource(R.drawable.corbeille), contentDescription = null
                            )
                        },
                        onClick = {
                            if (item.isFolder())
                                File(item.fullPath).deleteRecursively()
                            else File(item.fullPath).delete()
                            viewModel.setSelectedItem(null, true)
                            viewModel.setIsContextMenuVisible(false)
                            viewModel.refreshCurrentFolder()
                            BottomTools.setCurrentContent(DEFAULT)
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
        }

        TextSection(
            modifier = Modifier
                .height(52.dp)
                .align(Alignment.CenterHorizontally),
            name = if (item.isFile())
                item.name.substringBeforeLast(".")
            else item.name
        )
    }
}

suspend fun getImage(
    item: Item,
    viewModel: SigmaViewModel,
    context: MainActivity,
    composite: CompositeData?,
): Bitmap {
    var result: Bitmap
    result = when {
        item.picture != null -> item.picture as Bitmap // Utilise l'image en mémoire si disponible

        item is SigmaFile -> {
//            if (item.fullPath.endsWith("mp4") ||
//                item.fullPath.endsWith("mkv") ||
//                item.fullPath.endsWith("avi") ||
//                item.fullPath.endsWith("html") ||
//                item.fullPath.endsWith("iso") ||
//                item.fullPath.endsWith("mpg")
//            ) {
            var image = composite?.getCroppedPicture() ?: composite?.getInitialPicture()
            ?: vectorDrawableToBitmap(context, R.drawable.file)

            image
//            } else vectorDrawableToBitmap(context, R.drawable.file)
        }

        item is SigmaFolder -> {
            val initialPicture = composite?.getInitialPicture()
            val croppedPicture = composite?.getCroppedPicture()

            if (initialPicture == null && croppedPicture == null) {
                val isPopulated = viewModel.changingPictureUseCase.isFolderPopulated(item)
                if (isPopulated) vectorDrawableToBitmap(
                    context, R.drawable.folder_full
                )
                else vectorDrawableToBitmap(context, R.drawable.folder_empty)
            } else {
                //une image est présente pour ce répertoire
                if (croppedPicture != null)
                    croppedPicture
                else
                    initialPicture as Bitmap

            }

        }
//            var hasPictureFile = viewModel.diskRepository.hasPictureFile(item)
//
//            if (!hasPictureFile) {
//                val isPopulated = viewModel.changingPictureUseCase.isFolderPopulated(item)
//                if (isPopulated) vectorDrawableToBitmap(
//                    context,
//                    R.drawable.folder_full
//                ) else vectorDrawableToBitmap(context, R.drawable.folder_empty)
//            } else {
//                //une image est présente pour ce répertoire
//                try {
//                    var picture: Bitmap? = null
//                    val folderPicturePath = "${item.fullPath}/" +
//                            ".folderPicture.html"
//                    val folderPictureCroppedPath = "${item.fullPath}/" +
//                            ".folderPictureCropped.html"
//
//                    if (File(folderPictureCroppedPath).exists())
//                        picture = viewModel.base64DataSource.extractImageFromHtml(folderPictureCroppedPath)
//                    else {
//                        picture = viewModel.base64DataSource.extractImageFromHtml(folderPicturePath)
//                    }


//                    if (picture != null) {
//                        item.copy(picture = picture)
//
//                        //maintenance
//                        if (!File(folderPictureCroppedPath).exists())
//                            viewModel.diskRepository.saveFolderPictureToHtmlFile(item, true)
//
//                        picture as Bitmap
//                    } else vectorDrawableToBitmap(
//                        context,
//                        if (viewModel.diskRepository.countFilesAndFolders(File(item.fullPath)) == Pair(
//                                0,
//                                0
//                            )
//                        ) R.drawable.folder_empty
//                        else R.drawable.folder_full
//                    )
        else -> vectorDrawableToBitmap(
            context, R.drawable.folder_full
        )

    }

    return result
}

suspend fun getFlag(
    item: Item,
): ColoredTag? {

    //créer html d'office

    if (item.isFile())
        return Flag.fileGet(item.fullPath)

    if (item.isFolder())
        return Flag.folderGet(item.fullPath)

    return null

    //il faudrait pouvoir savoir si le flag est dans la liste des Tools.DEFAULT
    //si non => supprimer le Tag du fichier
    //comment est construite cette liste Tools.DEFAULT? progressivement? 
    //dans ce cas est-elle complète maintenant?
}

fun vectorDrawableToBitmap(context: Context, drawableId: Int): Bitmap {
    val drawable =
        context.getDrawable(drawableId) ?: throw IllegalArgumentException("Drawable introuvable")
    val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
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
    imageSource: Bitmap,
    contentScale: ContentScale,
    item: Item,
    selectedItemFullPath: String?,
) {
    val context = LocalContext.current
    val imageRequest = ImageRequest.Builder(context).data(imageSource).build()
    val imageBitmap: Bitmap? = imageSource
    var containerSize = IntSize(175, 175)

    val shouldShowMesh = remember(imageBitmap, contentScale) {
        if (imageBitmap != null) {
            !doesImageFillBox(
                containerWidth = containerSize.width,
                containerHeight = containerSize.height,
                imageWidth = imageBitmap.width,
                imageHeight = imageBitmap.height,
                contentScale = contentScale
            )
        } else false
    }

//    val shape = RoundedCornerShape(8.dp)
//
//    val modifierWithBorder = Modifier
//        .clip(shape)
//        .background(Color.Transparent)
//        .then(
//            if (item.id == selectedItemId)
//                Modifier.dashedBorder(
//                    color = Color(0xFFDBBC00),
//                    strokeWidth = 2.dp,
//                    cornerRadius = 8.dp,
//                    dashLength = 10.dp,
//                    gapLength = 10.dp
//                )
//            else Modifier
//        )
//        .then(modifier)

    Box(
        modifier = Modifier
    ) {
        if (shouldShowMesh) {
            Image(
                painter = painterResource(R.drawable.maillage1),
                contentDescription = null,
                modifier = Modifier
                    .matchParentSize()
                    .zIndex(0f)
//                    .alpha(1f)
            )
        }

        Image(
            bitmap = imageSource.asImageBitmap(),
            contentDescription = "Miniature",
            contentScale = contentScale,
            modifier = modifier
                .matchParentSize()
        )

//        AsyncImage(
//            model = imageRequest,
//            contentDescription = "Miniature",
//            contentScale = contentScale,
//            modifier = Modifier
//                .fillMaxSize()
//                .zIndex(0f)
//        )
    }
}

fun doesImageFillBox(
    containerWidth: Int,
    containerHeight: Int,
    imageWidth: Int,
    imageHeight: Int,
    contentScale: ContentScale
): Boolean {
    if (imageWidth <= 0 || imageHeight <= 0 || containerWidth <= 0 || containerHeight <= 0)
        return false

    val containerRatio = containerWidth.toFloat() / containerHeight
    val imageRatio = imageWidth.toFloat() / imageHeight

    return when (contentScale) {
        ContentScale.Crop,
        ContentScale.FillBounds -> true

        ContentScale.Fit,
        ContentScale.Inside -> {
            if (imageRatio > containerRatio) {
                (containerWidth / imageRatio) >= containerHeight
            } else {
                (containerHeight * imageRatio) >= containerWidth
            }
        }

        ContentScale.FillWidth -> imageRatio <= containerRatio
        ContentScale.FillHeight -> imageRatio >= containerRatio
        ContentScale.None -> false
        else -> false
    }
}

fun bitmapToTempUri(context: Context, bitmap: Bitmap): Uri {
    val tempFile = File.createTempFile("source_", ".jpg", context.cacheDir)
    FileOutputStream(tempFile).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
    }
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",  // N'oublie pas de déclarer FileProvider dans le manifest
        tempFile
    )
}

fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: Dp = 2.dp,
    cornerRadius: Dp = 0.dp,
    dashLength: Dp = 10.dp,
    gapLength: Dp = 10.dp
): Modifier = drawWithContent {
    drawContent() // dessine l’image ou autre contenu en premier

    val stroke = strokeWidth.toPx()
    val radius = cornerRadius.toPx()
    val dash = dashLength.toPx()
    val gap = gapLength.toPx()

    val inset = stroke / 2f
    val rect = Rect(
        left = inset,
        top = inset,
        right = size.width - inset,
        bottom = size.height - inset
    )

    val path = Path().apply {
        addRoundRect(RoundRect(rect, CornerRadius(radius)))
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = stroke,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, gap), 0f)
        )
    )
}

suspend fun managePicture(
    composite: CompositeData?,
    item: Item,
    imageCache: MutableMap<String, Any?>,
    flagCache: StateFlow<MutableMap<String, ColoredTag>>,
    scaleCache: MutableMap<String, ContentScale>,
    imageSource: MutableState<Any?>,
    viewModel: SigmaViewModel,
    context: MainActivity
) {

    val cached = imageCache[item.fullPath]
    if (cached != null) {
        imageSource.value = cached
    } else {
        val result = getImage(item, viewModel, context, composite)
        imageCache[item.fullPath] = null
        imageCache[item.fullPath] = result
        imageSource.value = result
    }
}