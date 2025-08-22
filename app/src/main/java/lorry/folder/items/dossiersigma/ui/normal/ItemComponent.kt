package lorry.folder.items.dossiersigma.ui.normal

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import coil.compose.SubcomposeAsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.headless.domain.ColoredTag
import lorry.folder.items.dossiersigma.headless.domain.Item
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity
import lorry.folder.items.dossiersigma.ui.sigma.SigmaColors
import java.io.File
import java.io.FileOutputStream

@Composable
context(SigmaActivity)
fun ItemComponent(
    modifier: Modifier,
    item: Item,
    onItemUpdated: (Item) -> Unit,
    imageCache: StateFlow<MutableMap<String, Any?>>,
    flagCache: StateFlow<MutableMap<String, ColoredTag>>,
    scaleCache: StateFlow<MutableMap<String, ContentScale>>,
    memoCache: StateFlow<MutableMap<String, String>>,
    draggableStartPosition: StateFlow<Offset?>,
    onHoveredNotHovered: (Item?) -> Unit,
    selectedItemFullPath: StateFlow<String?>,
    onItemTapped: ((Item) -> Unit),
    onItemLongPressed: ((Item) -> Unit),
    onTopLeftPanelClick: (Item) -> Unit,
    getInfoSup: suspend (Item) -> String?,
    getInfoInf: suspend (Item) -> String?,

    ) {
    val image by imageCache
        .map { map -> map[item.fullPath] }
        .collectAsState(initial = item.picture)

    val tag by flagCache
        .map { map -> map[item.fullPath] }
        .collectAsState(initial = item.tag)
//
//    val scale by scaleCache
//        .map { map -> map[item.fullPath] }
//        .collectAsState(initial = item.scale)
//
//    val memo by memoCache
//        .map { map -> map[item.fullPath] }
//        .collectAsState(initial = item.memo)
//
//    val memoEmpty by memoCache
//        .map { map -> map[item.fullPath].isNullOrEmpty() }
//        .collectAsState(initial = true)

    val imageHeight = 160.dp

    val dragState by mainViewModel.dragState.collectAsState()
    var bounds by remember { mutableStateOf<Rect?>(null) }

    val isHovered = remember(dragState, bounds) {
        if (dragState != null && bounds != null)
            dragState != null && bounds?.contains(dragState!!.offset) == true
        else false
    }

    LaunchedEffect(isHovered) {
        if (isHovered)
            onHoveredNotHovered(item)
        else
            onHoveredNotHovered(null)
    }

    Column {
        val shape1 = RoundedCornerShape(8.dp)
        val isSelectedItemState by selectedItemFullPath
            .map { it == item.fullPath }
            .collectAsState(false)

        var isStartInLittleBox by remember { mutableStateOf(false) }
        var areShortcutsDisplayed = remember { mutableStateOf(false) }

        val modifierWithBorder = Modifier
            .clip(shape1)
            .background(Color.Companion.Transparent)
            .then(
                if (isSelectedItemState)
                    Modifier.dashedBorder(
                        color = Color(0xFFDBBC00),
                        strokeWidth = 2.dp,
                        cornerRadius = 8.dp,
                        dashLength = 10.dp,
                        gapLength = 10.dp
                    )
                else (
                if (tag != null) {
                    Modifier.border(2.dp, tag!!.color, shape1)
                } else Modifier)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        // * tap sur un item

                        if (areShortcutsDisplayed.value)
                            areShortcutsDisplayed.value = false
                        else onItemTapped(item)
                    },
                    onLongPress = { offset ->
                        onItemLongPressed(item)
                    })
            }

        Box(
            modifier = modifierWithBorder
//            modifier = Modifier
                .width(imageHeight)
                .height(imageHeight)
                .onGloballyPositioned {
                    val pos = it.positionInRoot()
                    bounds = Rect(
                        offset = pos,
                        size = Size(
                            it.size.width.toFloat(),
                            it.size.height.toFloat()
                        )
                    )
                }
                .then(
                    if (isHovered) Modifier.Companion.border(2.dp, Color.Companion.Black)
                    else Modifier.Companion
                )
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            // Vérifie si le point de départ est dans l'encart
                            val density = this@pointerInput
                            val boxWidthPx = with(density) { 45.dp.toPx() }
                            val boxHeightPx = with(density) { (18.dp * 2 + 5.dp).toPx() }
                            val width = this@pointerInput.size.width

                            if (offset.x <= width / 2
//                                offset.x <= boxWidthPx
//                                && offset.y <= boxHeightPx
                            ) {
                                println("Swipe DÉMARRÉ dans l’encart")
                                isStartInLittleBox = true
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            if (isStartInLittleBox) {
                                val density = this@pointerInput
                                val boxWidthPx = with(density) { 45.dp.toPx() }
                                val width = this@pointerInput.size.width

                                if (change.position.x > width / 2) {
                                    isStartInLittleBox = false
                                    areShortcutsDisplayed.value = !areShortcutsDisplayed.value
                                }
                            }
                        },
                        onDragEnd = {
                            isStartInLittleBox = false
                        }
                    )
                }
        ) {
            ImageSection(
                modifier = Modifier.Companion
                    .align(Alignment.Companion.BottomCenter)
                    .fillMaxSize()
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .border(
                        1.dp,
                        Color.Companion.Transparent,
                        androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ),
                image = image ?: item.picture,
//                image = if (item.isFile()) R.drawable.document2 else R.drawable.folder234full,
//                scale = scale,
                scale = null,
                name = item.name,
                areShortcutsDisplayed = areShortcutsDisplayed
            )

            val infoSup = produceState<String?>(initialValue = null, item) {
                value = getInfoSup(item)
            }.value

            val infoInf = produceState<String?>(initialValue = null, item) {
                value = getInfoInf(item)
            }.value
//
            if (infoSup == null || infoInf == null) {
//                        CircularProgressIndicator()
            } else {
                val boxWidth = 45.dp

                Box(
                    modifier = Modifier.Companion
                        .align(Alignment.Companion.TopStart)
                        .graphicsLayer {
                            shape = RoundedCornerShape(
                                topStart = 8.dp,
                                bottomEnd = 8.dp
                            )
                            clip = true
                            shadowElevation = 0f
                        }
                        .background(SigmaColors.current.secondary)
                        .width(boxWidth)
                        .clickable {
                            onTopLeftPanelClick(item)
                        }
                ) {
                    // Couche 2 (Conditionnelle) : Le maillage, dessiné par-dessus le fond
//                    if (!memoEmpty) {
//                        Image(
//                            painter = painterResource(id = R.drawable.obliques4), // Remplacez par votre fichier
//                            contentDescription = "Maillage de fond",
//                            contentScale = ContentScale.Companion.Crop, // Assure que l'image remplit l'espace
//                            modifier = Modifier.Companion.matchParentSize() // Fait en sorte que l'image prenne toute la taille de la Box
//                        )
//                    }

                    Column(
                        modifier = Modifier.Companion
                            .align(Alignment.Companion.TopStart)
                            .padding(start = 0.dp, top = 0.dp)
                            .width(boxWidth)
                    ) {
                        val textHeight = 18.dp

                        Text(
                            modifier = Modifier.Companion
                                .align(Alignment.Companion.CenterHorizontally)
                                .padding(0.dp)
                                .height(textHeight),
                            text = infoSup,
//                            fontWeight = if (memoEmpty) FontWeight.Companion.ExtraLight else FontWeight.Companion
//                                .ExtraBold,
                            fontSize = 10.sp,
                            color = SigmaColors.current.onSecondary
                        )

                        Text(
                            modifier = Modifier.Companion
                                .align(Alignment.Companion.CenterHorizontally)
                                .padding(
                                    top = 0.dp, start = 0.dp, bottom = 5.dp, end = 0.dp
                                )
                                .height(textHeight),
                            text = infoInf,
//                            fontWeight = if (memoEmpty) FontWeight.Companion.ExtraLight else FontWeight.Companion.ExtraBold,
                            fontSize = 10.sp,
                            color = SigmaColors.current.onSecondary
                        )
                    }
                }
            }

            if (item.isFolder())
                Box(
                    modifier = Modifier.Companion
                        .align(Alignment.Companion.BottomEnd)
                        .padding(end = 6.dp, bottom = 20.dp)
                        .graphicsLayer {
                            rotationZ = -15f
                            shadowElevation = 4f
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                            clip = true
                        }
                        .background(
                            color = Color(0xFFCCFF00), // rouge tampons administratifs
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "DOSSIER",
//                        text = Instant.ofEpochMilli(item.modificationDate).atZone(ZoneId.systemDefault()).format(
//                            DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        fontSize = 11.sp,
                        color = Color(0xFF0047AB),
                        letterSpacing = 1.sp,
                        lineHeight = 12.sp
                    )
                }
        }

        TextSection(
            modifier = Modifier.Companion
                .height(52.dp)
                .align(Alignment.Companion.CenterHorizontally),
            name = if (item.isFile())
                item.name.substringBefore(".")
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
        textAlign = TextAlign.Companion.Center,
        lineHeight = 13.sp,
        maxLines = 3,
        fontSize = 12.sp,
        color = SigmaColors.current.onPrimary,
    )
}

