package lorry.folder.items.dossiersigma.ui.folderContent.IndexBar

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity

interface IIndexBar {

    @Composable
    context(BoxScope, SigmaActivity)
    fun display(currentScrollState: LazyGridState)

}