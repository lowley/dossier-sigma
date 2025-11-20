package lorry.folder.items.dossiersigma.ui.folderContent.items.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import lorry.folder.items.dossiersigma.basics.domain.Item
import lorry.folder.items.dossiersigma.basics.domain.SigmaPath
import lorry.folder.items.dossiersigma.basics.domain.str
import lorry.folder.items.dossiersigma.ui.folderContent.items.sphere.BackgroundContent
import lorry.folder.items.dossiersigma.ui.folderContent.items.sphere.SphericOverlayedBox
import lorry.folder.items.dossiersigma.ui.folderContent.items.sphere.overlays.BottomOverlay
import lorry.folder.items.dossiersigma.ui.folderContent.items.sphere.overlays.Equators
import lorry.folder.items.dossiersigma.ui.folderContent.items.sphere.overlays.IOverlayContent
import lorry.folder.items.dossiersigma.ui.folderContent.items.sphere.overlays.TopOverlay
import lorry.folder.items.dossiersigma.ui.sigma.DragState
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity
import lorry.folder.items.dossiersigma.ui.sigma.SigmaColors
import java.io.File
import java.io.FileOutputStream

@Composable
context(SigmaActivity)
fun ItemComponent(
    modifier: Modifier,
    item: Item,
    onHoveredNotHovered: (Item?) -> Unit,
    selectedItemFullPath: StateFlow<SigmaPath?>,
    onItemTapped: ((Item) -> Unit),
    onItemLongPressed: ((Item) -> Unit),
    onTopLeftPanelClick: (Item) -> Unit,
    getInfoSup: suspend (Item) -> String?,
    getInfoInf: suspend (Item) -> String?,
    dragState: StateFlow<DragState?>,

    ) {

    val memo = item.memo
    val memoEmpty = memo?.isEmpty() ?: true

    val tag = item.tag
    val scale = item.scale

    val image by mainViewModel.folderContentComponent.currentFolderFlow
        .map { folder -> folder?.picture }
        .collectAsState(initial = null)

    val imageHeight = 160.dp

    var bounds = remember { mutableStateOf<Rect?>(null) }
    val state by dragState.collectAsState()

    val isHovered = remember(state, bounds) {
        if (state != null && bounds != null && state?.offset != null)
            state != null && bounds.value?.contains(state?.offset!!) == true
        else false
    }

    LaunchedEffect(isHovered) {
        if (isHovered)
            onHoveredNotHovered(item)
        else
            onHoveredNotHovered(null)
    }

    // dans l'item: Column contient une image au dessus d'un texte
    Column {
        val shape1 = RoundedCornerShape(8.dp)
        val isSelectedItemState by selectedItemFullPath
            .map { it?.equalsTo(item.fullPath.str) == true }
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

        //l'image dans l'item
        val backgroundContent = object : IOverlayContent {
            context(BoxScope)
            @Composable
            override fun display(modifier: Modifier, name: String) {
                BackgroundContent(
                    modifier = Modifier,
                    item = item,
                    image = image,
                    scale = scale,
                    areShortcutsDisplayed = areShortcutsDisplayed,
                    getInfoSup = getInfoSup,
                    getInfoInf = getInfoInf,
                    onTopLeftPanelClick = onTopLeftPanelClick,
                    memoEmpty = memoEmpty,
                )
            }
        }

        SphericOverlayedBox(
            modifier = modifierWithBorder,
            backgroundContent = backgroundContent,
            topOverlay = TopOverlay(Modifier, item.name),
            equatorOverlays = Equators.allOverlays(),
            bottomOverlay = BottomOverlay(Modifier, item.name),
            isHovered = isHovered,
            length = imageHeight,
            bounds = bounds,
            item = item
        )

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