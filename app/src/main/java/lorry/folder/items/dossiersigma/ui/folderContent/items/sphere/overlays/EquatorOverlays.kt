package lorry.folder.items.dossiersigma.ui.folderContent.items.sphere.overlays

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import lorry.basics.getAllOf

enum class Layer { TOP, EQUATOR, BOTTOM }
interface IOverlayContent {
    @Composable
    context(BoxScope)
    fun display(
        modifier: Modifier,
        name: String
    )
}

object Equators {
    val overlays: MutableList<IOverlayContent> = mutableListOf()

//    fun getOverlays0() {
//        if (overlays.isEmpty())
//            overlays.addAll(getAllOf<Equators>())
//    }

    fun allOverlays(): List<IOverlayContent>{
        if (overlays.isEmpty())
            overlays.addAll(listOf(spanish, greek, russian, american, ethiopian, hawaiian))
        return overlays
    }

    val spanish = object: IOverlayContent{
        context(BoxScope)
        @Composable
        override fun display(modifier: Modifier, name: String) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center),
                text = "Spanish overlay #1"
            )
        }
    }

    val greek = object: IOverlayContent{
        context(BoxScope)
        @Composable
        override fun display(modifier: Modifier, name: String) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center),
                text = "Greek overlay #2"
            )
        }
    }

    val russian = object: IOverlayContent{
        context(BoxScope)
        @Composable
        override fun display(modifier: Modifier, name: String) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center),
                text = "Russian overlay #3"
            )
        }
    }

    val american = object: IOverlayContent{
        context(BoxScope)
        @Composable
        override fun display(modifier: Modifier, name: String) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center),
                text = "American overlay #4"
            )
        }
    }

    val ethiopian = object: IOverlayContent{
        context(BoxScope)
        @Composable
        override fun display(modifier: Modifier, name: String) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center),
                text = "Ethiopian overlay #5"
            )
        }
    }
}

val Equators.hawaiian: IOverlayContent
    get() = object: IOverlayContent{
        context(BoxScope)
        @Composable
        override fun display(modifier: Modifier, name: String) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center),
                text = "Spanish overlay #1"
            )
        }
    }
