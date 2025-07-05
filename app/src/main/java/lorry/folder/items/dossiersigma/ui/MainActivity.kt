package lorry.folder.items.dossiersigma.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.viewModelScope
import com.pointlessapps.rt_editor.model.RichTextValue
import com.pointlessapps.rt_editor.model.Style
import com.pointlessapps.rt_editor.ui.RichTextEditor
import com.pointlessapps.rt_editor.ui.defaultRichTextFieldStyle
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.PermissionsManager
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.data.intent.DSI_IntentWrapper
import lorry.folder.items.dossiersigma.domain.services.MoveFileService
import lorry.folder.items.dossiersigma.domain.usecases.files.ChangePathUseCase
import lorry.folder.items.dossiersigma.domain.usecases.homePage.HomeViewModel
import lorry.folder.items.dossiersigma.ui.components.BottomTools
import lorry.folder.items.dossiersigma.ui.components.Breadcrumb
import lorry.folder.items.dossiersigma.ui.components.BrowserOverlay
import lorry.folder.items.dossiersigma.ui.components.CustomMoveFileExistingDestinationDialog
import lorry.folder.items.dossiersigma.ui.components.CustomTextDialog
import lorry.folder.items.dossiersigma.ui.components.CustomYesNoDialog
import lorry.folder.items.dossiersigma.ui.components.ItemComponent
import lorry.folder.items.dossiersigma.ui.components.TagInfos
import lorry.folder.items.dossiersigma.ui.components.TagInfosDialog
import lorry.folder.items.dossiersigma.ui.components.Tools.DEFAULT
import lorry.folder.items.dossiersigma.ui.theme.DossierSigmaTheme
import java.io.File
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var intentWrapper: DSI_IntentWrapper

    @Inject
    lateinit var changePathUseCase: ChangePathUseCase

    val mainViewModel: SigmaViewModel by viewModels()
    val homeViewModel: HomeViewModel by viewModels()
    lateinit var openTextDialog: MutableState<Boolean>
    lateinit var openYesNoDialog: MutableState<Boolean>
    lateinit var openMoveFileDialog: MutableState<Boolean>
    lateinit var openTagInfosDialog: MutableState<Boolean>
    lateinit var isContextMenuVisible: State<Boolean>
    lateinit var homePageVisible: State<Boolean>

    @OptIn(
        ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class,
        ExperimentalMaterial3Api::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionsManager = PermissionsManager()
        if (!permissionsManager.hasExternalStoragePermission())
            permissionsManager.requestExternalStoragePermission(this)


        window.navigationBarColor = ContextCompat.getColor(this, R.color.background)

//        viewModel.viewModelScope.launch(Dispatchers.IO) {
//            viewModel.initCoil(this@MainActivity)
//        }
        initializeFileIntentLauncher(mainViewModel)

        setContent {
            val homePageVisible by homeViewModel.homePageVisible.collectAsState()

            Scaffold(
                floatingActionButton = {
                    if (!homePageVisible &&
                        (::openTextDialog.isInitialized && !openTextDialog.value) &&
                        (::openYesNoDialog.isInitialized && !openYesNoDialog.value) &&
                        (::openMoveFileDialog.isInitialized && !openMoveFileDialog.value) &&
                        (::openTagInfosDialog.isInitialized && !openTagInfosDialog.value)
                    )
                        Button(
                            onClick = {
                                mainViewModel.setDialogMessage("Nom du dossier à créer")
                                mainViewModel.dialogOnOkLambda = { newName, viewModel, mainActivity ->
                                    val currentFolderPath = viewModel.currentFolderPath.value
                                    val newFullName = "$currentFolderPath/$newName"
                                    if (!File(newFullName).exists()) {
                                        if (File(newFullName).mkdir()) {
                                            Toast.makeText(
                                                mainActivity,
                                                "Répertoire créé",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            viewModel.refreshCurrentFolder()
                                        } else
                                            Toast.makeText(
                                                mainActivity,
                                                "Un problème est survenu",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                    }
                                }

                                openTextDialog.value = true
                            },
                            shapes = ButtonShapes(
                                shape = RoundedCornerShape(30.dp),
                                pressedShape = RoundedCornerShape(30.dp)
                            ),
                            modifier = Modifier
                                .padding(bottom = 55.dp, end = 20.dp)
                                .size(60.dp)
                                .alpha(0.5f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF006d77),
                                contentColor = Color(0xFF83c5be)

                            )
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.plus),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(50.dp)
                            )
                        }
                }
            ) { padding ->
                DossierSigmaTheme {

                    val currentFolder by mainViewModel.currentFolder.collectAsState()
                    val selectedItem by mainViewModel.selectedItem.collectAsState()
                    val activity = LocalContext.current as Activity
                    val pictureUpdateId by mainViewModel.pictureUpdateId.collectAsState()
                    val currentTool by BottomTools.currentTool.collectAsState()

                    openTextDialog = remember { mutableStateOf(false) }
                    openYesNoDialog = remember { mutableStateOf(false) }
                    openMoveFileDialog = remember { mutableStateOf(false) }
                    openTagInfosDialog = remember { mutableStateOf(false) }
                    val dialogMessage = mainViewModel.dialogMessage.collectAsState()

                    isContextMenuVisible = mainViewModel.isContextMenuVisible.collectAsState()

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
                        mainViewModel.refreshCurrentFolder()
                    }

                    LaunchedEffect(Unit) {
                        BottomTools.setCurrentContent(DEFAULT)
                    }

                    LaunchedEffect(pictureUpdateId) {
                        //exécuté juste après AccessingToInternetSiteForPictureUseCase/openBrowser 
//                    if (selectedItemPicture.reset) {
//                        viewModel.startPictureFlow()
//                        return@LaunchedEffect
//                    }

                        selectedItem?.let { item ->
                            mainViewModel.browserManager.closeBrowser()
                            mainViewModel.refreshCurrentFolder()
//                        viewModel.updateItemList(item.copy(picture = selectedItemPicture.picture))
                            Toast.makeText(
                                this@MainActivity,
                                "Changement d'image effectué", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    Box(

                    ) {
//                        var value by remember { mutableStateOf(RichTextValue.get()) }
                        val isRichText = mainViewModel.isDisplayingMemo.collectAsState()

                        if (isRichText.value) {
                            val snapshots = mainViewModel.memoCache.collectAsState()
                            val currentItem = mainViewModel.selectedItem.collectAsState()

                            val initialValue = remember(currentItem.value?.fullPath, snapshots.value) {
                                val snapshot = snapshots.value[currentItem.value?.fullPath]
                                snapshot?.let { RichTextValue.fromSnapshot(it) } ?: RichTextValue.get()
                            }

                            var currentValue by remember(currentItem.value?.fullPath) {
                                mutableStateOf(initialValue)
                            }

                            Column(
                                modifier = Modifier
                                    .width(500.dp)
                                    .height(400.dp)
                                    .align(Alignment.TopCenter)
                                    .zIndex(15f)
                            ) {
                                val focusRequester = remember { FocusRequester() }

                                var integerForKeyboard by remember { mutableStateOf(0) }

                                if (integerForKeyboard != 0)
                                    LaunchedEffect(integerForKeyboard) {
                                        focusRequester.requestFocus()
                                    }

                                RichTextEditor(
                                    modifier = Modifier
                                        .background(Color.White)
                                        .height(300.dp)
                                        .focusRequester(focusRequester),
                                    value = currentValue,
                                    onValueChange = {
                                        currentValue = it
                                        currentItem.value?.fullPath?.let { path ->
                                            mainViewModel.setMemoCacheValue(path, it.getLastSnapshot())
                                        }
                                        currentItem.value?.memo = it.getLastSnapshot()
                                    },
                                    textFieldStyle = defaultRichTextFieldStyle().copy(
                                        placeholder = "Entrez du texte",
                                        textColor = Color.Black,
                                        placeholderColor = Color.Gray,
                                    )
                                )

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight()
                                            .background(Color.DarkGray)
                                            .horizontalScroll(rememberScrollState()),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        IconButton(onClick = {
                                            val shapshot = currentValue.getLastSnapshot()

                                            currentValue = RichTextValue.get()
                                            mainViewModel.setIsDisplayingMemo(false)

                                            val item = mainViewModel.selectedItem.value
                                            if (item == null)
                                                return@IconButton

                                            mainViewModel.viewModelScope.launch {
                                                if (item.isFile()) {
//                                                    if (mainViewModel.base64Embedder.extractMemoFromFile
//                                                            (item.fullPath) != null)
//                                                        mainViewModel.base64Embedder.removeMemoFromFile(item.fullPath)

                                                    mainViewModel.base64Embedder.appendMemoToFile(item.fullPath)
                                                }

                                                if (item.isFolder()) {
//                                                    if (mainViewModel.diskRepository.extractMemoFromFolder
//                                                            (item.fullPath) != null)
//                                                        mainViewModel.diskRepository.removeMemoFromFolder(item.fullPath)

                                                    mainViewModel.diskRepository.insertMemoToFolder(item.fullPath)
                                                }
                                            }

                                            mainViewModel.setSelectedItem(null)
                                        }) {
                                            Icon(
                                                modifier = Modifier.size(24.dp),
                                                painter = painterResource(id = R.drawable.exit),
                                                tint = Color(0xFFd1495b),
                                                contentDescription = null
                                            )
                                        }

                                        IconButton(onClick = {
                                            integerForKeyboard += 1
                                        }) {
                                            Icon(
                                                modifier = Modifier.size(24.dp),
                                                painter = painterResource(id = R.drawable.clavier),
                                                contentDescription = null
                                            )
                                        }

                                        EditorAction(
                                            iconRes = R.drawable.bold,
                                            active = currentValue.currentStyles.contains(Style.Bold)
                                        ) {
                                            currentValue = currentValue.insertStyle(Style.Bold)
                                        }
                                        EditorAction(
                                            iconRes = R.drawable.underline,
                                            active = currentValue.currentStyles.contains(Style.Underline)
                                        ) {
                                            currentValue = currentValue.insertStyle(Style.Underline)
                                        }
                                        EditorAction(
                                            iconRes = R.drawable.italic,
                                            active = currentValue.currentStyles.contains(Style.Italic)
                                        ) {
                                            currentValue = currentValue.insertStyle(Style.Italic)
                                        }
                                        EditorAction(
                                            iconRes = R.drawable.strikethrough,
                                            active = currentValue.currentStyles.contains(Style.Strikethrough)
                                        ) {
                                            currentValue = currentValue.insertStyle(Style.Strikethrough)
                                        }
                                        EditorAction(
                                            iconRes = R.drawable.leftalign,
                                            active = currentValue.currentStyles.contains(Style.AlignLeft)
                                        ) {
                                            currentValue = currentValue.insertStyle(Style.AlignLeft)
                                        }
                                        EditorAction(
                                            iconRes = R.drawable.centeralign,
                                            active = currentValue.currentStyles.contains(Style.AlignCenter)
                                        ) {
                                            currentValue = currentValue.insertStyle(Style.AlignCenter)
                                        }
                                        EditorAction(
                                            iconRes = R.drawable.rightalign,
                                            active = currentValue.currentStyles.contains(Style.AlignRight)
                                        ) {
                                            currentValue = currentValue.insertStyle(Style.AlignRight)
                                        }
                                        EditorAction(
                                            iconRes = R.drawable.textsize,
                                            active = currentValue.currentStyles
                                                .filterIsInstance<Style.TextSize>()
                                                .isNotEmpty()
                                        ) {
                                            // Remove all styles in selected region that changes the text size
                                            currentValue = currentValue.clearStyles(Style.TextSize())

                                            // Here you would show a dialog of some sorts and allow user to pick
                                            // a specific text size. I'm gonna use a random one between 50% and 200%

                                            currentValue = currentValue.insertStyle(
                                                Style.TextSize(
                                                    (Random.nextFloat() *
                                                            (Style.TextSize.MAX_VALUE - Style.TextSize.MIN_VALUE) +
                                                            Style.TextSize.MIN_VALUE).toFloat()
                                                )
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight()
                                            .background(Color.DarkGray)
                                            .horizontalScroll(rememberScrollState()),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        
                                        EditorAction(
                                            iconRes = R.drawable.palette,
                                            active = currentValue.currentStyles
                                                .filterIsInstance<Style.TextColor>()
                                                .isNotEmpty()
                                        ) {
                                            // Remove all styles in selected region that changes the text color
                                            currentValue =
                                                currentValue.clearStyles(Style.TextColor(Color.Transparent))

                                            // Here you would show a dialog of some sorts and allow user to pick
                                            // a specific color. I'm gonna use a random one

                                            currentValue = currentValue.insertStyle(
                                                Style.TextColor(Random.nextInt(360).hueToColor())
                                            )
                                        }
                                        EditorAction(R.drawable.clear, active = true) {
                                            currentValue = currentValue.insertStyle(Style.ClearFormat)
                                        }
                                        EditorAction(
                                            iconRes = R.drawable.undo,
                                            active = currentValue.isUndoAvailable
                                        ) {
                                            currentValue = currentValue.undo()
                                        }
                                        EditorAction(
                                            iconRes = R.drawable.redo,
                                            active = currentValue.isRedoAvailable
                                        ) {
                                            currentValue = currentValue.redo()
                                        }
                                    }
                                }

                            }
                        }
                        
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF363E4C))
                                .pointerInput(selectedItem?.id) {
                                    detectTapGestures(onTap = {
                                        if (selectedItem?.id != null) {
                                            mainViewModel.setSelectedItem(null, true)
                                            BottomTools.setCurrentContent(DEFAULT)
                                        }
                                    })
                                }
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
                                    Icon(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .padding(
                                                start = 15.dp,
                                                end = 5.dp
                                            )
                                            .align(Alignment.CenterVertically)
                                            .size(50.dp)
                                            .pointerInput(true) {
                                                detectTapGestures(
                                                    onTap = {
//                                                changePathUseCase.askInputFolder()
                                                        homeViewModel.setHomePageVisible(!homePageVisible)

                                                    }
                                                )
                                            },
                                        painter = painterResource(R.drawable.mouvement),
                                        tint = Color(0xFFe9c46a),
                                        contentDescription = null
                                    )

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
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(end = 10.dp)
                                    )
                                    {
                                        Text(
                                            text = "truc",
                                            modifier = Modifier.align(Alignment.CenterEnd),
                                            color = Color.White
                                        )
                                    }

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
                                    }
                            }

                            if (homePageVisible) {
                                LazyVerticalGrid(
                                    columns = GridCells.Adaptive(150.dp),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 10.dp, vertical = 10.dp)
                                        .weight(1f)
                                ) {
                                    val _60Color = Color(0xFF243e36)
                                    val _30Color = Color(0xFF7ca982)
                                    val _10Color = Color(0xFF8fc0a9)

                                    lazyGridItems(
                                        homeViewModel.homeItems.value,
                                        key = { it.id }) { item ->
                                        Card(
                                            modifier = Modifier
                                                .padding(start = 10.dp, end = 10.dp, bottom = 20.dp)
                                                .size(150.dp)
                                                .clip(RoundedCornerShape(13.dp)),
                                            colors = CardDefaults.cardColors(
                                                containerColor = _60Color,
                                                contentColor = _30Color,
                                            ),
                                            elevation = CardDefaults.cardElevation(
                                                defaultElevation = 10.dp
                                            ),
                                            onClick = {
                                                mainViewModel.viewModelScope.launch {
                                                    item.onClick(mainViewModel, homeViewModel)
                                                }
                                            },
                                            border = BorderStroke(2.dp, _10Color),
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(13.dp))
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = item.icon),
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .size(100.dp)
                                                        .align(Alignment.TopCenter)
                                                        .padding(top = 15.dp),
                                                    tint = Color.Unspecified
                                                )

                                                Text(
                                                    text = item.title,
                                                    color = _30Color,
                                                    modifier = Modifier
                                                        .align(Alignment.BottomCenter)
                                                        .padding(bottom = 15.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                val scrollStates = remember { mutableMapOf<String, LazyGridState>() }
                                val currentScrollState =
                                    scrollStates.getOrPut(currentFolder.fullPath) {
                                        LazyGridState()
                                    }

                                LazyVerticalGrid(
                                    columns = GridCells.Adaptive(150.dp),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 10.dp)
                                        .weight(1f),
                                    state = currentScrollState
                                ) {
                                    lazyGridItems(currentFolder.items, key = {
                                        it.fullPath + "-" +
                                                pictureUpdateId + it.id.toString()
                                    }) { item ->
                                        ItemComponent(
                                            viewModel = mainViewModel,
                                            item = item,
                                            modifier = Modifier
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            imageCache = mainViewModel.imageCache,
                                            context = this@MainActivity,
                                            scaleCache = mainViewModel.scaleCache,
                                            flagCache = mainViewModel.flagCache,
                                            memoCache = mainViewModel.memoCache
//                                        onDrop = { tag: ColoredTag ->
//                                            mainViewModel.assignColoredTagToItem(item, tag)
//                                        }
                                        )
                                    }
                                }
                            }

                            if (!homePageVisible) {
                                BottomTools.BottomToolBar(
                                    openTextDialog,
                                    activity = this@MainActivity
                                )
                            }

                            val url by mainViewModel.browserManager.currentPage.collectAsState()

                            BrowserOverlay(
                                currentPage = url,
                                onClose = {
                                    mainViewModel.browserManager.closeBrowser()
                                },
                                onImageClicked = { url ->
                                    mainViewModel.viewModelScope.launch {
                                        manageImageClick(mainViewModel, url)
                                        //génère des problèmes dans manageImageClick
//                            mainViewModel.setSelectedItem(null)
                                        BottomTools.setCurrentContent(DEFAULT)
                                        mainViewModel.setSelectedItem(null, true)
                                        mainViewModel.refreshCurrentFolder()
                                    }
                                },
                                viewmodel = mainViewModel
                            )
                        }

                        if (openTextDialog.value)
                            CustomTextDialog(dialogMessage.value ?: "", openTextDialog) { text ->
                                if (mainViewModel.dialogOnOkLambda != null) {
                                    mainViewModel.viewModelScope.launch {
                                        mainViewModel.dialogOnOkLambda?.invoke(
                                            text,
                                            mainViewModel,
                                            this@MainActivity
                                        )
                                    }
                                    mainViewModel.dialogOnOkLambda = null
                                } else
                                    mainViewModel.viewModelScope.launch {
                                        currentTool?.onClick(mainViewModel, this@MainActivity)
                                    }
                            }

                        if (openYesNoDialog.value) {
                            CustomYesNoDialog(dialogMessage.value ?: "", openYesNoDialog) { yesNo ->
                                if (mainViewModel.dialogYesNoLambda != null) {
                                    mainViewModel.viewModelScope.launch {
                                        mainViewModel.dialogYesNoLambda?.invoke(
                                            yesNo,
                                            mainViewModel,
                                            this@MainActivity
                                        )
                                    }
                                    mainViewModel.dialogYesNoLambda = null
                                } else
                                    mainViewModel.viewModelScope.launch {
                                        currentTool?.onClick(mainViewModel, this@MainActivity)
                                    }
                            }
                        }

                        if (openMoveFileDialog.value) {
                            CustomMoveFileExistingDestinationDialog(
                                openDialog = openMoveFileDialog,
                                onOverwrite = {
                                    val intent =
                                        Intent(this@MainActivity, MoveFileService::class.java).apply {
                                            putExtra("source", BottomTools.movingItem?.fullPath ?: "")
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
                                        Intent(this@MainActivity, MoveFileService::class.java).apply {
                                            putExtra("source", BottomTools.movingItem?.fullPath ?: "")
                                            putExtra("destination", BottomTools.itemToMove?.fullPath)
                                            putExtra("addSuffix", " - copie")
                                        }
                                    startService(intent)
                                    mainViewModel.refreshCurrentFolder()
                                }
                            )
                        }

                        if (openTagInfosDialog.value) {
                            TagInfosDialog(
                                text = dialogMessage.value ?: "",
                                openDialog = openTagInfosDialog,
                                onDatasCompleted = { infos: TagInfos?, model: SigmaViewModel, activity: MainActivity ->
                                    mainViewModel.dialogTagLambda?.invoke(
                                        infos!!,
                                        mainViewModel,
                                        this@MainActivity
                                    )
                                },
                                viewModel = mainViewModel,
                                mainActivity = this@MainActivity
                            )
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
            val croppedBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(resultUri))
            mainViewModel.updatePicture(
                Bitmap.createBitmap(croppedBitmap),
                onlyCropped = true
            )
            mainViewModel.refreshCurrentFolder()
        }

//        mainViewModel.setSelectedItem(null)
//        BottomTools.setCurrentContent(DEFAULT, mainViewModel))
    }

    @Composable
    private fun EditorAction(
        @DrawableRes iconRes: Int,
        active: Boolean,
        onClick: () -> Unit,
    ) {
        IconButton(onClick = onClick) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = iconRes),
                tint = if (active) Color.White else Color.Black,
                contentDescription = null
            )
        }
    }

    private fun Int.hueToColor(saturation: Float = 1f, value: Float = 0.5f): Color = Color(
        ColorUtils.HSLToColor(floatArrayOf(this.toFloat(), saturation, value))
    )
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

object BoldRedStyle : Style.TextStyle {
    override val spanStyle = SpanStyle(
        color = Color.Red,
        fontWeight = FontWeight.Bold,
    )
    override val tag = "BoldRedStyle"
}