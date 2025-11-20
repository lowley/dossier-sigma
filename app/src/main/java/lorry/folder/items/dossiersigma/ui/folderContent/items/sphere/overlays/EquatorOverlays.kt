package lorry.folder.items.dossiersigma.ui.folderContent.items.sphere.overlays

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import lorry.folder.items.dossiersigma.external.capsule.utilities.Country
import lorry.folder.items.dossiersigma.external.capsule.utilities.CountryFrench
import lorry.folder.items.dossiersigma.external.capsule.utilities.produceCountry

enum class Layer { TOP, EQUATOR, BOTTOM }
interface IOverlayContent {
    context(BoxScope)
    @Composable
    fun display(
        modifier: Modifier,
        name: String,
        country: Country?
    )
}

object Equators {
    val overlays: MutableList<IOverlayContent> = mutableListOf()

    fun allOverlays(): List<IOverlayContent> {
        if (overlays.isEmpty()) {
            val countryOverlays1 = listOf(
                "usa",
                "Spain",
                "italy",
                "south_america",
                "uk",
                "oceania",
                "africa",
                "slavish",
                "middle east",
                "asia",
                "greece",
                "israel",
                "cuba",
                "australia",
            )
                .map { it.produceCountry().let { overlayContent(it) } }
                .toMutableList()

            countryOverlays1.addFirst(nothing)
            overlays.addAll(countryOverlays1)
        }

        return overlays
    }

    fun overlayContent(overlayCountry: Country): IOverlayContent = object : IOverlayContent {
        context(BoxScope)
        @Composable
        override fun display(modifier: Modifier, name: String, country: Country?) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(IntrinsicSize.Min)
//                        .height(IntrinsicSize.Min)
                ) {

                    if (overlayCountry.third != null)
                        Flag(
                            resId = overlayCountry.third!!,
                            contentDescription = overlayCountry.second
                        )

                    Text(
                        modifier = modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        color = Color(0xFFCCCCCC),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        text = overlayCountry.second ?: "???"
                    )
                }
            }
        }
    }


    val nothing = object : IOverlayContent {
        context(BoxScope)
        @Composable
        override fun display(modifier: Modifier, name: String, country: Country?) {

        }
    }

    @Composable
    fun Flag(resId: Int, contentDescription: String?) {
        AsyncImage(
            model = resId,
            modifier = Modifier
                .width(100.dp)
                .height(80.dp),
            contentDescription = contentDescription
        )
    }
}