package lorry.folder.items.dossiersigma.ui.sigma

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import de.charlex.compose.SpeedDialData
import de.charlex.compose.SpeedDialFloatingActionButton
import de.charlex.compose.SpeedDialFloatingActionButtonState
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.domain.services.MoveToNASService
import java.io.File

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SigmaActivity.SigmaFAB(
    homePageVisible: Boolean,
    isTextDialogVisible: Boolean,
    isYesNoDialogVisible: Boolean,
    isMoveFileDialogVisible: Boolean,
    isFilePickerVisible: Boolean,
    isTagInfosDialogVisible: Boolean,
    fabState: SpeedDialFloatingActionButtonState,
    isSettingsPageVisible: Boolean
) {
    if (!homePageVisible &&
        !isTextDialogVisible &&
        !isYesNoDialogVisible &&
        !isMoveFileDialogVisible &&
        !isFilePickerVisible &&
        !isTagInfosDialogVisible &&
        !isSettingsPageVisible
    )

        Box(
            Modifier
                .padding(bottom = 5.dp, end = 20.dp)
                .height(65.dp)
        ) {

            SpeedDialFloatingActionButton(
                modifier = Modifier,
                initialExpanded = false,
                animationDuration = 300,
                animationDelayPerSelection = 100,
                showLabels = true,
                fabBackgroundColor = SigmaColors.current.secondary,
                fabContentColor = SigmaColors.current.onSecondary,
                speedDialBackgroundColor = SigmaColors.current.secondary,
                speedDialContentColor = SigmaColors.current.onSecondary,
                speedDialData = listOf(
                    SpeedDialData(
                        label = "Dossier -> NAS",
                        painter = painterResource(id = R.drawable.ftp),
                    ) {
                        mainViewModel.viewModelScope.launch {
                            val files = mainViewModel.currentFolder.value.items.map { it.fullPath }
                            val intent =
                                Intent(this@SigmaFAB, MoveToNASService::class.java).apply {
                                    putExtra("filesToTransfer", Gson().toJson(files))
                                    putExtra(
                                        "nasDirectory",
                                        this@SigmaFAB.settingsViewModel.settingsManager.nasFolderFlow.firstOrNull()
                                    )
                                }
                            this@SigmaFAB.startService(intent)
                        }
                    },
                    SpeedDialData(
                        label = "Ajouter dossier",
                        painter = painterResource(id = R.drawable.dossier_plus)
                    ) {
                        mainViewModel.setDialogMessage("Nom du dossier à créer")
                        mainViewModel.dialogOnOkLambda =
                            { newName, viewModel, mainActivity ->
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

                        mainViewModel.setIsTextDialogVisible(true)
                    },
                )
            )
        }
}