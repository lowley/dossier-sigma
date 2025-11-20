package lorry.folder.items.dossiersigma.ui.folderContent.items.sphere

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.basics.domain.Item
import lorry.folder.items.dossiersigma.external.capsule.utilities.Country
import lorry.folder.items.dossiersigma.external.capsule.utilities.CountryName
import lorry.folder.items.dossiersigma.ui.sigma.SigmaColors

context(BoxScope)
@Composable
fun BackgroundContent(
    modifier: Modifier.Companion,
    item: Item,
    image: Any?,
    scale: ContentScale?,
    areShortcutsDisplayed: MutableState<Boolean>,
    getInfoSup: suspend (Item) -> String?,
    getInfoInf: suspend (Item) -> String?,
    onTopLeftPanelClick: (Item) -> Unit,
    memoEmpty: Boolean
) {

    ImageSection(
        modifier = modifier
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
        scale = scale,
//                scale = null,
        name = item.name,
        areShortcutsDisplayed = areShortcutsDisplayed
    )

    //Ajout à l'image
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
        val shapeForInsert = RoundedCornerShape(
            topStart = 8.dp,
            bottomEnd = 8.dp
        )

        //l'ajout à l'image proprement dit: encart supérieur gauche
        Box(
            modifier = modifier
                .align(Alignment.Companion.TopStart)
                .graphicsLayer {
                    shape = shapeForInsert
                    clip = true
                    shadowElevation = 0f
                }
                .background(SigmaColors.current.secondary)
                .width(boxWidth)
                .border(
                    1.dp,
                    lerp(
                        SigmaColors.current.secondary,
                        SigmaColors.current.primary,
                        0.5f
                    ),
                    shape = shapeForInsert
                )
                .clickable {
                    onTopLeftPanelClick(item)
                }
        ) {
//                     Couche 2 (Conditionnelle) : Le maillage, dessiné par-dessus le fond
            if (!memoEmpty) {
                Image(
                    painter = painterResource(id = R.drawable.obliques4), // Remplacez par votre fichier
                    contentDescription = "Maillage de fond",
                    contentScale = ContentScale.Companion.Crop, // Assure que l'image remplit l'espace
                    modifier = modifier.matchParentSize() // Fait en sorte que l'image prenne toute la taille de la Box
                )
            }

            Column(
                modifier = modifier
                    .align(Alignment.Companion.TopStart)
                    .padding(start = 0.dp, top = 0.dp)
                    .width(boxWidth)
            ) {
                val textHeight = 18.dp

                Text(
                    modifier = modifier
                        .align(Alignment.Companion.CenterHorizontally)
                        .padding(0.dp)
                        .height(textHeight),
                    text = infoSup,
                    fontWeight = if (memoEmpty) FontWeight.Companion.Normal else FontWeight.Companion
                        .ExtraBold,
                    fontSize = 10.sp,
                    color = SigmaColors.current.onSecondary
                )

                Text(
                    modifier = modifier
                        .align(Alignment.Companion.CenterHorizontally)
                        .padding(
                            top = 0.dp, start = 0.dp, bottom = 5.dp, end = 0.dp
                        )
                        .height(textHeight),
                    text = infoInf,
                    fontWeight = if (memoEmpty) FontWeight.Companion.Normal else FontWeight.Companion.ExtraBold,
                    fontSize = 10.sp,
                    color = SigmaColors.current.onSecondary
                )
            }
        }
    }

    //2e encart: si répertoire: "DOSSIER"
    if (item.isFolder())
        Box(
            modifier = modifier
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

    if ("spain" == "spain")
        Box(
            modifier = modifier
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
            CountryFlag(
                modifier = Modifier,
                country = item.country
            )
        }


}

context(BoxScope)
@Composable
fun CountryFlag(
    country: Country?,
    modifier: Modifier = Modifier
        .align(Alignment.Companion.BottomStart)
        .padding(start = 10.dp, bottom = 10.dp),
    contentDescription: CountryName? = country?.first
) {
    val resId = country?.second

    if (resId != null) {
        Image(
            painter = rememberAsyncImagePainter(model = resId),
            contentDescription = contentDescription,
            modifier = modifier
                .size(width = 20.dp, height = 15.dp)
        )
    } else {
        // Option : afficher un placeholder, ou rien
    }
}

@Composable
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
                .data(image ?: R.drawable.fichier4)
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
                            lerp(
                                SigmaColors.current.secondary,
                                SigmaColors.current.primary,
                                0.8f
                            )
                        )
                        else null
                    )
                }
            },
            error = {
                // Fallback en cas d’erreur
            }
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