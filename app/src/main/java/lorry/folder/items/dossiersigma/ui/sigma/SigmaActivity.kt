package lorry.folder.items.dossiersigma.ui.sigma

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.ButtonColors
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import com.elixer.palette.Presets
import com.elixer.palette.composables.Palette
import com.elixer.palette.constraints.HorizontalAlignment
import com.elixer.palette.constraints.VerticalAlignment
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import de.charlex.compose.BottomAppBarSpeedDialFloatingActionButton
import de.charlex.compose.rememberSpeedDialFloatingActionButtonState
import dev.materii.pullrefresh.PullRefreshLayout
import dev.materii.pullrefresh.rememberPullRefreshState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.PermissionsManager
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.data.intent.DSI_IntentWrapper
import lorry.folder.items.dossiersigma.domain.services.MoveFileService
import lorry.folder.items.dossiersigma.domain.usecases.files.ChangePathUseCase
import lorry.folder.items.dossiersigma.domain.usecases.homePage.HomeItem
import lorry.folder.items.dossiersigma.domain.usecases.homePage.HomeViewModel
import lorry.folder.items.dossiersigma.ui.components.BottomTools
import lorry.folder.items.dossiersigma.ui.components.Breadcrumb
import lorry.folder.items.dossiersigma.ui.components.BrowserOverlay
import lorry.folder.items.dossiersigma.ui.components.CustomMoveFileExistingDestinationDialog
import lorry.folder.items.dossiersigma.ui.components.CustomTextDialog
import lorry.folder.items.dossiersigma.ui.components.CustomYesNoDialog
import lorry.folder.items.dossiersigma.ui.components.FolderChooserDialog
import lorry.folder.items.dossiersigma.ui.components.HomeItemDialog
import lorry.folder.items.dossiersigma.ui.components.HomeItemInfos
import lorry.folder.items.dossiersigma.ui.components.TagInfos
import lorry.folder.items.dossiersigma.ui.components.TagInfosDialog
import lorry.folder.items.dossiersigma.ui.components.Tools
import lorry.folder.items.dossiersigma.ui.components.Tools.DEFAULT
import lorry.folder.items.dossiersigma.ui.home.homePage
import lorry.folder.items.dossiersigma.ui.memoEditor.MemoEditor
import lorry.folder.items.dossiersigma.ui.normal.NormalPage
import lorry.folder.items.dossiersigma.ui.settings.SettingsViewModel
import lorry.folder.items.dossiersigma.ui.settings.settingsPage
import lorry.folder.items.dossiersigma.ui.theme.DossierSigmaTheme
import javax.inject.Inject
import kotlin.collections.distinctBy


@AndroidEntryPoint
class SigmaActivity : ComponentActivity() {

    companion object {
        val TAG = "MainActivity"
        val FILE_REQUEST_CODE = 969
    }

    @Inject
    lateinit var intentWrapper: DSI_IntentWrapper

    @Inject
    lateinit var changePathUseCase: ChangePathUseCase

    val mainViewModel: SigmaViewModel by viewModels()
    val homeViewModel: HomeViewModel by viewModels()
    val settingsViewModel: SettingsViewModel by viewModels()

    /**
     * Appelée par la boîte de dialogue de création / modification de HomeItem
     * @see HomeItemDialog
     * @see FolderChooserDialog
     */
    var onFolderChosen: (String?) -> Unit = {}
    var onGotBrowserImage: (String) -> Unit = {}

    val sigmaActivity = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionsManager = PermissionsManager()
        if (!permissionsManager.hasExternalStoragePermission())
            permissionsManager.requestExternalStoragePermission(this)

        window.navigationBarColor = ContextCompat.getColor(this, R.color.background)
        initializeFileIntentLauncher(mainViewModel)

        BottomTools.viewModel = mainViewModel
        BottomTools.observeDefaultContent(mainViewModel)

