package lorry.folder.items.dossiersigma.ui.memo

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity

interface IMemoComponent {

    fun isDisplayed(): Boolean
    fun closeMemo()
    fun toggleIsDisplayed()

    @Composable
    context(SigmaActivity, BoxScope)
    fun Render()

}