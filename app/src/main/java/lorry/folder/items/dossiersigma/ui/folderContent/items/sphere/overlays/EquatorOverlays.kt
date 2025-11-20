package lorry.folder.items.dossiersigma.ui.folderContent.items.sphere.overlays

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import lorry.basics.getAll
import lorry.folder.items.dossiersigma.ui.folderContent.items.sphere.OverlayContent

sealed class Equator{
    val overlays: List<OverlayContent> = listOf()

    fun getOverlays(){
        overlays.plus(getAll(Equator::class))
    }
}

val Equator.spanish: OverlayContent
    get() = {
        Text(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            text = "Spanish overlay #1"
        )
    }

val Equator.greek: OverlayContent
    get() = {
        Text(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            text = "Greek overlay #2"
        )
    }

val Equator.russian: OverlayContent
    get() = {
        Text(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            text = "Russian overlay #3"
        )
    }

val Equator.american: OverlayContent
    get() = {
        Text(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            text = "American overlay #4"
        )
    }

val Equator.ethiopian: OverlayContent
    get() = {
        Text(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            text = "Ethiopian overlay #5"
        )
    }



