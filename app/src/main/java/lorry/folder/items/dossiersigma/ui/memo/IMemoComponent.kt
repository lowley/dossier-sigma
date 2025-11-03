package lorry.folder.items.dossiersigma.ui.memo

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.basics.domain.Item

interface IMemoComponent {

    val isDisplayingMemo: StateFlow<Boolean>
    fun isDisplayed(): Boolean
    fun closeMemo()
    fun toggleIsDisplayed()

    @Composable
    context(BoxScope)
    fun Render(selectedItem: Item?, setSelectedItem: (Item?) -> Unit)

}