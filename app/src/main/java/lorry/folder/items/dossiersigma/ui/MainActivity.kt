package lorry.folder.items.dossiersigma.ui

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.robertlevonyan.compose.buttontogglegroup.RowToggleButtonGroup
import dagger.hilt.android.AndroidEntryPoint
import lorry.folder.items.dossiersigma.PermissionsManager
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.SigmaApplication
import lorry.folder.items.dossiersigma.data.intent.DSI_IntentWrapper
import lorry.folder.items.dossiersigma.domain.usecases.files.ChangePathUseCase
import lorry.folder.items.dossiersigma.ui.components.Breadcrumb
import lorry.folder.items.dossiersigma.ui.components.BrowserScreen
import lorry.folder.items.dossiersigma.ui.components.ItemComponent
import lorry.folder.items.dossiersigma.ui.theme.DossierSigmaTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var intentWrapper: DSI_IntentWrapper
    
    @Inject
    lateinit var changePathUseCase: ChangePathUseCase
    
    @OptIn(ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionsManager = PermissionsManager()
        if (!permissionsManager.hasExternalStoragePermission())
            permissionsManager.requestExternalStoragePermission(this)
        val viewModel: SigmaViewModel by viewModels()
        window.navigationBarColor = ContextCompat.getColor(this, R.color.background)

//        viewModel.viewModelScope.launch(Dispatchers.IO) {
//            viewModel.initCoil(this@MainActivity)
//        }
        initializeFileIntentLauncher(viewModel)


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
                val activity = LocalContext.current as Activity

                SideEffect {
                    activity.window.statusBarColor = Color(0xFF363E4C).toArgb()
                }

                LaunchedEffect(selectedItemPicture.id) {
                    //exécuté juste après AccessingToInternetSiteForPictureUseCase/openBrowser 
                    if (selectedItemPicture.reset) {
                        viewModel.startPictureFlow()
                        return@LaunchedEffect
                    }

                    selectedItem?.let { item ->
                        viewModel.hideBrowser()
                        viewModel.goToFolder(folderState.value.fullPath, viewModel.sorting.value)
//                        viewModel.updateItemList(item.copy(picture = selectedItemPicture.picture))
                        Toast.makeText(this@MainActivity, "Changement effectué", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF363E4C))
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        AsyncImage(
                            model = R.drawable.bouger,
                            contentDescription = "Miniature",
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .size(30.dp)
                                .pointerInput(true) {
                                    detectTapGestures(
                                        onTap = {
                                            changePathUseCase.askInputFolder()
                                        }
                                    )
                                },
                        )

                        Breadcrumb(
                            items = folderState.value.fullPath.split("/").filter { it != "" },
                            onPathClick = { path ->
                                viewModel.goToFolder(
                                    path,
                                    ITEMS_ORDERING_STRATEGY.DATE_DESC
                                )
                            },
                            modifier = Modifier
                                .padding(start = 20.dp),
                            activeColor = Color(0xFF8697CB),
                            inactiveColor = Color(0xFF8697CB),
                            arrowColor = Color.Magenta,
                            transitionDuration = 200,
                        )
                        
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                        
                        RowToggleButtonGroup(
                            modifier = Modifier
                                .padding(end = 20.dp)
                                .width(200.dp)
                                .height(40.dp),
                            buttonCount = 2,
                            selectedColor = Color(0xFF8697CB),
                            unselectedColor = LightGray,
                            selectedContentColor = Color.White,
                            unselectedContentColor = DarkGray,
                            elevation = ButtonDefaults.elevatedButtonElevation(0.dp),
                            buttonIcons = arrayOf(
                                painterResource(id = R.drawable.trier_decroissant),
                                painterResource(id = R.drawable.trier_croissant),
                            ),
                            buttonTexts = arrayOf(
                                "Date",
                                "Nom",
                            ),
                            shape = RoundedCornerShape(20.dp),
                            primarySelection = 0,
                        ) { index ->
                            when (index) {
                                0 -> {
                                    viewModel.goToFolder(
                                        folderState.value.fullPath,
                                        ITEMS_ORDERING_STRATEGY.DATE_DESC
                                    )
                                }

                                1 -> {
                                    viewModel.goToFolder(
                                        folderState.value.fullPath,
                                        ITEMS_ORDERING_STRATEGY.NAME_ASC
                                    )
                                }
                            }
                        }
                    }

                    val itemIdWithVisibleMenu = remember { mutableStateOf("") }

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(150.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                            .weight(1f) // Permet au LazyVerticalGrid de prendre tout l'espace restant
                    ) {
                        lazyGridItems(folderState.value.items, key = { it.fullPath }) { item ->
                            ItemComponent(
                                viewModel = viewModel,
                                item = item,
                                modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                imageCache = viewModel.imageCache,
                                itemIdWithVisibleMenu = itemIdWithVisibleMenu

                            )
                        }
                    }

                    val isGoogle = viewModel.isGoogle.collectAsState()

                    if (isBrowserVisible)
                        Box(
                            modifier = Modifier
                        ) {
                            BrowserScreen(
                                viewModel,
                                subject = browserSearch,
                                url = if (searchIsForPersonNotMovies) SigmaApplication.INTERNET_PERSON_SITE_SEARCH else SigmaApplication.INTERNET_MOVIE_SITE_SEARCH,
                                isGoogle = isGoogle.value
                            )

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

    private fun initializeFileIntentLauncher(viewModel: SigmaViewModel) {
        val launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val pathUri = result.data?.data
                viewModel.onFolderSelected(pathUri)
            }
        intentWrapper.setLauncher(launcher as Object)
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