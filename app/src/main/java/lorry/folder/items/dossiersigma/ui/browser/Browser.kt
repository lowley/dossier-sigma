package lorry.folder.items.dossiersigma.ui.browser

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.ui.browser.ui.BrowserWindow
import lorry.folder.items.dossiersigma.ui.browser.utilities.BrowserState
import lorry.folder.items.dossiersigma.ui.sigma.SigmaViewModel
import javax.inject.Inject

class Browser @Inject constructor(
    val context: Context,
    override val vm: BrowserViewModel
) : IBrowser {

    @Composable
    override fun rememberBrowserState(): BrowserState {
        return rememberSaveable { BrowserState() }
    }

    ////////////
    // zoneUI //
    ////////////
    @Composable
    override fun Render() {
        val browserState by vm.state.collectAsState()

        if (browserState.isOpen)
            BrowserWindow(
                modifier = Modifier,
                browserState = browserState,
                onImageClicked = { imageUrl ->
                    browserState.onImageClicked(imageUrl)
                }
            )
    }
}

fun manageImageClick(viewModel: SigmaViewModel, imageUrl: String) {
    if (viewModel.selectedItem.value != null)
        viewModel.viewModelScope.launch {
            viewModel.updatePicture(imageUrl)
        }
}