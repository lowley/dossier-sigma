package lorry.folder.items.dossiersigma.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import lorry.folder.items.dossiersigma.ui.theme.DossierSigmaTheme
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import lorry.folder.items.dossiersigma.GlobalStateManager
import lorry.folder.items.dossiersigma.PermissionsManager
import lorry.folder.items.dossiersigma.SigmaApplication
import lorry.folder.items.dossiersigma.ui.components.Breadcrumb
import lorry.folder.items.dossiersigma.ui.components.BrowserScreen
import lorry.folder.items.dossiersigma.ui.components.ItemComponent
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var globalStateManager: GlobalStateManager
    
    @OptIn(ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionsManager = PermissionsManager()
        if (!permissionsManager.hasExternalStoragePermission())
            permissionsManager.requestExternalStoragePermission(this)

        val viewModel: SigmaViewModel by viewModels()
        
        setContent {
            DossierSigmaTheme {
                //barre d'outils

                val state = rememberScrollState()
                val folderState = viewModel.folder.collectAsState()
                val isBrowserVisible by viewModel.isBrowserVisible.collectAsState()
                val browserPersonSearch by viewModel.browserPersonSearch.collectAsState()
                val selectedItem by globalStateManager.selectedItem.collectAsState()

                LaunchedEffect(selectedItem) {
                    selectedItem?.let { item ->
                        if (!globalStateManager.doNotTriggerChange) {
                            viewModel.hideBrowser()
                            viewModel.updateItemList(item)
                            globalStateManager.setSelectedItem(null)
                            Toast.makeText(this@MainActivity, "Changement effectué", Toast.LENGTH_SHORT).show()
                        }
                        else
                            globalStateManager.doNotTriggerChange = false
                    }
                }
                
                Column(modifier = Modifier.fillMaxSize()) {
                    Breadcrumb(
                        items = folderState.value.fullPath.split("/"),
                        onPathClick = { path -> viewModel.goToFolder(path) },
                        modifier = Modifier,
                        activeColor = Color.Red,
                        inactiveColor = Color.Gray,
                        arrowColor = Color.Magenta,
                        transitionDuration = 7000,
                        arrowTransitionSpeed = 7000
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(120.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // Permet au LazyVerticalGrid de prendre tout l'espace restant
                    ) {
                        lazyGridItems(folderState.value.items, key = { it.id }) { item ->
                            ItemComponent(context = this@MainActivity, viewModel = viewModel, item = item)
                        }
                    }

                    if (isBrowserVisible)
                        BrowserScreen(globalStateManager, browserPersonSearch)
                }
            }
        }
    }
}

fun <T> LazyGridScope.lazyGridItems(
    items: List<T>,
    key: ((T) -> Any)? = null,
    itemContent: @Composable (T) -> Unit
) {
    itemsIndexed(items, key = { index, item -> key?.invoke(item) ?: index }) { _, item ->
        itemContent(item)
    }
}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    DossierSigmaTheme {
//        
//    }
//}