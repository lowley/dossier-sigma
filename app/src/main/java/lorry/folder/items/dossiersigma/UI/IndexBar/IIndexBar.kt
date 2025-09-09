package lorry.folder.items.dossiersigma.UI.IndexBar

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import lorry.folder.items.dossiersigma.UI.sigma.SigmaActivity

interface IIndexBar {

    @Composable
    context(BoxScope, SigmaActivity)
    fun display(currentScrollState: LazyGridState)



}