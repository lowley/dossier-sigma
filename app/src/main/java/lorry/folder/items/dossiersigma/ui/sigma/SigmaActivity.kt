package lorry.folder.items.dossiersigma.ui.sigma

//region imports
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.leinardi.android.speeddial.compose.SpeedDialOverlay
import com.leinardi.android.speeddial.compose.SpeedDialState
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.ComponentWithViewModel
import lorry.folder.items.dossiersigma.PermissionsManager
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.external.intent.DSI_IntentWrapper
import lorry.folder.items.dossiersigma.headless.domain.Item
import lorry.folder.items.dossiersigma.headless.folderContentBack.ReloadType
import lorry.folder.items.dossiersigma.headless.moveToNasWorker.MoveToNASWorker
import lorry.folder.items.dossiersigma.headless.usecases.files.ChangePathUseCase
import lorry.folder.items.dossiersigma.headless.usecases.homePage.HomeItem
import lorry.folder.items.dossiersigma.headless.usecases.homePage.HomeViewModel
import lorry.folder.items.dossiersigma.ui.IndexBar.IIndexBar
import lorry.folder.items.dossiersigma.ui.browser.IBrowser
import lorry.folder.items.dossiersigma.ui.browser.ui.BrowserBottomToolbar
import lorry.folder.items.dossiersigma.ui.items.IItemsComponent
import lorry.folder.items.dossiersigma.ui.folderContent.breadcrumb.Breadcrumb
import lorry.folder.items.dossiersigma.ui.folderContent.tools.controller.BottomComponent
import lorry.folder.items.dossiersigma.ui.folderContent.tools.controller.IBottomComponent
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.BottomTools
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.Tool
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.toolbars.DEFAULT
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.toolbars.FILE
import lorry.folder.items.dossiersigma.ui.folderContent.tools.utils.ToolsViewModel
import lorry.folder.items.dossiersigma.ui.memo.IMemoComponent
import lorry.folder.items.dossiersigma.ui.settings.DefaultColorScheme
import lorry.folder.items.dossiersigma.ui.settings.SettingsPage
import lorry.folder.items.dossiersigma.ui.settings.SettingsViewModel
import lorry.folder.items.dossiersigma.ui.fullSizeDialogs.FullSizeExtras
import lorry.folder.items.dossiersigma.ui.fullSizeDialogs.HomeItemInfos
import lorry.folder.items.dossiersigma.ui.tinies.HomeButtonIcon
import lorry.folder.items.dossiersigma.ui.tinies.HomePage
import lorry.folder.items.dossiersigma.ui.tinies.SigmaFAB
import lorry.folder.items.dossiersigma.ui.tinies.SortingArea
import lorry.folder.items.dossiersigma.ui.tinies.initializeFileIntentLauncher
import javax.inject.Inject

//endregion

val SigmaColors = staticCompositionLocalOf { DefaultColorScheme() }

@AndroidEntryPoint
class SigmaActivity : ComponentActivity() {

    companion object {
        val TAG = "SigmaActivity"
        val FILE_REQUEST_CODE = 969
    }

    @Inject
    lateinit var intentWrapper: DSI_IntentWrapper

    @Inject
    lateinit var changePathUseCase: ChangePathUseCase

    @Inject
    lateinit var memo: IMemoComponent

    @Inject
    lateinit var folderContentFrontComponent: IItemsComponent

    @Inject
    lateinit var bottomFactory: BottomComponent.Factory

    @Inject
    lateinit var bottomToolsFactory: BottomTools.Factory

    //cd [[BottomComponent]]
    @Inject
    lateinit var toolsViewModel: ToolsViewModel

//    @Inject
//    lateinit var bottomTools: BottomTools

    @Inject
    lateinit var browser: IBrowser

    val mainViewModel: SigmaViewModel by viewModels()
    val homeViewModel: HomeViewModel by viewModels()
    val settingsViewModel: SettingsViewModel by viewModels()

    /**
     * Appelée par la boîte de dialogue de création / modification de HomeItem
     * @see HomeItemDialog
     * @see FolderChooserDialog
     */
    var onFolderChosen: (String?) -> Unit = {}

    val sigmaActivity = this

