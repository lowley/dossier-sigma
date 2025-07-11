package lorry.folder.items.dossiersigma.ui.components

import android.content.Context
import android.graphics.Bitmap
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.map
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.ui.SigmaViewModel
import lorry.folder.items.dossiersigma.ui.components.Tools.DEFAULT
import java.io.File
import java.io.FileOutputStream

@Composable
fun ItemComponent(
    modifier: Modifier,
    viewModel: SigmaViewModel,
    item: Item,
    onItemUpdated: (Item) -> Unit
) {
    val image by viewModel.imageCache
        .map { map -> map[item.fullPath] }
        .collectAsState(initial = item.picture)

    val tag by viewModel.flagCache
        .map { map -> map[item.fullPath] }
        .collectAsState(initial = item.tag)

    val scale by viewModel.scaleCache
        .map { map -> map[item.fullPath] }
        .collectAsState(initial = item.scale)

    val memo by viewModel.memoCache
        .map { map -> map[item.fullPath] }
        .collectAsState(initial = item.memo)

    var imageOffset by remember { mutableStateOf(DpOffset.Zero) }
    val density = LocalDensity.current
    val imageHeight = 160.dp

    val dragOffset by viewModel.dragOffset.collectAsState()
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

    Column() {
        val shape1 = RoundedCornerShape(8.dp)
        val selectedItemFullPathState by viewModel.selectedItemFullPath.collectAsState()
        val isSelectedItemState by viewModel.selectedItemFullPath
            .map { it == item.fullPath }
            .collectAsState(false)

        val modifierWithBorder = Modifier
            .clip(shape1)
            .background(Color.Transparent)
            .then(
                if (isSelectedItemState)
                    Modifier.dashedBorder(
                        color = Color(0xFFDBBC00),
                        strokeWidth = 2.dp,
                        cornerRadius = 8.dp,
                        dashLength = 10.dp,
                        gapLength = 10.dp
                    )
                else (if (tag != null) {
                    Modifier.border(2.dp, tag!!.color, shape1)
                } else Modifier)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        if (selectedItemFullPathState != null) {
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
                        viewModel.setSelectedItem(item.copy(), true)
                        BottomTools.setCurrentContent(Tools.FILE)
                    })
            }

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
            ImageSection(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color.Transparent, RoundedCornerShape(8.dp)),
                image = image,
                scale = scale
            )

            val infoSup = produceState<String?>(initialValue = null, item) {
                value = viewModel.getInfoSup(item)
            }.value

            val infoInf = produceState<String?>(initialValue = null, item) {
                value = viewModel.getInfoInf(item)
            }.value

            if (infoSup == null || infoInf == null) {
//                        CircularProgressIndicator()
            } else {
                val boxWidth = 45.dp

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
                            viewModel.setSelectedItem(item.copy())
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

                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(0.dp)
                                .height(textHeight),
                            text = infoSup,
                            fontWeight = if (memo.isNullOrEmpty()) FontWeight.ExtraLight else FontWeight
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
                            fontWeight = if (memo.isNullOrEmpty()) FontWeight.ExtraLight else FontWeight.ExtraBold,
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
    image: Any?,
    scale: ContentScale?,
) {
    var imageSize by remember { mutableStateOf<IntSize?>(null) }
    var containerSize = IntSize(175, 175)

    // Le calcul reste le même, il sera relancé quand imageSize changera
    val shouldShowMesh = remember(imageSize, scale) {
        val size = imageSize
        if (size != null) {
            !doesImageFillBox(
                containerWidth = containerSize.width,
                containerHeight = containerSize.height,
                imageWidth = size.width,
                imageHeight = size.height,
                contentScale = scale ?: ContentScale.Crop
            )
        } else {
            false // On ne montre pas le maillage avant de connaître la taille
        }
    }

    Box(
        modifier = modifier.onSizeChanged { containerSize = it }
    ) {
        if (shouldShowMesh) {
            Image(
                painter = painterResource(R.drawable.maillage1),
                contentDescription = null,
                modifier = Modifier.matchParentSize()
            )
        }

        AsyncImage(
            model = image,
            contentDescription = "Miniature",
            contentScale = scale ?: ContentScale.Crop,
            // Callback pour récupérer la taille de l'image une fois chargée
            onSuccess = { successState ->
                val drawable = successState.result.drawable
                imageSize = IntSize(drawable.intrinsicWidth, drawable.intrinsicHeight)
            },
            modifier = Modifier.matchParentSize()
        )
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

fun imageAsAnyToTempUri(context: Context, image: Any): Uri {
    var bitmap = image as? Bitmap
    if (bitmap == null) {
        val imageInt = image as? Int
            ?: throw Exception("imageAsAnyToTempUri: erreur de conversion d'une image")

        val drawable = ContextCompat.getDrawable(context, imageInt)
        bitmap = drawable?.toBitmap()
    }

    if (bitmap == null)
        throw Exception("imageAsAnyToTempUri: erreur de conversion d'une image")

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