        setContent {
            val isTextDialogVisible by mainViewModel.isTextDialogVisible.collectAsState()
            val isYesNoDialogVisible by mainViewModel.isYesNoDialogVisible.collectAsState()
            val isMoveFileDialogVisible by mainViewModel.isMoveFileDialogVisible.collectAsState()
            val isTagInfosDialogVisible by mainViewModel.isTagInfosDialogVisible.collectAsState()
            val isHomeItemDialogVisible by mainViewModel.isHomeItemDialogVisible.collectAsState()
            val isFilePickerVisible by mainViewModel.isFilePickerVisible.collectAsState()

            val homePageVisible by homeViewModel.homePageVisible.collectAsState()

            val fabState = rememberSpeedDialFloatingActionButtonState()

            val test by BottomTools.currentContent.collectAsState()
            val cache by mainViewModel.flagCache.collectAsState()

            val currentContentInfos by remember {
                derivedStateOf { Pair(test?.name, cache.values.distinctBy { it.id }) }
            }

            val currentPage by mainViewModel.browserManager.currentPage.collectAsState()
            var webView = mainViewModel.webView.collectAsState()
            var canGoBack = mainViewModel.canGoBack.collectAsState()
            var canGoForward = mainViewModel.canGoForward.collectAsState()

            Scaffold(
                containerColor = Color(0xFF363E4C),
                bottomBar = {
                    if (!homePageVisible && currentPage == null)
//                        && (currentContentInfos.first == "DEFAULT_CONTENT" &&
//                                currentContentInfos.second.isNotEmpty())
//                        || currentContentInfos.first != "DEFAULT_CONTENT")
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Transparent)
                        ) {
                            Spacer(
                                modifier = Modifier
                                    .padding(start = 50.dp, end = 50.dp, top = 5.dp, bottom = 0.dp)
                                    .height(1.dp)
                                    .fillMaxWidth()
                                    .background(Color.LightGray)
                            )

                            BottomAppBar(
                                containerColor = Color.Transparent,
                                contentColor = Color.Black,
                                tonalElevation = 0.dp
                            ) {
                                BottomTools.BottomToolBar(activity = this@SigmaActivity)
                            }
                        }

                    if (currentPage != null)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Transparent)
                        ) {
                            Spacer(
                                modifier = Modifier
                                    .padding(start = 50.dp, end = 50.dp, top = 5.dp, bottom = 0.dp)
                                    .height(1.dp)
                                    .fillMaxWidth()
                                    .background(Color.LightGray)
                            )
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                                    .background(Color.Transparent)
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { webView?.value?.goBack() },
                                    enabled = canGoBack.value,
                                    modifier = Modifier.padding(horizontal = 5.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFe9c46a),
                                        contentColor = Color.Black
                                    )
                                )
                                {
                                    Icon(
                                        painter = painterResource(id = R.drawable.la_gauche),
                                        contentDescription = "back",
                                        modifier = Modifier.size(ButtonDefaults.IconSize),
                                        tint = Color.Black
                                    )
                                }

                                Button(
                                    onClick = { mainViewModel.browserManager.setCurrentPage("https://www.google.fr") },
                                    modifier = Modifier.padding(horizontal = 5.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFe9c46a),
                                        contentColor = Color.Black
                                    )
                                )
                                {
                                    Icon(
                                        painter = painterResource(id = R.drawable.maison),
                                        contentDescription = "home",
                                        modifier = Modifier.size(ButtonDefaults.IconSize),
                                        tint = Color.Black
                                    )
                                }

                                Button(
                                    onClick = mainViewModel.browserManager::closeBrowser,
                                    modifier = Modifier.padding(horizontal = 5.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFe9c46a),
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text(
                                        "Retourner à l'application",
                                        color = Color.Black
                                    )
                                }

                                Button(
                                    onClick = { webView.value?.goForward() },
                                    enabled = canGoForward.value,
                                    modifier = Modifier.padding(horizontal = 5.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFe9c46a),
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.la_droite),
                                        contentDescription = "forward",
                                        modifier = Modifier.size(ButtonDefaults.IconSize),
                                        tint = Color.Black
                                    )
                                }
                            }
                        }
                },
