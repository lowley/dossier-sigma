package lorry.folder.items.dossiersigma.ui.browser

import android.content.Context
import android.content.ContextWrapper
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.ui.browser.ui.BrowserWindow
import lorry.folder.items.dossiersigma.ui.browser.utilities.BrowserState
import lorry.folder.items.dossiersigma.ui.sigma.SigmaViewModel
import javax.inject.Inject

class Browser @Inject constructor(
    @ActivityContext private val context: Context
) : IBrowser {

    override val vm: BrowserViewModel by lazy {
        val activity = context.findActivity()
        ViewModelProvider(activity)[BrowserViewModel::class.java]
    }

    @Composable
    override fun rememberBrowserState(): BrowserState {
        return remember { BrowserState() }
    }

    ////////////
    // zoneUI //
    ////////////
    @Composable
    override fun Render(modifier: Modifier) {
        val browserState by vm.state.collectAsState()

        if (browserState.isOpen)
            BrowserWindow(
                modifier = modifier,
                browserState = browserState,
                onImageClicked = { imageUrl ->
                    browserState.onImageClicked(imageUrl)
                },
                setCanGoBack = { value ->
                    vm.changeState(
                        canGoBack = value
                    )
                },
                setCanGoForward = { value ->
                    vm.changeState(
                        canGoForward = value
                    )
                },
                closeBrowser = {
                    vm.close()
                }
            )
    }

    private fun Context.findActivity(): ComponentActivity =
        generateSequence(this) { (it as? ContextWrapper)?.baseContext }
            .filterIsInstance<ComponentActivity>()
            .firstOrNull()
            ?: error("Browser attend un @ActivityContext ; vérifie le scope et l’annotation.")
}

fun manageImageClick(viewModel: SigmaViewModel, imageUrl: String) {
    if (viewModel.selectedItem.value != null)
        viewModel.viewModelScope.launch {
            viewModel.updatePicture(imageUrl)
            SigmaViewModel.requestRefresh()

        }
}

