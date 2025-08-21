package lorry.folder.items.dossiersigma.ui.browser

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import lorry.folder.items.dossiersigma.headless.domain.Item
import lorry.folder.items.dossiersigma.ui.browser.utilities.BrowserState
import lorry.folder.items.dossiersigma.ui.browser.utilities.BrowserTarget

interface IBrowser {

    val vm: BrowserViewModel

    @Composable
    fun Render(modifier: Modifier)

    @Composable
    fun rememberBrowserState(): BrowserState

}

// Extension utilitaire (pas override ⇒ défauts autorisés)
fun IBrowser.changeState(
    isOpen: Boolean = vm.state.value.isOpen,
    item: Item? = vm.state.value.item,
    target: BrowserTarget? = vm.state.value.target,
    canGoBack: Boolean = vm.state.value.canGoBack,
    canGoForward: Boolean = vm.state.value.canGoForward,
    onImageClicked: (String) -> Unit = vm.state.value.onImageClicked,
) = vm.changeState(isOpen, item, target, canGoBack, canGoForward, onImageClicked)
