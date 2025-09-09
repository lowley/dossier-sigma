package lorry.folder.items.dossiersigma.UI.memo

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.UI.sigma.SigmaActivity

interface IMemoComponent {

    val isDisplayingMemo: StateFlow<Boolean>
    fun isDisplayed(): Boolean
    fun closeMemo()
    fun toggleIsDisplayed()

    @Composable
    context(SigmaActivity, BoxScope)
    fun Render()

}