package lorry.folder.items.dossiersigma.ui.sigma

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSizeDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.elixer.palette.Presets
import com.elixer.palette.composables.Palette
import com.elixer.palette.constraints.HorizontalAlignment
import com.elixer.palette.constraints.VerticalAlignment
import com.jaiselrahman.filepicker.activity.FilePickerActivity
import com.jaiselrahman.filepicker.config.Configurations
import com.jaiselrahman.filepicker.model.MediaFile
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.PermissionsManager
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.data.dataSaver.CompositeManager
import lorry.folder.items.dossiersigma.data.dataSaver.Memo
import lorry.folder.items.dossiersigma.data.intent.DSI_IntentWrapper
import lorry.folder.items.dossiersigma.domain.services.MoveFileService
import lorry.folder.items.dossiersigma.domain.usecases.files.ChangePathUseCase
import lorry.folder.items.dossiersigma.domain.usecases.homePage.HomeItem
import lorry.folder.items.dossiersigma.domain.usecases.homePage.HomeViewModel
import lorry.folder.items.dossiersigma.ui.components.BottomTools
import lorry.folder.items.dossiersigma.ui.components.BottomTools.viewModel
import lorry.folder.items.dossiersigma.ui.components.Breadcrumb
import lorry.folder.items.dossiersigma.ui.components.BrowserOverlay
import lorry.folder.items.dossiersigma.ui.components.CustomMoveFileExistingDestinationDialog
import lorry.folder.items.dossiersigma.ui.components.CustomTextDialog
import lorry.folder.items.dossiersigma.ui.components.CustomYesNoDialog
import lorry.folder.items.dossiersigma.ui.components.FolderChooserDialog
import lorry.folder.items.dossiersigma.ui.components.HomeItemDialog
import lorry.folder.items.dossiersigma.ui.components.HomeItemInfos
import lorry.folder.items.dossiersigma.ui.components.ItemComponent
import lorry.folder.items.dossiersigma.ui.components.TagInfos
import lorry.folder.items.dossiersigma.ui.components.TagInfosDialog
import lorry.folder.items.dossiersigma.ui.components.Tools.DEFAULT
import lorry.folder.items.dossiersigma.ui.memoEditor.MemoEditor
import lorry.folder.items.dossiersigma.ui.settings.SettingsViewModel
import lorry.folder.items.dossiersigma.ui.settings.settingsPage
import lorry.folder.items.dossiersigma.ui.theme.DossierSigmaTheme
import javax.inject.Inject
import kotlin.random.Random


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
    var onFolderChoosed: (String?) -> Unit = {}
    var onGotBrowserImage: (String) -> Unit = {}

    /**
     * Appelée par la boîte de dialogue de création / modification de HomeItem
     * @see HomeItemDialog
     */
    val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // La permission a été accordée, on peut maintenant lancer le sélecteur de fichiers
            launchFilePicker()
        } else {
            // L'utilisateur a refusé la permission.
            Toast.makeText(this, "Permission de lecture des fichiers refusée", Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun launchFilePicker() {
        val intent = Intent(this, FilePickerActivity::class.java)
        intent.putExtra(
            FilePickerActivity.CONFIGS,
            Configurations.Builder()
                .setCheckPermission(false) // On le laisse à false, car on gère nous-mêmes
                .setShowFiles(true)
                .setMaxSelection(1)
                .build()
        )
        // Utilisez votre lanceur moderne pour le résultat du fichier
//        filePickerLauncher.launch(intent)
        ActivityCompat.startActivityForResult(sigmaActivity, intent, FILE_REQUEST_CODE, null)

    }

    fun checkPermissionAndLaunchPicker() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                // La permission est déjà accordée
                launchFilePicker()
            }

            else -> {
                // La permission n'est pas accordée, on la demande
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // La permission a été accordée, on peut maintenant lancer le sélecteur de fichiers
            launchFilePicker()
        } else {
            // L'utilisateur a refusé la permission.
            Toast.makeText(this, "Permission de lecture des fichiers refusée", Toast.LENGTH_SHORT)
                .show()
        }
    }

    val sigmaActivity = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionsManager = PermissionsManager()
        if (!permissionsManager.hasExternalStoragePermission())
            permissionsManager.requestExternalStoragePermission(this)

        window.navigationBarColor = ContextCompat.getColor(this, R.color.background)
        initializeFileIntentLauncher(mainViewModel)

        setContent {
            val homePageVisible by homeViewModel.homePageVisible.collectAsState()

            val isTextDialogVisible by mainViewModel.isTextDialogVisible.collectAsState()
            val isYesNoDialogVisible by mainViewModel.isYesNoDialogVisible.collectAsState()
            val isMoveFileDialogVisible by mainViewModel.isMoveFileDialogVisible.collectAsState()
            val isTagInfosDialogVisible by mainViewModel.isTagInfosDialogVisible.collectAsState()
            val isHomeItemDialogVisible by mainViewModel.isHomeItemDialogVisible.collectAsState()
            val isFilePickerVisible by mainViewModel.isFilePickerVisible.collectAsState()

            Scaffold(
                floatingActionButton = {
                    NewFolderFAB(
                        homePageVisible = homePageVisible,
                        isTextDialogVisible = isTextDialogVisible,
                        isYesNoDialogVisible = isYesNoDialogVisible,
                        isMoveFileDialogVisible = isMoveFileDialogVisible,
                        isTagInfosDialogVisible = isTagInfosDialogVisible,
                        isFilePickerVisible = isFilePickerVisible
                    )
                }
            ) { padding ->
                DossierSigmaTheme {

                    val currentFolder by mainViewModel.currentFolder.collectAsState()
                    val selectedItem by mainViewModel.selectedItem.collectAsState()
                    val activity = LocalContext.current as Activity
                    val currentTool by BottomTools.currentTool.collectAsState()

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
                            .fillMaxSize()
                            .zIndex(20f)
                    ) {
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
                                                        mainViewModel.setIsSettingsPageVisible(false)
                                                        homeViewModel.setHomePageVisible(true)
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
                                        Row(
                                            modifier = Modifier
                                                .width(IntrinsicSize.Min)
                                                .align(Alignment.CenterEnd),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {

                                            //ajouter un homeItem
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
                                                                homeViewModel.setDialogHomeItemInfos(
                                                                    HomeItemInfos(
                                                                        oldTitle = "",
                                                                        newTitle = "",
                                                                        picture = null,
                                                                        path = ""
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

                            val isSettingsPageVisible by mainViewModel.isSettingsPageVisible.collectAsState()

                            if (isSettingsPageVisible)
                                settingsPage(vm = settingsViewModel)


                            if (homePageVisible) {
                                val homeItems by homeViewModel.homeItems.collectAsState(emptyList())

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

                                    lazyGridItems<HomeItem>(
                                        homeItems,
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
                                            border = BorderStroke(2.dp, _10Color),
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(13.dp))
                                            ) {
                                                AsyncImage(
                                                    modifier = Modifier
                                                        .size(120.dp)
                                                        .align(Alignment.TopCenter)
                                                        .padding(top = 27.dp)
                                                        .clickable {
                                                            mainViewModel.goToFolder(item.path)
                                                            homeViewModel.setHomePageVisible(false)
                                                        },
                                                    model = item.picture
                                                        ?: if (item.icon != 0) item.icon else R.drawable.dossier,
                                                    contentDescription = "Miniature",
                                                    contentScale = ContentScale.Fit,
                                                )

                                                Text(
                                                    text = item.title,
                                                    color = _30Color,
                                                    modifier = Modifier
                                                        .align(Alignment.BottomCenter)
                                                        .padding(bottom = 5.dp)
                                                        .clickable {
                                                            mainViewModel.goToFolder(item.path)
                                                            homeViewModel.setHomePageVisible(false)
                                                        }
                                                )

                                                //icône de modification
                                                Icon(
                                                    modifier = Modifier
                                                        .size(25.dp)
                                                        .padding(
                                                            start = 10.dp,
                                                            top = 10.dp
                                                        )
                                                        .align(Alignment.TopStart)
                                                        .pointerInput(true) {
                                                            detectTapGestures(
                                                                onTap = {
                                                                    homeViewModel.setDialogHomeItemInfos(
                                                                        HomeItemInfos(
                                                                            oldTitle = item.title,
                                                                            picture = item.picture,
                                                                            path = item.path
                                                                        )
                                                                    )

                                                                    mainViewModel.setIsHomeItemDialogVisible(
                                                                        true
                                                                    )
                                                                }
                                                            )
                                                        },
                                                    painter = painterResource(R.drawable.stylo),
                                                    tint = Color.Gray,
                                                    contentDescription = null
                                                )

                                                //icône de suppression
                                                Icon(
                                                    modifier = Modifier
                                                        .size(25.dp)
                                                        .padding(
                                                            end = 10.dp,
                                                            top = 10.dp
                                                        )
                                                        .align(Alignment.TopEnd)
                                                        .pointerInput(true) {
                                                            detectTapGestures(
                                                                onTap = {
                                                                    homeViewModel.removeHomeItem(
                                                                        item
                                                                    )
                                                                }
                                                            )
                                                        },
                                                    painter = painterResource(R.drawable.corbeille),
                                                    tint = Color.Gray,
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                val scrollStates =
                                    remember { mutableMapOf<String, LazyGridState>() }
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
                                        it.fullPath + "-" + it.id
                                    }) { item ->
                                        ItemComponent(
                                            viewModel = mainViewModel,
                                            item = item,
                                            modifier = Modifier
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            onItemUpdated = { item ->
//                                                mainViewModel.updateItemInList(item)
                                            }
//                                        onDrop = { tag: ColoredTag ->
//                                            mainViewModel.assignColoredTagToItem(item, tag)
//                                        }
                                        )
                                    }
                                }
                            }

                            if (!homePageVisible) {
                                BottomTools.BottomToolBar(
                                    activity = this@SigmaActivity
                                )
                            }

                            val url by mainViewModel.browserManager.currentPage.collectAsState()

                            BrowserOverlay(
                                currentPage = url,
                                onClose = {
                                    mainViewModel.browserManager.closeBrowser()
                                },
                                onImageClicked = { url ->
                                    onGotBrowserImage(url)
                                },
                                viewmodel = mainViewModel
                            )
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
                                            it(
                                                mainViewModel,
                                                this@SigmaActivity
                                            )
                                        }
                                    }
                            }

                        if (isYesNoDialogVisible) {
                            CustomYesNoDialog(dialogMessage.value ?: "", mainViewModel) { yesNo ->
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
                                            it(
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
                                onFolderChoosed(path)
                            }
                        }
                    }

                    val richTextState = rememberRichTextState()

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(20f)
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
                                    .fillMaxSize()
                                    .zIndex(25f),
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

        if (resultCode == FILE_REQUEST_CODE) {
            val files: ArrayList<MediaFile>? = data?.getParcelableArrayListExtra(
                FilePickerActivity.MEDIA_FILES,
                MediaFile::class.java
            )

            if (files.isNullOrEmpty())
                Log.d(TAG, "onActivityResult: rien de retourné")
            else
                Log.d(TAG, "onActivityResult: ${files[0].path}")

            return
        }

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
