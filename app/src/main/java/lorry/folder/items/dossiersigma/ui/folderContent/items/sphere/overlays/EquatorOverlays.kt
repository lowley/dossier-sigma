package lorry.folder.items.dossiersigma.ui.folderContent.items.sphere.overlays

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.basics.domain.SigmaPath
import lorry.folder.items.dossiersigma.basics.domain.toSigmaPath
import lorry.folder.items.dossiersigma.external.capsule.CapsuleComponent
import lorry.folder.items.dossiersigma.external.capsule.utilities.Country
import lorry.folder.items.dossiersigma.external.capsule.utilities.CountryClass
import lorry.folder.items.dossiersigma.external.capsule.utilities.CountryFrench
import lorry.folder.items.dossiersigma.external.capsule.utilities.produceCountry
import lorry.folder.items.dossiersigma.ui.sigma.SigmaColors

enum class Layer { TOP, EQUATOR, BOTTOM }
interface IOverlayContent {
    val country: Country?

    context(BoxScope)
    @Composable
    fun display(
        modifier: Modifier,
        name: String,
        country: Country?,
        fullPath: SigmaPath?
    )
}

object Equators {
    val overlays: MutableList<IOverlayContent> = mutableListOf()

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun allOverlays(): List<IOverlayContent> {
        if (overlays.isEmpty()) {
            val countryOverlays1 = listOf(
                "usa",
                "spain",
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
            countryOverlays1.add(shortcuts)
            overlays.addAll(countryOverlays1)
        }

        return overlays
    }

    fun overlayContent(overlayCountry: Country): IOverlayContent = object : IOverlayContent {
        override val country: Country?
            get() = overlayCountry

        context(BoxScope)
        @Composable
        override fun display(modifier: Modifier, name: String, country: Country?, fullPath: SigmaPath?) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable {
                        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
                        if (fullPath == null)
                            return@clickable

                        scope.launch {
                            val capsuleMgr = CapsuleComponent()
                            capsuleMgr.save(
                                CountryClass(country),
                                fullPath
                            )
                        }
                    }
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
        override val country: Country?
            get() = null

        context(BoxScope)
        @Composable
        override fun display(modifier: Modifier, name: String, country: Country?, fullPath: SigmaPath?) {

        }
    }

    val shortcuts = object : IOverlayContent {
        override val country: Country?
            get() = null

        context(BoxScope)
        @Composable
        override fun display(modifier: Modifier, name: String, country: Country?, fullPath: SigmaPath?) {
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
                )
                {

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