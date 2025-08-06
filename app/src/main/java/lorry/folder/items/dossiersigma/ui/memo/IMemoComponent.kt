package lorry.folder.items.dossiersigma.ui.memo

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity

interface IMemoComponent {

    val isDisplayingMemo: StateFlow<Boolean>
    fun isDisplayed(): Boolean
    fun closeMemo()
    fun toggleIsDisplayed()

    @Composable
    context(SigmaActivity, BoxScope)
    fun Render()

}