@Composable
context(SigmaActivity)
fun ImageSection(
    modifier: Modifier,
    image: Any?,
    scale: ContentScale?,
    name: String,
    areShortcutsDisplayed: MutableState<Boolean>,
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
                contentScale = scale ?: ContentScale.Companion.Crop
            )
        } else {
            false // On ne montre pas le maillage avant de connaître la taille
        }
    }

    Box(
        modifier = modifier.onSizeChanged { containerSize = it }
    ) {
        if (shouldShowMesh) {
            Icon(
                painter = painterResource(id = R.drawable.diagos),
                contentDescription = null,
                modifier = Modifier.Companion.matchParentSize(),
                tint = SigmaColors.current.tertiary
            )
        }

        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(image ?: R.drawable.document2)
                .apply { if (image is Int) decoderFactory(SvgDecoder.Factory()) }
                .crossfade(false)
                .build(),
            contentDescription = "Miniature",
            contentScale = scale ?: ContentScale.Companion.Crop,
            modifier = Modifier.Companion.matchParentSize(),
            loading = { /* Affiche un loader */ },
            success = { successState ->
                val drawable = successState.result.drawable
                imageSize = IntSize(drawable.intrinsicWidth, drawable.intrinsicHeight)

                Box(
                    modifier = Modifier.Companion.matchParentSize()

                ) {
                    Image(
                        painter = successState.painter,
                        contentDescription = "Miniature",
                        contentScale = scale ?: ContentScale.Companion.Crop,
                        modifier = Modifier.Companion
                            .matchParentSize(),
                        colorFilter = if (image == null || image is Int) ColorFilter.tint(
                            SigmaColors.current.tertiary
                        ) else null
                    )

                    Shortcuts(
                        modifier = Modifier,
                        areShortcutsDisplayed = areShortcutsDisplayed,
                        name = name
                    )
                }
            },
            error = {
                // Fallback en cas d’erreur
            }
        )

    }
}

