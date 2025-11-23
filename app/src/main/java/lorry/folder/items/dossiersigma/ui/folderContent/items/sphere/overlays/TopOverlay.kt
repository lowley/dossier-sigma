package lorry.folder.items.dossiersigma.ui.folderContent.items.sphere.overlays

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import lorry.folder.items.dossiersigma.basics.domain.SigmaPath
import lorry.folder.items.dossiersigma.external.capsule.utilities.Country
import lorry.folder.items.dossiersigma.external.capsule.utilities.CountryFrench
import lorry.folder.items.dossiersigma.external.capsule.utilities.CountryName
import lorry.folder.items.dossiersigma.external.capsule.utilities.CountryPicture

@Composable
fun TopOverlay(
    modifier: Modifier = Modifier,
    name: String
): IOverlayContent = object: IOverlayContent{
    override val country: Country?
        get() = null

    context(BoxScope)
    @Composable
    override fun display(
        modifier: Modifier,
        name: String,
        country: Country?,
        fullPAth: SigmaPath?
    ) {
        Text(
            modifier = modifier
                .fillMaxSize()
                .align(Alignment.Center),
            text = "Top Overlay"
        )
    }

}