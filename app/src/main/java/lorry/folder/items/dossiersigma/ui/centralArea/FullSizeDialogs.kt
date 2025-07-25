package lorry.folder.items.dossiersigma.ui.centralArea

import android.content.Intent
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.domain.services.MoveFileService
import lorry.folder.items.dossiersigma.domain.usecases.homePage.HomeItem
import lorry.folder.items.dossiersigma.ui.bottomArea.BottomTools
import lorry.folder.items.dossiersigma.ui.bottomArea.CustomMoveFileExistingDestinationDialog
import lorry.folder.items.dossiersigma.ui.bottomArea.CustomTextDialog
import lorry.folder.items.dossiersigma.ui.bottomArea.CustomYesNoDialog
import lorry.folder.items.dossiersigma.ui.bottomArea.FolderChooserDialog
import lorry.folder.items.dossiersigma.ui.bottomArea.HomeItemDialog
import lorry.folder.items.dossiersigma.ui.bottomArea.HomeItemInfos
import lorry.folder.items.dossiersigma.ui.bottomArea.TagInfos
import lorry.folder.items.dossiersigma.ui.bottomArea.TagInfosDialog
import lorry.folder.items.dossiersigma.ui.bottomArea.Tools.DEFAULT
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity
import lorry.folder.items.dossiersigma.ui.sigma.SigmaViewModel

@Composable
context(SigmaActivity, BoxScope)
fun FullSizeDialogs(

) {
    val isTextDialogVisible by mainViewModel.isTextDialogVisible.collectAsState()
    val isYesNoDialogVisible by mainViewModel.isYesNoDialogVisible.collectAsState()
    val isMoveFileDialogVisible by mainViewModel.isMoveFileDialogVisible.collectAsState()
    val isTagInfosDialogVisible by mainViewModel.isTagInfosDialogVisible.collectAsState()
    val isHomeItemDialogVisible by mainViewModel.isHomeItemDialogVisible.collectAsState()
    val isFilePickerVisible by mainViewModel.isFilePickerVisible.collectAsState()
    val dialogMessage = mainViewModel.dialogMessage.collectAsState()
    val currentTool by BottomTools.currentTool.collectAsState()

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