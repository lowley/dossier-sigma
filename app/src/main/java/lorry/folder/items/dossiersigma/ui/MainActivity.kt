package lorry.folder.items.dossiersigma.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import lorry.folder.items.dossiersigma.ui.theme.DossierSigmaTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import lorry.folder.items.dossiersigma.PermissionsManager
import lorry.folder.items.dossiersigma.SigmaApplication
import lorry.folder.items.dossiersigma.ui.components.Breadcrumb
import lorry.folder.items.dossiersigma.ui.components.BrowserScreen
import lorry.folder.items.dossiersigma.ui.components.ItemComponent

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

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
                val browserSearch by viewModel.browserSearch.collectAsState()
                val selectedItemPicture by viewModel.selectedItemPicture.collectAsState()
                val searchIsForPersonNotMovies by viewModel.searchIsForPersonNotMovies
                    .collectAsState()
                val selectedItem by viewModel.selectedItem.collectAsState()
                
                LaunchedEffect(selectedItemPicture.id) {
                    //exécuté juste après AccessingToInternetSiteForPictureUseCase/openBrowser 
                    if (selectedItemPicture.reset) {
                        viewModel.startPictureFlow()
                        return@LaunchedEffect
                    }

                    selectedItem?.let { item ->
                        viewModel.hideBrowser()
                        viewModel.updateItemList(item.copy(picture = selectedItemPicture.picture))
                        Toast.makeText(this@MainActivity, "Changement effectué", Toast.LENGTH_SHORT)
                            .show()
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
                            ItemComponent(
                                context = this@MainActivity,
                                viewModel = viewModel,
                                item = item
                            )
                        }
                    }

                    if (isBrowserVisible)
                        Box(
                            modifier = Modifier
                        ){
                            BrowserScreen(viewModel, 
                                subject = browserSearch,
                                url = if (searchIsForPersonNotMovies) SigmaApplication.INTERNET_PERSON_SITE_SEARCH else SigmaApplication.INTERNET_MOVIE_SITE_SEARCH)
                            
                            Button(
                                modifier = Modifier
                                    .align(Alignment.TopCenter),
                                onClick = { viewModel.hideBrowser() }
                            ) {
                                Text("Fermer le navigateur sans copier d'image")
                            }
                        }
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