@Composable
context(BoxScope)
fun Shortcuts(
    modifier: Modifier,
    areShortcutsDisplayed: MutableState<Boolean>,
    name: String
) {
    if (areShortcutsDisplayed.value) {

        LaunchedEffect(Unit) {
            delay(3_000)
            areShortcutsDisplayed.value = false
        }

        if (areShortcutsDisplayed.value) {
            val shortcuts = name
                .substringBeforeLast(".")
                .substringAfter(".")
                .split(".")

            if (shortcuts.size != 1
                || shortcuts[0] == name
            )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.5f)) // <-- voile assombrissant
                ) {

                    Column(
                        modifier = Modifier.Companion
                            .matchParentSize()
                            .padding(top = 45.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        for (shortcut in shortcuts) {
                            Text(
                                text = shortcut,
                                color = SigmaColors.current.onPrimary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
        }
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
        ContentScale.Companion.Crop,
        ContentScale.Companion.FillBounds -> true

        ContentScale.Companion.Fit,
        ContentScale.Companion.Inside -> {
            if (imageRatio > containerRatio) {
                (containerWidth / imageRatio) >= containerHeight
            } else {
                (containerHeight * imageRatio) >= containerWidth
            }
        }

        ContentScale.Companion.FillWidth -> imageRatio <= containerRatio
        ContentScale.Companion.FillHeight -> imageRatio >= containerRatio
        ContentScale.Companion.None -> false
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
            pathEffect = PathEffect.Companion.dashPathEffect(floatArrayOf(dash, gap), 0f)
        )
    )
}