    @Inject
    lateinit var indexBar: IIndexBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //#[[BottomComponent]] initialisation composant IBottomComponent
//        (bottomComponent as ComponentWithViewModel<ToolsViewModel>).attach(toolsViewModel)
//        bottomComponent.sigmaViewModel = mainViewModel

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        window.navigationBarDividerColor = android.graphics.Color.TRANSPARENT

        val permissionsManager = PermissionsManager()
        if (!permissionsManager.hasExternalStoragePermission())
            permissionsManager.requestExternalStoragePermission(this)

        initializeFileIntentLauncher(mainViewModel)

        setContent {
//            val myColorScheme by settingsViewModel.settingsManager.colorSchemeFlow.collectAsState(
//                null
//            ) onCreate
            val colorScheme by settingsViewModel.colorScheme.collectAsState()

            MaterialTheme(
                colorScheme = colorScheme,
                typography = Typography(),
                shapes = Shapes(),
            ) {
                CompositionLocalProvider(SigmaColors provides colorScheme) {
                    AppContent()
                }
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun AppContent() {

        val bottomComponent = remember { bottomFactory.create(
            viewModel = toolsViewModel,
            sigmaViewModel = mainViewModel
        ) }

        val bottomTools = remember { bottomToolsFactory.create(
            viewModel = mainViewModel,
        ) }

        bottomComponent.observeDefaultContent()

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorScheme.background
        ) {
            val isTextDialogVisible by mainViewModel.isTextDialogVisible.collectAsState()
            val isYesNoDialogVisible by mainViewModel.isYesNoDialogVisible.collectAsState()
            val isMoveFileDialogVisible by mainViewModel.isMoveFileDialogVisible.collectAsState()
            val isTagInfosDialogVisible by mainViewModel.isTagInfosDialogVisible.collectAsState()
            val isHomeItemDialogVisible by mainViewModel.isHomeItemDialogVisible.collectAsState()
            val isFilePickerVisible by mainViewModel.isFilePickerVisible.collectAsState()

            val homePageVisible by homeViewModel.homePageVisible.collectAsState()
            val isSettingsPageVisible by mainViewModel.isSettingsPageVisible.collectAsState()

            var fabState = rememberSaveable { mutableStateOf(SpeedDialState.Collapsed) }
            var overlayVisible = rememberSaveable { mutableStateOf(fabState.value.isExpanded()) }
            val colors = colorScheme
            val isDisplayingMemo by memo.isDisplayingMemo.collectAsState()
            val isKeyboardVisible by keyboardAsState()

            val currentFolder by mainViewModel.folderContentComponent.currentFolderFlow.collectAsState(
                null
            )
            val currentSorting = mainViewModel.folderContentComponent.sorting

            val browserState by browser.vm.state.collectAsState()

            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = {
                    //////////////////////////
                    // bottomAppBar normale //
                    //////////////////////////

                    val hidden = homePageVisible ||
                            browserState.url != null ||
                            (isDisplayingMemo && isKeyboardVisible)

                    if (!hidden)
//                        && (currentContentInfos.first == "DEFAULT_CONTENT" &&
//                                currentContentInfos.second.isNotEmpty())
//                        || currentContentInfos.first != "DEFAULT_CONTENT")
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(65.dp)
                                .background(SigmaColors.current.primary)
                        ) {
                            Spacer(
                                modifier = Modifier
                                    .padding(
                                        start = 50.dp,
                                        end = 50.dp,
                                        top = 5.dp,
                                        bottom = 0.dp
                                    )
                                    .height(1.dp)
                                    .fillMaxWidth()
                                    .background(SigmaColors.current.secondary)
                            )

                            BottomAppBar(
                                containerColor = SigmaColors.current.primary,
                                contentColor = colors.background,
                                tonalElevation = 0.dp,

                                ) {
                                bottomTools.BottomToolBar(
                                    activity = this@SigmaActivity,
                                    beginDrag = folderContentFrontComponent::beginDrag,
                                    terminateDrag = folderContentFrontComponent::terminateDrag,
                                    setDragTargetItem = folderContentFrontComponent::setDragTargetItem,
                                    addDragOffset = folderContentFrontComponent::addDragOffset,
                                    dragTargetItem = folderContentFrontComponent.dragTargetItem,
                                )
                            }
                        }

                    ///////////////////////////////
                    // barre d'outils du browser //
                    ///////////////////////////////

                    //déclaration du composant BrowserBottomToolbar: #[[BrowserBottomToolbar]]
                    if (browserState.url != null)
                        BrowserBottomToolbar()
                },
                floatingActionButton = {
                    Column {
                        SigmaFAB(
                            homePageVisible = homePageVisible,
                            isTextDialogVisible = isTextDialogVisible,
                            isYesNoDialogVisible = isYesNoDialogVisible,
                            isMoveFileDialogVisible = isMoveFileDialogVisible,
                            isTagInfosDialogVisible = isTagInfosDialogVisible,
                            isFilePickerVisible = isFilePickerVisible,
                            isSettingsPageVisible = isSettingsPageVisible,
                            fabState = fabState,
                            overlayVisible = overlayVisible,
                            context = this@SigmaActivity,
                        )
                    }
                }
            ) { padding ->
//                val currentFolder by mainViewModel.currentFolder.collectAsState()
//                val currentFolderLite by mainViewModel.currentFolderLite.collectAsState()
                val selectedItem by mainViewModel.selectedItem.collectAsState()
                val activity = LocalContext.current as Activity
                val isSettingsPageVisible by mainViewModel.isSettingsPageVisible.collectAsState()

                val dialogMessage = mainViewModel.dialogMessage.collectAsState()
                val currentPath =
                    mainViewModel.folderContentComponent.currentPath.collectAsStateWithLifecycle(
                        initialValue = null
                    )

                SideEffect {
                    activity.window.statusBarColor = colors.primary.toArgb()
                }

                BackHandler(enabled = true) {

                    //stockage de sorting actuel dans le cache
                    // -> auto lors des modifications du tri

                    mainViewModel.viewModelScope.launch {
                        val last1 =
                            mainViewModel.folderContentComponent.folderPathHistory.value.last()

                        //retour dans l'history
                        mainViewModel.folderContentComponent.removeLastFolderPathHistory()

                        val last2 =
                            mainViewModel.folderContentComponent.folderPathHistory.value.last()
                        mainViewModel.settingsManager.saveCurrentPath(last2)
                        Log.d("Sact", last1)
                    }

                    //récup sorting dans cache du tri
                    // -> se fait automatiquement dans le combine de currentFolderFlow

                    //on applique le nouveau tri
                    // -> se fait automatiquement dans le combine de currentFolderFlow
                }

                val view = LocalView.current
                val bg = colorScheme.background
                val colors = SigmaColors.current

                SideEffect {
                    val window = activity.window
                    window.navigationBarColor = colors.primary.toArgb()
                    WindowInsetsControllerCompat(window, view)
                        .isAppearanceLightNavigationBars = false
                    window.isNavigationBarContrastEnforced = false
                }

                LaunchedEffect(Unit) {
                    bottomComponent.setCurrentContent(DEFAULT)
                }

                //////////////////////////////
                // box de l' aire centrale  //
                //////////////////////////////
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = 0.dp,
                            end = 0.dp,
                            top = 0.dp,
//                                padding.calculateTopPadding(),
                            bottom = padding.calculateBottomPadding()
                        )
                ) {

                    //////////////////////////////////////////////
                    // l'aire centrale est organisée en colonne //
                    //////////////////////////////////////////////
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 0.dp)
                            .background(SigmaColors.current.primary)
                            .pointerInput(selectedItem?.id) {

                                //////////////////////////////////
                                // RAZ si tap sur une zone vide //
                                //////////////////////////////////
                                detectTapGestures(onTap = {
                                    if (selectedItem?.id != null) {
                                        mainViewModel.setSelectedItem(null, true)
                                        bottomComponent.setCurrentContent(DEFAULT)
                                    }
                                })
                            }
                    ) {
                        /////////////////////////////////
                        // zone horizontale supérieure //
                        /////////////////////////////////
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 15.dp),
                        ) {

                            ////////////////////////////
                            // aire gauche des outils //
                            // normal vs home page    //
                            ////////////////////////////
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                            ) {

                                ///////////////////////////////////
                                // zone Home button + breadcrumb //
                                ///////////////////////////////////
                                Row(
                                    modifier = Modifier
                                        .padding(end = 0.dp)
                                        .height(40.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    /////////////////
                                    // Home button //
                                    /////////////////

                                    var reloadType =
                                        mainViewModel.folderContentComponent.reloadType.collectAsState()
                                    var secondary = SigmaColors.current.secondary

                                    var stuff by remember { mutableStateOf(R.drawable.mouvement to secondary) }

                                    LaunchedEffect(reloadType.value) {
                                        Log.d(
                                            "reloadIcon",
                                            "HomeButton: reloadType: ${reloadType.value.first}, path: ${currentPath.value}"
                                        )

                                        val reloadIcon = when (reloadType.value.first) {
                                            ReloadType.Disk -> R.drawable.disquette
                                            ReloadType.Cache -> R.drawable.cpu
                                            ReloadType.Room -> R.drawable.cloche
                                            else -> R.drawable.mouvement
                                        }

                                        val reloadColor = when (reloadType.value.first) {
                                            ReloadType.Disk -> Color.Red
                                            ReloadType.Cache -> Color.Green
                                            ReloadType.Room -> Color.Yellow
                                            else -> secondary
                                        }

                                        stuff = reloadIcon to reloadColor

                                        delay(5_000)

                                        stuff = R.drawable.mouvement to secondary
                                    }

                                    HomeButtonIcon(
                                        stuff = stuff
                                    ) {
                                        if (!homePageVisible) {
//                                            mainViewModel.folderContentComponent.manuallyInvalidateItems()
                                            mainViewModel.folderContentComponent.setWaitingForItems(
                                                true
                                            )
                                        }

                                        mainViewModel.setIsSettingsPageVisible(false)
                                        homeViewModel.toggleHomePageVisible()
                                    }

                                    ////////////////
                                    // breadcrumb //
                                    ////////////////

                                    if (!homePageVisible)
                                        key(currentPath.value ?: "") {
                                            Breadcrumb(
                                                items = currentFolder?.fullPath?.split("/")
                                                    ?.filter { it != "" } ?: emptyList(),
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
                                }
                            }

                            ////////////////////////////
                            // aire droite des outils //
                            // normal vs home page    //
                            ////////////////////////////
                            if (homePageVisible) {

                                ////////////////////////////////////////////////
                                // zone horizontale supérieure pour home page //
                                ////////////////////////////////////////////////
                                Box(
                                    modifier = Modifier
                                        .padding(end = 10.dp, bottom = 0.dp)
                                        .height(40.dp)
                                )
                                {
                                    ////////////////////////////
                                    // aire de droite: outils //
                                    ////////////////////////////
                                    Row(
                                        modifier = Modifier
                                            .width(IntrinsicSize.Min)
                                            .height(IntrinsicSize.Min)
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
                                            tint = SigmaColors.current.secondary,
                                            contentDescription = null
                                        )

                                        val isSettingsPageVisible by mainViewModel.isSettingsPageVisible.collectAsState()

                                        ///////////////////////
                                        // Icône de settings //
                                        ///////////////////////
                                        Icon(
                                            modifier = Modifier
                                                .padding(horizontal = 10.dp)
                                                .size(30.dp)
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

                                            tint = SigmaColors.current.secondary,
                                            contentDescription = null
                                        )
                                    }
                                }

                            } else {

                                /////////////////////////////////
                                // aire d'affichage des outils //
                                // dans page normale           //
                                /////////////////////////////////

                                ////////////////////////////////////////////
                                // aire de l'avancement copie/déplacement //
                                ////////////////////////////////////////////

                                val nasText by folderContentFrontComponent.copyNASText.collectAsState()
                                val allNasText by folderContentFrontComponent.copyAllNASText.collectAsState()

                                Row(
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .wrapContentSize()
                                        .clickable {
                                            if (nasText != "1 -> NAS" &&
                                                allNasText != "Tous -> NAS"
                                            ) {
                                                MoveToNASWorker.sourceFolderInPath.matchAction(
                                                    someAction = { path ->
                                                        mainViewModel.goToFolder(path)
                                                    },
                                                    noneAction = {
                                                        //on ne fait rien
                                                    }
                                                )

                                            }
                                        }
                                ) {
//                                    val ctx = LocalContext.current
//                                    val isRunning =
//                                        settingsViewModel.settings.isFileObserverEnabledFlow.collectAsState(
//                                            initial = false
//                                        )
//                                    val realFreshness by
//                                    settingsViewModel.settings.testFreshnessFlow.collectAsState(
//                                        null
//                                    )
//                                    val theoricalFreshness by
//                                    mainViewModel.folderContentComponent.folderCacheFlow
//                                        .map {
//                                            it[mainViewModel.folderContentComponent.currentFolderFlow.value?.fullPath]
//                                                ?.freshness
//                                        }.collectAsState(null)
//
//                                    Log.d(
//                                        "SigmaActivitos",
//                                        "realF:${realFreshness.hashCode()}, theo:${theoricalFreshness.hashCode()}"
//                                    )
//
////                                    Button(
////                                        modifier = Modifier,
////                                        onClick = {
////                                            if (isRunning.value)
////                                                ctx.stopDaemon()
////                                            else
////                                                ctx.startDaemon()
////                                        }
////                                    ) {
//
//                                    val reloadType =
//                                        mainViewModel.folderContentComponent.reloadType.collectAsState()
//
//                                    val reloadIcon = when (reloadType.value) {
//                                        ReloadType.Disk -> R.drawable.disquette
//                                        ReloadType.Cache -> R.drawable.cpu
//                                        ReloadType.Room -> R.drawable.horloge
//                                        else -> R.drawable.point_dinterrogation
//                                    }
//
//                                    Icon(
//                                        modifier = Modifier
//                                            .padding(end = 10.dp)
//                                            .size(20.dp)
//                                            .pointerInput(true) {
//                                                detectTapGestures(
//                                                    onTap = {
//
//                                                    }
//                                                )
//                                            },
//                                        painter = painterResource(reloadIcon),
//                                        tint = when (reloadType.value) {
//                                            ReloadType.Disk -> Color.Red
//                                            ReloadType.Cache -> Color.Green
//                                            ReloadType.Room -> Color.Yellow
//                                            else -> Color.Black
//                                        },
//                                        contentDescription = null
//                                    )

//

                                    if (nasText != "1 -> NAS")
                                        Text(
                                            modifier = Modifier
                                                .align(Alignment.CenterVertically)
                                                .padding(end = 5.dp),
                                            text = nasText,
                                            color = SigmaColors.current.onPrimary,
                                            maxLines = 1,
                                            fontWeight = if (nasText != "1 -> NAS" &&
                                                allNasText != "Tous -> NAS"
                                            ) FontWeight.Bold else FontWeight.Normal
                                        )
                                    else
                                        if (allNasText != "Tous -> NAS")
                                            Text(
                                                modifier = Modifier
                                                    .align(Alignment.CenterVertically)
                                                    .padding(end = 5.dp),
                                                text = allNasText,
                                                color = SigmaColors.current.onPrimary,
                                                maxLines = 1,
                                                fontWeight = if (nasText != "1 -> NAS" &&
                                                    allNasText != "Tous -> NAS"
                                                ) FontWeight.Bold else FontWeight.Normal
                                            )
                                }

                                ///////////////////////////
                                // aire de tri des items //
                                ///////////////////////////
                                //TODO pas besoin de tout recharger
                                SortingArea(
                                    modifier = Modifier,
                                    sortingFlow = currentSorting,
                                    onDateSortClick = {
                                        mainViewModel.folderContentComponent.setSorting(
                                            sorting = SortingCriterion.ByDateDesc
                                        )

                                        mainViewModel.folderContentComponent.reloadCurrentFolder()
                                    },
                                    onNameSortClick = {
                                        mainViewModel.folderContentComponent.setSorting(
                                            sorting = SortingCriterion.ByNameAsc
                                        )

                                        mainViewModel.folderContentComponent.reloadCurrentFolder()
                                    }
                                )
                            }
                        }

                        ///////////////////
                        // Settings Page //
                        ///////////////////
                        if (isSettingsPageVisible)
                            SettingsPage(vm = settingsViewModel)

                        ///////////////
                        // Home page //
                        ///////////////
                        if (homePageVisible) {

                            HomePage(
                                homeItemsInVM = homeViewModel.homeItems,
                                onItemClicked = { item: HomeItem ->
                                    mainViewModel.folderContentComponent.manuallyInvalidateItems()
                                    mainViewModel.folderContentComponent.setWaitingForItems(true)
                                    mainViewModel.goToFolder(
                                        item.path
                                    )
                                    homeViewModel.setHomePageVisible(false)
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
                                    homeViewModel.removeHomeItem(item)
                                    mainViewModel.viewModelScope.launch {
                                        settingsViewModel.settings.saveHomeItems(
                                            homeViewModel.homeItems.value
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
                                },
                                onItemsReordered = { newList ->
                                    homeViewModel.setHomeItems(newList)
                                    mainViewModel.viewModelScope.launch {
                                        settingsViewModel.settings.saveHomeItems(
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
                                },
                                modifier = Modifier
                            )
                        }

                        /////////////////////////
                        // folder content page //
                        /////////////////////////
                        if (!homePageVisible) {
                            key(currentPath.value ?: "") {
                                folderContentFrontComponent.FolderContentFrontPage(
                                    onItemTapped = { item ->
                                        run {

                                            if (selectedItem != null) {
                                                mainViewModel.setSelectedItem(null, true)
                                                bottomComponent.setCurrentContent(
                                                    DEFAULT
                                                )
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
                                        bottomComponent.setCurrentContent(FILE)
                                    },
                                    onTopLeftPanelClick = { item ->
                                        /**
                                         * suite dans MainActivity
                                         */
                                        mainViewModel.setSelectedItem(item.copy())
                                        memo.toggleIsDisplayed()
                                    },
                                )
                            }
                        }
                    }

                    ////////////////////////////////////////////////
                    // TextDialog, YesNoDialog, MoveFileDialog,   //
                    // TagInfosDialog, HomeItemDialog, FilePicker //
                    // browser                                    //
                    ////////////////////////////////////////////////

                    /**
                     * @startuml
                     * class View2
                     * class Repo2
                     * View2 -- Repo2
                     * @enduml
                     */
                    FullSizeExtras(browser)

                    ////////////////////////////////
                    // memo + palette de couleurs //
                    ////////////////////////////////
                    memo.Render(
                        selectedItem = selectedItem,
                        setSelectedItem = { item: Item? ->
                            mainViewModel.setSelectedItem(item)
                        }
                    )

                    /////////////////////////////////
                    // étiquette mobile éventuelle //
                    /////////////////////////////////
                    //si icône d'étiquette
                    //2e icône, draggable
                    /**
                     * @startuml
                     * class ViewModel
                     * class Repository
                     * ViewModel --> Repository
                     * @enduml
                     */
                    val dragState by folderContentFrontComponent.dragState.collectAsState()
                    dragState?.let { dragState ->
                        dragState.tool?.let { tool: Tool ->
                            bottomTools.MobileSticker(
                                dragState = dragState,
                                activity = this@SigmaActivity
                            )
                        }
                    }

                    SpeedDialOverlay(
                        visible = overlayVisible.value,
                        color = Color(0x00000000),
                        onClick = {
                            overlayVisible.value = false
                            fabState.value = SpeedDialState.Collapsed
                        },
                    )
                }
            }

        }
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
    }

    @Composable
    fun keyboardAsState(): MutableState<Boolean> {
        val view = LocalView.current
        val imeState = remember { mutableStateOf(false) }

        DisposableEffect(view) {
            val listener = ViewTreeObserver.OnPreDrawListener {
                val isVisible = ViewCompat.getRootWindowInsets(view)
                    ?.isVisible(WindowInsetsCompat.Type.ime()) == true
                imeState.value = isVisible
                true
            }

            view.viewTreeObserver.addOnPreDrawListener(listener)

            onDispose {
                view.viewTreeObserver.removeOnPreDrawListener(listener)
            }
        }

        return imeState
    }
}