//                bottomBar = {
//                    BottomAppBar(
//                        modifier = Modifier.padding(end = 15.dp)
//                    ) {
//                        Spacer(modifier = Modifier.weight(1f))
//
////                        BottomAppBarSpeedDialFloatingActionButton(
////                            state = fabState,
////                            containerColor = Color.Transparent,
////                            modifier = Modifier
////                        ) {
////                            Icon(
////                                modifier = Modifier
////                                    .size(30.dp),
////                                painter = painterResource(R.drawable.dossiers),
////                                tint = Color.Black,
////                                contentDescription = null
////                            )
////                        }
//                    }
//                },
                floatingActionButton = {
                    Column {
                        NewFolderFAB(
                            homePageVisible = homePageVisible,
                            isTextDialogVisible = isTextDialogVisible,
                            isYesNoDialogVisible = isYesNoDialogVisible,
                            isMoveFileDialogVisible = isMoveFileDialogVisible,
                            isTagInfosDialogVisible = isTagInfosDialogVisible,
                            isFilePickerVisible = isFilePickerVisible,
                            fabState = fabState
                        )
                    }
                }
            ) { padding ->
                DossierSigmaTheme {

                    val currentFolder by mainViewModel.currentFolder.collectAsState()
                    val selectedItem by mainViewModel.selectedItem.collectAsState()
                    val activity = LocalContext.current as Activity
                    val currentTool by BottomTools.currentTool.collectAsState()
                    val isSettingsPageVisible by mainViewModel.isSettingsPageVisible.collectAsState()

                    val dialogMessage = mainViewModel.dialogMessage.collectAsState()

                    SideEffect {
                        activity.window.statusBarColor = Color(0xFF363E4C).toArgb()
                    }

                    BackHandler(enabled = true) {
                        mainViewModel.sortingCache[mainViewModel.currentFolderPath.value] =
                            mainViewModel.sorting.value
                        mainViewModel.removeLastFolderPathHistory()

                        val newSorting = if (mainViewModel.folderPathHistory.value.isEmpty())
                            ITEMS_ORDERING_STRATEGY.DATE_DESC
                        else
                            mainViewModel.sortingCache[mainViewModel.folderPathHistory.value.last()]
                                ?: ITEMS_ORDERING_STRATEGY.DATE_DESC
                        mainViewModel.setSorting(newSorting)
//                        mainViewModel.refreshCurrentFolder()
                    }

                    LaunchedEffect(Unit) {
                        BottomTools.setCurrentContent(DEFAULT)
                    }

                    Box(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
//                            .zIndex(20f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF363E4C))
                                .pointerInput(selectedItem?.id) {

                                    //////////////////////////////////
                                    // RAZ si tap sur une zone vide //
                                    //////////////////////////////////
                                    detectTapGestures(onTap = {
                                        if (selectedItem?.id != null) {
                                            mainViewModel.setSelectedItem(null, true)
                                            BottomTools.setCurrentContent(DEFAULT)
                                        }
                                    })
                                }
                        ) {
                            Spacer(modifier = Modifier.height(20.dp))

                            /////////////////////////////////
                            // zone horizontale supérieure //
                            /////////////////////////////////
                            Box(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val sortingWidth = 200.dp

                                ///////////////////////////////////
                                // zone Home button + breadcrumb //
                                ///////////////////////////////////
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = sortingWidth),
                                ) {

                                    /////////////////
                                    // Home button //
                                    /////////////////
                                    HomeButtonIcon(
                                        icon = R.drawable.mouvement
                                    ) {
                                        mainViewModel.setIsSettingsPageVisible(false)
                                        homeViewModel.setHomePageVisible(true)
                                    }

                                    ////////////////
                                    // breadcrumb //
                                    ////////////////
                                    if (!homePageVisible)
                                        Breadcrumb(
                                            items = currentFolder.fullPath.split("/")
                                                .filter { it != "" },
                                            onPathClick = { path ->
                                                mainViewModel.goToFolder(path)
                                            },
                                            modifier = Modifier
                                                .padding(start = 10.dp)
                                                .align(Alignment.CenterVertically),
                                            activeColor = Color(0xFF8697CB),
                                            inactiveColor = Color(0xFF8697CB),
                                            arrowColor = Color.Magenta,
                                            transitionDuration = 200,
                                        )
                                }

                                if (homePageVisible) {

                                    ////////////////////////////////////////////////
                                    // zone horizontale supérieure pour home page //
                                    ////////////////////////////////////////////////
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(end = 10.dp)
                                    )
                                    {
                                        Row(
                                            modifier = Modifier
                                                .width(IntrinsicSize.Min)
                                                .align(Alignment.CenterEnd),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {

                                            ///////////////////////
                                            // ajout de homeItem //
                                            ///////////////////////
                                            Icon(
                                                modifier = Modifier
                                                    .size(35.dp)
                                                    .padding(
                                                        start = 10.dp,
                                                        end = 5.dp
                                                    )
                                                    .pointerInput(true) {
                                                        detectTapGestures(
                                                            onTap = {
                                                                val homeItemCount =
                                                                    homeViewModel.homeItems.value.size
                                                                homeViewModel.setDialogHomeItemInfos(
                                                                    HomeItemInfos(
                                                                        oldTitle = "",
                                                                        newTitle = "",
                                                                        picture = null,
                                                                        path = "",
                                                                        index = homeItemCount
                                                                    )
                                                                )

                                                                mainViewModel.setIsHomeItemDialogVisible(
                                                                    true
                                                                )
                                                            }
                                                        )
                                                    },
                                                painter = painterResource(R.drawable.plus),
                                                tint = Color(0xFFe9c46a),
                                                contentDescription = null
                                            )

                                            val isSettingsPageVisible by mainViewModel.isSettingsPageVisible.collectAsState()

                                            ///////////////////////
                                            // Icône de settings //
                                            ///////////////////////
                                            Icon(
                                                modifier = Modifier
                                                    .size(50.dp)
                                                    .padding(
                                                        start = 10.dp,
                                                        end = 10.dp
                                                    )
                                                    .pointerInput(true) {
                                                        detectTapGestures(
                                                            onTap = {
                                                                homeViewModel.setHomePageVisible(
                                                                    false
                                                                )
                                                                mainViewModel.setIsSettingsPageVisible(
                                                                    true
                                                                )
                                                            }
                                                        )
                                                    },
                                                painter = painterResource(R.drawable.settings),
                                                tint = Color(0xFFe9c46a),
                                                contentDescription = null
                                            )
                                        }
                                    }

                                } else {

                                    ////////////////////////////////////////
                                    // zone de sélection du tri des items //
                                    ////////////////////////////////////////

                                    //TODO pas besoin de tout recharger
                                    SortingArea(
                                        sortingWidth = sortingWidth,
                                        sortingFlow = mainViewModel.sorting,
                                        onDateSortClick = {
                                            mainViewModel.goToFolder(
                                                currentFolder.fullPath,
                                                ITEMS_ORDERING_STRATEGY.DATE_DESC
                                            )
                                        },
                                        onNameSortClick = {
                                            mainViewModel.goToFolder(
                                                currentFolder.fullPath,
                                                ITEMS_ORDERING_STRATEGY.NAME_ASC
                                            )
                                        }
                                    )
                                }
                            }

                            ///////////////////
                            // Settings Page //
                            ///////////////////
                            if (isSettingsPageVisible)
                                settingsPage(vm = settingsViewModel)

                            ///////////////
                            // Home page //
                            ///////////////
                            if (homePageVisible) {

                                homePage(
                                    homeItemsInVM = homeViewModel.homeItems,
                                    onItemClicked = { item: HomeItem ->
                                        mainViewModel.goToFolder(item.path)
                                        homeViewModel.setHomePageVisible(
                                            false
                                        )
                                    },
                                    onEditTapped = { item: HomeItem ->
                                        homeViewModel.setDialogHomeItemInfos(
                                            HomeItemInfos(
                                                oldTitle = item.title,
                                                picture = item.picture,
                                                path = item.path,
                                                index = item.index
                                            )
                                        )

                                        mainViewModel.setIsHomeItemDialogVisible(
                                            true
                                        )
                                    },
                                    onDeleteTapped = { item: HomeItem ->
                                        homeViewModel.removeHomeItem(
                                            item
                                        )
                                    },
                                    onItemsReordered = { newList ->
                                        homeViewModel.setHomeItems(newList)
                                        mainViewModel.viewModelScope.launch {
                                            settingsViewModel.settingsManager.saveHomeItems(
                                                newList
                                                    .toSet()
                                                    .map {
                                                        HomeItemInfos(
                                                            newTitle = it.title,
                                                            oldTitle = it.title,
                                                            picture = it.picture,
                                                            path = it.path,
                                                            index = it.index
                                                        )
                                                    }.toSet()
                                            )
                                        }
                                    }
                                )
                            }

                            /////////////////
                            // Normal page //
                            /////////////////
                            if (!homePageVisible) {
                                NormalPage(
                                    currentFolderFlow = mainViewModel.currentFolder,
                                    imageCache = mainViewModel.imageCache,
                                    flagCache = mainViewModel.flagCache,
                                    scaleCache = mainViewModel.scaleCache,
                                    memoCache = mainViewModel.memoCache,
                                    onHoveredNotHovered = { item ->
                                        mainViewModel.setDragTargetItem(item)
                                    },
                                    selectedItemFullPath = mainViewModel.selectedItemFullPath,
                                    dragOffset = mainViewModel.dragOffset,
                                    draggableStartPosition = mainViewModel.draggableStartPosition,
                                    onItemTapped = { item ->
                                        run {

                                            if (selectedItem != null) {
                                                mainViewModel.setSelectedItem(null, true)
                                                BottomTools.setCurrentContent(DEFAULT)
                                                return@run
                                            }

                                            if (item.isFolder()) {
                                                mainViewModel.goToFolder(item.fullPath)
                                            }

                                            if (item.isFile() &&
                                                (item.name.endsWith(".mp4") ||
                                                        item.name.endsWith(".mkv") ||
                                                        item.name.endsWith(".mpg") ||
                                                        item.name.endsWith(".iso") ||
                                                        item.name.endsWith(".avi"))
                                            ) {
                                                mainViewModel.playVideoFile(item.fullPath)
                                            }
                                            if (item.isFile() && item.name.endsWith(".html")) {
                                                mainViewModel.playHtmlFile(item.fullPath)
                                            }
                                        }

                                    },
                                    onItemLongPressed = { item ->
                                        mainViewModel.setSelectedItem(item.copy(), true)
                                        BottomTools.setCurrentContent(Tools.FILE)
                                    },
                                    onTopLeftPanelClick = { item ->
                                        /**
                                         * suite dans MainActivity
                                         */
                                        mainViewModel.setSelectedItem(item.copy())
                                        mainViewModel.setIsDisplayingMemo(!mainViewModel.isDisplayingMemo.value)
                                    },
                                    getInfoSup = { item ->
                                        mainViewModel.getInfoSup(item)
                                    },
                                    getInfoInf = { item ->
                                        mainViewModel.getInfoInf(item)
                                    },
                                    onRefresh = {
                                        mainViewModel.refreshCurrentFolder()
                                    },
                                    currentPage = mainViewModel.browserManager.currentPage,
                                    closeBrowser = {
                                        mainViewModel.browserManager.closeBrowser()
                                    },
                                    onGotBrowserImage = onGotBrowserImage,
                                    setCurrentBrowserPage = mainViewModel.browserManager::setCurrentPage,
                                    webView = mainViewModel.webView,
                                    canGoBack = mainViewModel.canGoBack,
                                    canGoForward = mainViewModel.canGoForward,
                                    setCanGoBack = mainViewModel::setCanGoBack,
                                    setCanGoForward = mainViewModel::setCanGoForward,
                                    setWebView = mainViewModel::setWebView
                                )
                            }
                        }

                        if (isTextDialogVisible)
                            CustomTextDialog(
                                text = dialogMessage.value ?: "",
                                viewModel = mainViewModel,
                                initialText = mainViewModel.dialogInitialText.value ?: ""
                            ) { text ->
                                if (mainViewModel.dialogOnOkLambda != null) {
                                    mainViewModel.viewModelScope.launch {
                                        mainViewModel.dialogOnOkLambda?.invoke(
                                            text,
                                            mainViewModel,
                                            this@SigmaActivity
                                        )
                                    }
                                    mainViewModel.dialogOnOkLambda = null
                                } else
                                    mainViewModel.viewModelScope.launch {
                                        currentTool?.onClick?.let {
                                            it.invoke(
                                                currentTool!!,
                                                mainViewModel,
                                                this@SigmaActivity
                                            )
                                        }
                                    }
                            }

                        if (isYesNoDialogVisible) {
                            CustomYesNoDialog(
                                dialogMessage.value ?: "",
                                mainViewModel
                            ) { yesNo ->
                                if (mainViewModel.dialogYesNoLambda != null) {
                                    mainViewModel.viewModelScope.launch {
                                        mainViewModel.dialogYesNoLambda?.invoke(
                                            yesNo,
                                            mainViewModel,
                                            this@SigmaActivity
                                        )
                                    }
                                    mainViewModel.dialogYesNoLambda = null
                                } else
                                    mainViewModel.viewModelScope.launch {
                                        currentTool?.onClick?.let {
                                            it.invoke(
                                                currentTool!!,
                                                mainViewModel,
                                                this@SigmaActivity
                                            )
                                        }
                                    }
                            }
                        }

                        if (isMoveFileDialogVisible) {
                            CustomMoveFileExistingDestinationDialog(
                                viewModel = mainViewModel,
                                onOverwrite = {
                                    val intent =
                                        Intent(
                                            this@SigmaActivity,
                                            MoveFileService::class.java
                                        ).apply {
                                            putExtra(
                                                "source",
                                                BottomTools.movingItem?.fullPath ?: ""
                                            )
                                            putExtra(
                                                "destination",
                                                BottomTools.movingItem?.fullPath ?: ""
                                            )
                                            putExtra("addSuffix", "")
                                        }
                                    startService(intent)
                                    mainViewModel.refreshCurrentFolder()
                                },
                                onCancel = {
                                    BottomTools.setCurrentContent(DEFAULT)
                                    val item = BottomTools.movingItem
                                    val movingParent = item?.fullPath?.substringBeforeLast("/")

                                    if (movingParent != null)
                                        mainViewModel.goToFolder(movingParent)
                                    BottomTools.movingItem = null
                                    mainViewModel.setSelectedItem(null, true)
                                    mainViewModel.refreshCurrentFolder()


                                },
                                onCreateCopy = {
                                    val intent =
                                        Intent(
                                            this@SigmaActivity,
                                            MoveFileService::class.java
                                        ).apply {
                                            putExtra(
                                                "source",
                                                BottomTools.movingItem?.fullPath ?: ""
                                            )
                                            putExtra(
                                                "destination",
                                                BottomTools.itemToMove?.fullPath
                                            )
                                            putExtra("addSuffix", " - copie")
                                        }
                                    startService(intent)
                                    mainViewModel.refreshCurrentFolder()
                                }
                            )
                        }

                        if (isTagInfosDialogVisible) {
                            TagInfosDialog(
                                text = dialogMessage.value ?: "",
                                viewModel = mainViewModel,
                                onDatasCompleted = { infos: TagInfos?, model: SigmaViewModel, activity: SigmaActivity ->
                                    mainViewModel.dialogTagLambda?.invoke(
                                        infos!!,
                                        mainViewModel,
                                        this@SigmaActivity
                                    )
                                },
                                mainActivity = this@SigmaActivity
                            )
                        }

                        if (isHomeItemDialogVisible) {
                            val dialogHomeItemInfos by homeViewModel.dialogHomeItemInfos.collectAsState()

                            HomeItemDialog(
                                viewModel = mainViewModel,
                                onDatasCompleted = { infos: HomeItemInfos? ->
                                    if (infos?.newTitle == null || infos.path == null)
                                        return@HomeItemDialog
                                    val items = homeViewModel.homeItems.value
                                    if (infos.oldTitle in items.map { it.title }) {
                                        //modifier
                                        homeViewModel.setHomeItems(
                                            items
                                                .map {
                                                    if (it.title == infos.oldTitle)
                                                        HomeItem(
                                                            title = infos.newTitle,
                                                            path = infos.path,
                                                            picture = infos.picture
                                                        )
                                                    else
                                                        it
                                                })
                                    } else {
                                        //insérer
                                        val newList = items.toMutableList()
                                        newList.add(
                                            HomeItem(
                                                title = infos.newTitle,
                                                picture = infos.picture,
                                                path = infos.path
                                            )
                                        )

                                        homeViewModel.setHomeItems(newList)
                                    }
                                },
                                message = "Addition/Edition de raccourci",
                                homeItemInfos = homeViewModel.dialogHomeItemInfos,
                            )
                        }

                        if (isFilePickerVisible) {
                            FolderChooserDialog(
                                modifier = Modifier
                                    .align(Alignment.Center),
                                viewModel = mainViewModel
                            ) { path ->
                                onFolderChosen(path)
                            }
                        }
                    }

                    val richTextState = rememberRichTextState()

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
//                            .zIndex(20f)
                    ) {
                        val isRichText = mainViewModel.isDisplayingMemo.collectAsState()
                        val isDisplayingPalette =
                            mainViewModel.isDisplayingMemoPalette.collectAsState()

                        if (isRichText.value) {
                            MemoEditor(
                                modifier = Modifier
                                    .align(Alignment.TopCenter),
                                isRichText = isRichText,
                                richTextState = richTextState
                            )
                        }

                        if (isRichText.value && isDisplayingPalette.value) {
                            val keyboardController = LocalSoftwareKeyboardController.current
                            keyboardController?.hide()

                            Column(
                                modifier = Modifier
                                    .fillMaxSize(),
//                                    .zIndex(25f),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Palette(
                                        defaultColor = Color.Magenta,
                                        buttonSize = 100.dp,
                                        swatches = Presets.material(),
                                        innerRadius = 400f,
                                        strokeWidth = 120f,
                                        spacerRotation = 5f,
                                        spacerOutward = 2f,
                                        verticalAlignment = VerticalAlignment.Middle,
                                        horizontalAlignment = HorizontalAlignment.Center,
                                        onColorSelected = { color ->
                                            mainViewModel.setIsDisplayingMemoPalette(false)
                                            val saved =
                                                mainViewModel.savedSelectedRange.value
                                                    ?: return@Palette
                                            richTextState.selection = saved
                                            richTextState.addSpanStyle(
                                                SpanStyle(
                                                    color = color
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    suspend fun manageImageClick(viewModel: SigmaViewModel, imageUrl: String) {
        if (viewModel.selectedItem.value != null)
            viewModel.updatePicture(imageUrl)
    }

    private fun initializeFileIntentLauncher(viewModel: SigmaViewModel) {
        val launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val pathUri = result.data?.data
                viewModel.onFolderSelected(pathUri)
            }
        intentWrapper.setLauncher(launcher as Object)
    }

    //callback de UCrop pour rogner manuellement l'image de l'item
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null)
            return

        var resultUri: Uri? = null
        var cropError: Throwable? = null

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            resultUri = UCrop.getOutput(data)
        } else if (resultCode == UCrop.RESULT_ERROR) {
            cropError = UCrop.getError(data)
        }

        if (resultUri == null)
            return

        mainViewModel.viewModelScope.launch {
            val croppedBitmap =
                BitmapFactory.decodeStream(contentResolver.openInputStream(resultUri))
            mainViewModel.updatePicture(
                Bitmap.createBitmap(croppedBitmap),
                onlyCropped = true
            )
//            mainViewModel.refreshCurrentFolder()
        }

//        mainViewModel.setSelectedItem(null)
//        BottomTools.setCurrentContent(DEFAULT, mainViewModel))
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
