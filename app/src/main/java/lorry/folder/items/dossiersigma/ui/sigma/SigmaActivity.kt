package lorry.folder.items.dossiersigma.ui.sigma

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import de.charlex.compose.rememberSpeedDialFloatingActionButtonState
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.PermissionsManager
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.data.intent.DSI_IntentWrapper
import lorry.folder.items.dossiersigma.domain.usecases.files.ChangePathUseCase
import lorry.folder.items.dossiersigma.domain.usecases.homePage.HomeItem
import lorry.folder.items.dossiersigma.domain.usecases.homePage.HomeViewModel
import lorry.folder.items.dossiersigma.ui.bottomArea.BottomTools
import lorry.folder.items.dossiersigma.ui.bottomArea.BrowserBottomToolbar
import lorry.folder.items.dossiersigma.ui.bottomArea.FolderChooserDialog
import lorry.folder.items.dossiersigma.ui.bottomArea.HomeItemDialog
import lorry.folder.items.dossiersigma.ui.bottomArea.HomeItemInfos
import lorry.folder.items.dossiersigma.ui.bottomArea.MobileSticker
import lorry.folder.items.dossiersigma.ui.bottomArea.Tool
import lorry.folder.items.dossiersigma.ui.bottomArea.Tools
import lorry.folder.items.dossiersigma.ui.bottomArea.Tools.DEFAULT
import lorry.folder.items.dossiersigma.ui.centralArea.FullSizeExtras
import lorry.folder.items.dossiersigma.ui.centralArea.Memo
import lorry.folder.items.dossiersigma.ui.centralArea.homePage
import lorry.folder.items.dossiersigma.ui.components.Breadcrumb
import lorry.folder.items.dossiersigma.ui.normal.NormalPage
import lorry.folder.items.dossiersigma.ui.settings.SettingsViewModel
import lorry.folder.items.dossiersigma.ui.settings.SettingsPage
import lorry.folder.items.dossiersigma.ui.theme.DossierSigmaTheme
import javax.inject.Inject


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
            val isSettingsPageVisible by mainViewModel.isSettingsPageVisible.collectAsState()

            val fabState = rememberSpeedDialFloatingActionButtonState()

            val backgroundColor by settingsViewModel.settingsManager.backgroundColorFlow.collectAsState(Color.Black)

            val currentPage by mainViewModel.browserManager.currentPage.collectAsState()

            Scaffold(
                containerColor = backgroundColor,
                bottomBar = {
                    //////////////////////////
                    // bottomAppBar normale //
                    //////////////////////////
                    if (!homePageVisible && currentPage == null)
//                        && (currentContentInfos.first == "DEFAULT_CONTENT" &&
//                                currentContentInfos.second.isNotEmpty())
//                        || currentContentInfos.first != "DEFAULT_CONTENT")
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(65.dp)
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

                    ///////////////////////////////
                    // barre d'outils du browser //
                    ///////////////////////////////
                    if (currentPage != null)
                        BrowserBottomToolbar(
                            webView = mainViewModel.webView,
                            canGoBackFlow = mainViewModel.canGoBack,
                            canGoForwardFlow = mainViewModel.canGoForward
                        )
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
                            fabState = fabState
                        )
                    }
                }
            ) { padding ->
                DossierSigmaTheme {

                    val currentFolder by mainViewModel.currentFolder.collectAsState()
                    val selectedItem by mainViewModel.selectedItem.collectAsState()
                    val activity = LocalContext.current as Activity
                    val isSettingsPageVisible by mainViewModel.isSettingsPageVisible.collectAsState()

                    val dialogMessage = mainViewModel.dialogMessage.collectAsState()

                    SideEffect {
                        activity.window.statusBarColor = backgroundColor.toArgb()
                    }

                    BackHandler(enabled = true) {
                        mainViewModel.sortingCache[mainViewModel.currentFolderPath.value] =
                            mainViewModel.sorting.value
                        mainViewModel.removeLastFolderPathHistory()

                        val newSorting = if (mainViewModel.folderPathHistory.value.isEmpty())
                            SortingCriterion.ByDateDesc
                        else
                            mainViewModel.sortingCache[mainViewModel.folderPathHistory.value.last()]
                                ?: SortingCriterion.ByDateDesc
                        mainViewModel.setSorting(newSorting)
//                        mainViewModel.refreshCurrentFolder()
                    }

                    LaunchedEffect(Unit) {
                        BottomTools.setCurrentContent(DEFAULT)
                    }

                    //////////////////////////////
                    // box de l' aire centrale  //
                    //////////////////////////////
                    Box(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
//                            .zIndex(20f)
                    ) {

                        //////////////////////////////////////////////
                        // l'aire centrale est organisée en colonne //
                        //////////////////////////////////////////////
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(backgroundColor)
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
                            Row(
                                modifier = Modifier.fillMaxWidth()
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
                                            .height(IntrinsicSize.Min)
                                            .padding(end = 0.dp),
                                        verticalAlignment = Alignment.CenterVertically
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
                                            .padding(end = 10.dp)
                                    )
                                    {
                                        ////////////////////////////
                                        // aire de droite: outils //
                                        ////////////////////////////
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

                                    /////////////////////////////////
                                    // aire d'affichage des outils //
                                    // dans page normale           //
                                    /////////////////////////////////

                                    ////////////////////////////////////////////
                                    // aire de l'avancement copie/déplacement //
                                    ////////////////////////////////////////////

                                    val nasText by BottomTools.copyNASText.collectAsState()
                                    val allNasText by BottomTools.copyAllNASText.collectAsState()

                                    if (nasText != "1 -> NAS")
                                        Text(
                                            modifier = Modifier
                                                .align(Alignment.CenterVertically)
                                                .padding(end = 5.dp),
                                            text = nasText,
                                            color = Color.White,
                                            maxLines = 1
                                        )
                                    else
                                        if (allNasText != "Tous -> NAS")
                                            Text(
                                                modifier = Modifier
                                                    .align(Alignment.CenterVertically)
                                                    .padding(end = 5.dp),
                                                text = allNasText,
                                                color = Color.White,
                                                maxLines = 1
                                            )

                                    ///////////////////////////
                                    // aire de tri des items //
                                    ///////////////////////////
                                    //TODO pas besoin de tout recharger

                                    SortingArea(
                                        modifier = Modifier,
                                        sortingFlow = mainViewModel.sorting,
                                        onDateSortClick = {
                                            mainViewModel.goToFolder(
                                                currentFolder.fullPath,
                                                SortingCriterion.ByDateDesc
                                            )
                                        },
                                        onNameSortClick = {
                                            mainViewModel.goToFolder(
                                                currentFolder.fullPath,
                                                SortingCriterion.ByNameAsc
                                            )
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
                                    onHoveredNotHovered = { item ->
                                        mainViewModel.setDragTargetItem(item)
                                    },
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
                                )
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
                        FullSizeExtras()

                        ////////////////////////////////
                        // memo + palette de couleurs //
                        ////////////////////////////////
                        Memo()

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
                        val dragState by mainViewModel.dragState.collectAsState()
                        dragState?.let { dragState ->
                            dragState.tool?.let { tool: Tool ->
                                MobileSticker(
                                    dragState = dragState,
                                    activity = this@SigmaActivity
                                )
                            }
                        }
                    }
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
}



