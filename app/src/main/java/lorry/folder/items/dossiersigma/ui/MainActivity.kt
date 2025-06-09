package lorry.folder.items.dossiersigma.ui

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.PermissionsManager
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.data.intent.DSI_IntentWrapper
import lorry.folder.items.dossiersigma.domain.usecases.files.ChangePathUseCase
import lorry.folder.items.dossiersigma.domain.usecases.homePage.HomeViewModel
import lorry.folder.items.dossiersigma.ui.components.Breadcrumb
import lorry.folder.items.dossiersigma.ui.components.BrowserOverlay
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
        val mainViewModel: SigmaViewModel by viewModels()
        val homeViewModel: HomeViewModel by viewModels()

        window.navigationBarColor = ContextCompat.getColor(this, R.color.background)

//        viewModel.viewModelScope.launch(Dispatchers.IO) {
//            viewModel.initCoil(this@MainActivity)
//        }
        initializeFileIntentLauncher(mainViewModel)

        setContent {
            DossierSigmaTheme {
                //barre d'outils

                val state = rememberScrollState()
                val currentFolder by mainViewModel.currentFolder.collectAsState()
                val selectedItem by mainViewModel.selectedItem.collectAsState()
                val activity = LocalContext.current as Activity
                val pictureUpdateId by mainViewModel.pictureUpdateId.collectAsState()
                val homePageVisible by homeViewModel.homePageVisible.collectAsState()

                SideEffect {
                    activity.window.statusBarColor = Color(0xFF363E4C).toArgb()
                }

                BackHandler(enabled = true) {
                    mainViewModel.setSorting(ITEMS_ORDERING_STRATEGY.DATE_DESC)
                    mainViewModel.removeLastFolderPathHistory()
                }

                LaunchedEffect(pictureUpdateId) {
                    //exécuté juste après AccessingToInternetSiteForPictureUseCase/openBrowser 
//                    if (selectedItemPicture.reset) {
//                        viewModel.startPictureFlow()
//                        return@LaunchedEffect
//                    }

                    selectedItem?.let { item ->
                        mainViewModel.browserManager.closeBrowser()
                        mainViewModel.goToFolder(currentFolder.fullPath, mainViewModel.sorting.value)
//                        viewModel.updateItemList(item.copy(picture = selectedItemPicture.picture))
                        Toast.makeText(this@MainActivity, "Changement d'image effectué", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF363E4C))
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val sortingWidth = 200.dp

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = sortingWidth),
                        ) {
                            AsyncImage(
                                model = R.drawable.bouger,
                                contentDescription = "Miniature",
                                modifier = Modifier
                                    .padding(horizontal = 5.dp)
                                    .align(Alignment.CenterVertically)
                                    .size(30.dp)
                                    .pointerInput(true) {
                                        detectTapGestures(
                                            onTap = {
//                                                changePathUseCase.askInputFolder()
                                                    homeViewModel.setHomePageVisible(!homePageVisible)

                                            }
                                        )
                                    },
                            )

                            if (!homePageVisible)
                                Breadcrumb(
                                    items = currentFolder.fullPath.split("/").filter { it != "" },
                                    onPathClick = { path ->
                                        mainViewModel.goToFolder(
                                            path,
                                            ITEMS_ORDERING_STRATEGY.DATE_DESC
                                        )
                                    },
                                    modifier = Modifier
                                        .padding(start = 20.dp)
                                        .align(Alignment.CenterVertically),
                                    activeColor = Color(0xFF8697CB),
                                    inactiveColor = Color(0xFF8697CB),
                                    arrowColor = Color.Magenta,
                                    transitionDuration = 200,
                                )
                        }

                        if (homePageVisible) {
                            Text(
                                text = "truc",
                                color = Color.White
                            )

                        } else
                            Row(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .width(sortingWidth),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                val sorting by mainViewModel.sorting.collectAsState()

                                FilterChip(
                                    label = { Text("Date") },
                                    modifier = Modifier
                                        .padding(end = 5.dp)
                                        .align(Alignment.CenterVertically),
                                    selected = sorting == ITEMS_ORDERING_STRATEGY.DATE_DESC,
                                    leadingIcon = {
                                        if (sorting == ITEMS_ORDERING_STRATEGY.DATE_DESC)
                                            Icon(
                                                painterResource(id = R.drawable.trier_decroissant),
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = Color.Red
                                            )
                                        else
                                            Icon(
                                                painterResource(id = R.drawable.trier_decroissant),
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                            )
                                    },
//                            enabled = sorting == ITEMS_ORDERING_STRATEGY.NAME_ASC,
                                    onClick = {
                                        mainViewModel.goToFolder(
                                            currentFolder.fullPath,
                                            ITEMS_ORDERING_STRATEGY.DATE_DESC
                                        )
                                    }
                                )

                                FilterChip(
                                    label = { Text("Nom") },
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically),
                                    selected = sorting == ITEMS_ORDERING_STRATEGY.NAME_ASC,
                                    leadingIcon = {
                                        if (sorting == ITEMS_ORDERING_STRATEGY.NAME_ASC)
                                            Icon(
                                                painterResource(id = R.drawable.trier_croissant),
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = Color.Red
                                            )
                                        else
                                            Icon(
                                                painterResource(id = R.drawable.trier_croissant),
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                            )
                                    },
//                            enabled = sorting == ITEMS_ORDERING_STRATEGY.DATE_DESC,
                                    onClick = {
                                        mainViewModel.goToFolder(
                                            currentFolder.fullPath,
                                            ITEMS_ORDERING_STRATEGY.NAME_ASC
                                        )
                                    }
                                )

//                        RowToggleButtonGroup(
//                            modifier = Modifier
//                                .padding(end = 20.dp)
//                                .width(200.dp)
//                                .height(40.dp),
//                            buttonCount = 2,
//                            selectedColor = Color(0xFF8697CB),
//                            unselectedColor = LightGray,
//                            selectedContentColor = Color.White,
//                            unselectedContentColor = DarkGray,
//                            elevation = ButtonDefaults.elevatedButtonElevation(0.dp),
//                            buttonIcons = arrayOf(
//                                painterResource(id = R.drawable.trier_decroissant),
//                                painterResource(id = R.drawable.trier_croissant),
//                            ),
//                            buttonTexts = arrayOf(
//                                "Date",
//                                "Nom",
//                            ),
//                            shape = RoundedCornerShape(20.dp),
//                            primarySelection = 0
//                        ) { index ->
//                            when (index) {
//                                0 -> {
//                                    viewModel.goToFolder(
//                                        currentFolder.fullPath,
//                                        ITEMS_ORDERING_STRATEGY.DATE_DESC
//                                    )
//                                }
//
//                                1 -> {
//                                    viewModel.goToFolder(
//                                        currentFolder.fullPath,
//                                        ITEMS_ORDERING_STRATEGY.NAME_ASC
//                                    )
//                                }
//                            }
//                        }
                            }
                    }

                    val itemIdWithVisibleMenu = remember { mutableStateOf("") }

                    if (homePageVisible) {
                        Text(
                            text = "machin",
                            color = Color.White
                        )
                    } else
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(150.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp)
                                .weight(1f)
                        ) {
                            lazyGridItems(currentFolder.items, key = { it.fullPath }) { item ->

                                ItemComponent(
                                    viewModel = mainViewModel,
                                    item = item,
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    imageCache = mainViewModel.imageCache,
                                    itemIdWithVisibleMenu = itemIdWithVisibleMenu,
                                    context = this@MainActivity,
                                    scaleCache = mainViewModel.scaleCache
                                )
                            }
                        }

                    if (!homePageVisible)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.Black)
                                .height(48.dp)
                        ) {
                            Text("Ceci est un grand pas pour Sigma")
                        }

                    val url by mainViewModel.browserManager.currentPage.collectAsState()

                    BrowserOverlay(
                        currentPage = url,
                        onClose = {
                            mainViewModel.browserManager.closeBrowser()
                        },
                        onImageClicked = { url ->
                            //println("hasImage: $url")
                            manageImageClick(mainViewModel, url)
                        },
                        viewmodel = mainViewModel
                    )
                }
            }
        }
    }

    fun manageImageClick(viewModel: SigmaViewModel, imageUrl: String) {
        if (viewModel.selectedItem.value != null)
            viewModel.viewModelScope.launch {
                viewModel.updatePicture(imageUrl)
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