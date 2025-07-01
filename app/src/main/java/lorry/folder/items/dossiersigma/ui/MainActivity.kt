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
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.PermissionsManager
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.data.intent.DSI_IntentWrapper
import lorry.folder.items.dossiersigma.domain.ColoredTag
import lorry.folder.items.dossiersigma.domain.services.MoveFileService
import lorry.folder.items.dossiersigma.domain.usecases.files.ChangePathUseCase
import lorry.folder.items.dossiersigma.domain.usecases.homePage.HomeViewModel
import lorry.folder.items.dossiersigma.ui.components.BottomTools.Companion.itemToMove
import lorry.folder.items.dossiersigma.ui.components.BottomTools.Companion.movingItem
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

    @OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
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
                        (::openTagInfosDialog.isInitialized && !openTagInfosDialog.value))
                        Button(
                            onClick = {
                                mainViewModel.setDialogMessage("Nom du dossier à créer")
                                mainViewModel.dialogOnOkLambda = { newName, viewModel, mainActivity ->
                                    val currentFolderPath = viewModel.currentFolderPath.value
                                    val newFullName = "$currentFolderPath/$newName"
                                    if (!File(newFullName).exists()) {
                                        if (File(newFullName).mkdir()) {
                                            Toast.makeText(mainActivity, "Répertoire créé", Toast.LENGTH_SHORT).show()
                                            viewModel.refreshCurrentFolder()
                                        } else
                                            Toast.makeText(
                                                mainActivity,
                                                "Un problème est survenu",
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
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
                                .size(60.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF006d77),
                                contentColor = Color(0xFF83c5be)
                                
                            )
                        ) {
                            Icon(
                                painterResource(R.drawable.plus),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(50.dp)
                            )
                        }
                }
            ) { padding ->
                DossierSigmaTheme {

                    //barre d'outils

                    val currentFolder by mainViewModel.currentFolder.collectAsState()
                    val selectedItem by mainViewModel.selectedItem.collectAsState()
                    val activity = LocalContext.current as Activity
                    val pictureUpdateId by mainViewModel.pictureUpdateId.collectAsState()
                    val currentTool by mainViewModel.bottomTools.currentTool.collectAsState()

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
                        mainViewModel.bottomTools.setCurrentContent(DEFAULT)
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
                                "Changement d'image effectué",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }

                    val dragOffset by mainViewModel.dragOffset.collectAsState()
                    val density = LocalDensity.current
                    val draggableStartPosition by mainViewModel.draggableStartPosition.collectAsState()
                    val circlePosition = Offset((draggableStartPosition?.x ?: 0f) + (dragOffset?.x ?: 0f),
                        (draggableStartPosition?.y ?: 0f) + (dragOffset?.y ?: 0f))
                    
//                    Box(
//                        modifier = Modifier
//                            .offset {
//                                IntOffset(0-(dragOffset?.x?.toInt() ?: 0), 0-(dragOffset?.y?.toInt() ?:0))
//                            }
//                            .size(80.dp)
//                            .zIndex(10f)
//                            .background(color = Color.Red, shape = CircleShape)
//                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF363E4C))
                            .pointerInput(selectedItem?.id) {
                                detectTapGestures(onTap = {
                                    if (selectedItem?.id != null) {
                                        mainViewModel.setSelectedItem(null, true)
                                        mainViewModel.bottomTools.setCurrentContent(DEFAULT)
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
                                        items = currentFolder.fullPath.split("/").filter { it != "" },
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

                                lazyGridItems(homeViewModel.homeItems.value, key = { it.id }) { item ->
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
                            val currentScrollState = scrollStates.getOrPut(currentFolder.fullPath) {
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
//                                        onDrop = { tag: ColoredTag ->
//                                            mainViewModel.assignColoredTagToItem(item, tag)
//                                        }
                                    )
                                }
                            }
                        }

                        if (!homePageVisible) {
                            mainViewModel.bottomTools.BottomToolBar(
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
                                    mainViewModel.bottomTools.setCurrentContent(DEFAULT)
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
                                val intent = Intent(this@MainActivity, MoveFileService::class.java).apply {
                                    putExtra("source", movingItem?.fullPath ?: "")
                                    putExtra("destination", movingItem?.fullPath ?: "")
                                    putExtra("addSuffix", "")
                                }
                                startService(intent)
                                mainViewModel.refreshCurrentFolder()
                            },
                            onCancel = {
                                mainViewModel.bottomTools.setCurrentContent(DEFAULT)
                                val item = movingItem
                                val movingParent = item?.fullPath?.substringBeforeLast("/")

                                if (movingParent != null)
                                    mainViewModel.goToFolder(movingParent)
                                movingItem = null
                                mainViewModel.setSelectedItem(null, true)
                                mainViewModel.refreshCurrentFolder()


                            },
                            onCreateCopy = {
                                val intent = Intent(this@MainActivity, MoveFileService::class.java).apply {
                                    putExtra("source", movingItem?.fullPath ?: "")
                                    putExtra("destination", itemToMove?.fullPath)
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
//        mainViewModel.bottomTools.setCurrentContent(DEFAULT, mainViewModel))
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