package lorry.folder.items.dossiersigma.ui.dialogs

import android.content.Intent
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.basics.domain.str
import lorry.folder.items.dossiersigma.headless.services.MoveFileService
import lorry.folder.items.dossiersigma.headless.usecases.homePage.HomeItem
import lorry.folder.items.dossiersigma.headless.usecases.homePage.HomeUiState
import lorry.folder.items.dossiersigma.ui.browser.IBrowser
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.controller.IToolbarComponent
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.toolbars.DEFAULT
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity
import lorry.folder.items.dossiersigma.ui.sigma.SigmaViewModel

@Composable
context(SigmaActivity, BoxScope)
fun FullSizeExtras(
    browser: IBrowser,
    bottomComponent: IToolbarComponent
) {
    val isTextDialogVisible by mainViewModel.isTextDialogVisible.collectAsState()
    val isYesNoDialogVisible by mainViewModel.isYesNoDialogVisible.collectAsState()
    val isMoveFileDialogVisible by mainViewModel.isMoveFileDialogVisible.collectAsState()
    val isTagInfosDialogVisible by mainViewModel.isTagInfosDialogVisible.collectAsState()
    val isHomeItemDialogVisible by mainViewModel.isHomeItemDialogVisible.collectAsState()
    val isFilePickerVisible by mainViewModel.isFilePickerVisible.collectAsState()
    val dialogMessage = mainViewModel.dialogMessage.collectAsState()
    val currentTool by bottomComponent.currentTool.collectAsState()
    val browserState by browser.vm.state.collectAsState()

    if (isTextDialogVisible)
        CustomTextDialog(
            text = dialogMessage.value ?: "",
            viewModel = mainViewModel,
            initialText = mainViewModel.dialogInitialText.value ?: "",
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
                            bottomComponent.toolsViewModel.movingItem?.fullPath?.str ?: ""
                        )
                        putExtra(
                            "destination",
                            bottomComponent.toolsViewModel.movingItem?.fullPath?.str ?: ""
                        )
                        putExtra("addSuffix", "")
                    }
                startService(intent)
                mainViewModel.folderContentComponent.reloadCurrentFolder()
            },
            onCancel = {
                bottomComponent.toolsViewModel.rawFeed.setCurrentContent(DEFAULT)
                val item = bottomComponent.toolsViewModel.movingItem
                val movingParent = item?.fullPath?.dropLastSegmentOfPath()

                if (movingParent != null)
                    mainViewModel.goToFolder(movingParent)
                bottomComponent.toolsViewModel.movingItem = null
                mainViewModel.setSelectedItem(null, true)
                mainViewModel.folderContentComponent.reloadCurrentFolder()
            },
            onCreateCopy = {
                val intent =
                    Intent(
                        this@SigmaActivity,
                        MoveFileService::class.java
                    ).apply {
                        putExtra(
                            "source",
                            bottomComponent.toolsViewModel.movingItem?.fullPath?.str ?: ""
                        )
                        putExtra(
                            "destination",
                            bottomComponent.toolsViewModel.itemToMove?.fullPath?.str
                        )
                        putExtra("addSuffix", " - copie")
                    }
                startService(intent)
                mainViewModel.folderContentComponent.reloadCurrentFolder()
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

                val uiState = homeViewModel.uiState.value
                if (uiState !is HomeUiState.Ready)
                    return@HomeItemDialog

                val items = (uiState as HomeUiState.Ready).items
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
            modifier = Modifier.Companion
                .align(Alignment.Companion.Center),
            viewModel = mainViewModel
        ) { path ->
            onFolderChosen(path)
        }
    }

    // affichage du #[[browserBody]] si il y a lieu
    //modifié dans [[browserModification]]
    if (browserState.isOpen)
        browser.Render(
            modifier = Modifier.Companion
                .fillMaxSize()
        )
}