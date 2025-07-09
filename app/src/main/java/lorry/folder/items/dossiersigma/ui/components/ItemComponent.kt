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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import coil.request.ImageRequest
import com.pointlessapps.rt_editor.utils.RichTextValueSnapshot
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.data.base64.Base64DataSource
import lorry.folder.items.dossiersigma.data.base64.Tags
import lorry.folder.items.dossiersigma.data.base64.VideoInfoEmbedder
import lorry.folder.items.dossiersigma.data.dataSaver.CompositeData
import lorry.folder.items.dossiersigma.data.dataSaver.CompositeManager
import lorry.folder.items.dossiersigma.data.dataSaver.Flag
import lorry.folder.items.dossiersigma.data.dataSaver.InitialPicture
import lorry.folder.items.dossiersigma.data.dataSaver.Memo
import lorry.folder.items.dossiersigma.data.dataSaver.Scale
import lorry.folder.items.dossiersigma.domain.ColoredTag
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.domain.SigmaFile
import lorry.folder.items.dossiersigma.domain.SigmaFolder
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
    scaleCache: StateFlow<MutableMap<String, ContentScale>>,
    flagCache: StateFlow<MutableMap<String, ColoredTag>>,
    memoCache: StateFlow<MutableMap<String, String>>,
    context: MainActivity,
) {
    var imageOffset by remember { mutableStateOf(DpOffset.Zero) }
    val density = LocalDensity.current
    val imageHeight = 160.dp
    val imageSource = remember(item.fullPath) { mutableStateOf<Any?>(null) }
    val pictureUpdateId by viewModel.pictureUpdateId.collectAsState()

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
        new
    }

    LaunchedEffect(isHovered) {
        if (isHovered)
            viewModel.setDragTargetItem(item)
        else
            viewModel.setDragTargetItem(null)
    }

    LaunchedEffect(item.fullPath, pictureUpdateId) {
        val newComposite = item.getComposite()
        val newCompositeManager = CompositeManager(item.fullPath)
        val oldCompositeManager = CompositeManager(item.fullPath, useOld = true)
        val oldComposite = oldCompositeManager.getComposite()

        ///////////
        // image //
        ///////////
        val cached = imageCache[item.fullPath]
        if (cached != null) {
            imageSource.value = cached
        } else {
            val result =
                getImage(
                    item = item,
                    viewModel = viewModel,
                    context = context,
                    newComposite = newComposite,
                    oldComposite = oldComposite
                )
            imageCache[item.fullPath] = null
            imageCache[item.fullPath] = result
            imageSource.value = result
        }

        ///////////
        // scale //
        ///////////
        val scaleCached = scaleCache.value[item.fullPath]
        if (scaleCached != null) {
            item.scale = scaleCached
        } else {
            val fromDisk = newCompositeManager.getElement(Scale)
            item.scale = fromDisk

            if (fromDisk != null) {
                viewModel.setScaleCacheValue(item.fullPath, fromDisk)
            }
        }

        //////////
        // flag //
        //////////
        val flagCached = flagCache.value[item.fullPath]
        if (flagCached != null) {
            item.tag = flagCached
        } else {
            val fromDisk = newCompositeManager.getElement(Flag)
            item.tag = fromDisk
            if (fromDisk != null) {
                viewModel.setFlagCacheValue(item.fullPath, fromDisk)
            }
        }

        //////////
        // memo //
        //////////
        val memoCached = memoCache.value[item.fullPath]
        if (memoCached != null) {
            item.memo = memoCached
        } else {
            val fromDisk = newCompositeManager.getElement(Memo)
            item.memo = fromDisk
            if (fromDisk != null) {
                viewModel.setMemoCacheValue(item.fullPath, fromDisk)
            }
        }
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
                            viewModel.goToFolder(item.fullPath)
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
                val scaleMap by scaleCache.collectAsState()
                val scaleKey = scaleMap[item.fullPath]

                imageSource.value?.let { bitmap ->
                    key(pictureUpdateId, item.toString(), scaleKey) {
                        ImageSection(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color.Transparent, RoundedCornerShape(8.dp)),
                            imageSource = imageSource.value as Bitmap? ?: vectorDrawableToBitmap(
                                context, R.drawable.file
                            ),
                            item = item,
                            selectedItemFullPath = selectedItemFullPath,
                            scaleKey = scaleKey
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
                    val boxWidth = 45.dp //30

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
                            .width(boxWidth)
                            .clickable {
                                /**
                                 * suite dans MainActivity
                                 */
                                viewModel.setSelectedItem(item)
                                viewModel.setIsDisplayingMemo(!viewModel.isDisplayingMemo.value)
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(start = 0.dp, top = 0.dp)
                                .width(boxWidth)
                        ) {
                            val textHeight = 18.dp
                            val memoCacheLocal by memoCache.collectAsState()


                            key(memoCacheLocal[item.fullPath]) {
                                Text(
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .padding(0.dp)
                                        .height(textHeight),
                                    text = infoSup,
                                    fontWeight = if (item.isMemoUnchanged()) FontWeight.ExtraLight else FontWeight
                                        .ExtraBold,
                                    fontSize = 10.sp,
                                    color = Color.White
                                )
                                Text(
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .padding(
                                            top = 0.dp, start = 0.dp, bottom = 5.dp, end = 0.dp
                                        )
                                        .height(textHeight),
                                    text = infoInf,
                                    fontWeight = if (item.isMemoUnchanged()) FontWeight.ExtraLight else FontWeight.ExtraBold,
                                    fontSize = 10.sp,
                                    color = Color.White
                                )
                            }
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
    newComposite: CompositeData?,
    oldComposite: CompositeData?,
): Bitmap {
    var result: Bitmap
    result = when {
        item.picture != null -> item.picture as Bitmap // Utilise l'image en mémoire si disponible

        item is SigmaFile -> {
            var image = newComposite?.getCroppedPicture() ?: newComposite?.getInitialPicture()
            val repo = VideoInfoEmbedder()

            if (image == null) {
//            if (item.fullPath.endsWith("mp4") ||
//                item.fullPath.endsWith("mkv") ||
//                item.fullPath.endsWith("avi") ||
//                item.fullPath.endsWith("html") ||
//                item.fullPath.endsWith("iso") ||
//                item.fullPath.endsWith("mpg")
//            ) {
                var image = oldComposite?.getCroppedPicture() ?: oldComposite?.getInitialPicture()

                if (image != null) {
                    val compositeMgr = CompositeManager(item.fullPath)
                    compositeMgr.save(InitialPicture(image, repo))
                } else {
                    //récupère image existante selon l'ancienne méthode, si elle existe
                    val image64 = repo.extractBase64FromFile(
                        File(item.fullPath),
                        tag = Tags.COVER
                    )
                    if (image64 != null) {
                        image = repo.base64ToBitmap(image64)
                        val compositeMgr = CompositeManager(item.fullPath)
                        compositeMgr.save(InitialPicture(image, repo))
                    }
                }
            }

            image = image ?: vectorDrawableToBitmap(context, R.drawable.file)
            image
        }

        item is SigmaFolder -> {
            var image = newComposite?.getCroppedPicture() ?: newComposite?.getInitialPicture()

            val compositeMgr = CompositeManager(item.fullPath)
            val repo = Base64DataSource()
            val repo2 = VideoInfoEmbedder()

            if (image == null) {
                var image = oldComposite?.getCroppedPicture() ?: oldComposite?.getInitialPicture()

                if (image != null) {
                    val compositeMgr = CompositeManager(item.fullPath)
                    compositeMgr.save(InitialPicture(image, repo2))
                }
            }
            
            if (image == null) {
                image = repo.extractImageFromHtml("${item.fullPath}/.folderPicture.html")

                compositeMgr.save(InitialPicture(image, repo2))
            }

            if (newComposite?.getInitialPicture() == null
                && image != null
            ) {
                compositeMgr.save(InitialPicture(image, repo2))
            }

            if (image == null) {
                val isPopulated = viewModel.changingPictureUseCase.isFolderPopulated(item)
                image = if (isPopulated)
                    vectorDrawableToBitmap(
                        context, R.drawable.folder_full
                    )
                else vectorDrawableToBitmap(context, R.drawable.folder_empty)
            }

            image
        }

        else -> vectorDrawableToBitmap(
            context, R.drawable.folder_full
        )

    }

    return result
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
    item: Item,
    selectedItemFullPath: String?,
    scaleKey: ContentScale?,
) {
    val context = LocalContext.current
    val imageRequest = ImageRequest.Builder(context).data(imageSource).build()
    val imageBitmap: Bitmap? = imageSource
    var containerSize = IntSize(175, 175)

    val shouldShowMesh = remember(imageBitmap, scaleKey) {
        if (imageBitmap != null) {
            !doesImageFillBox(
                containerWidth = containerSize.width,
                containerHeight = containerSize.height,
                imageWidth = imageBitmap.width,
                imageHeight = imageBitmap.height,
                contentScale = item.scale ?: ContentScale.Crop
            )
        } else false
    }

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
            contentScale = item.scale ?: ContentScale.Crop,
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