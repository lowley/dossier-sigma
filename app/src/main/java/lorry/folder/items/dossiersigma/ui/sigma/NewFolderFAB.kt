package lorry.folder.items.dossiersigma.ui.sigma

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import de.charlex.compose.FloatingActionButtonItem
import de.charlex.compose.SubSpeedDialFloatingActionButtons
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.domain.services.MoveToNASService
import java.io.File

@Composable
fun SigmaActivity.NewFolderFAB(
    homePageVisible: Boolean,
    isTextDialogVisible: Boolean,
    isYesNoDialogVisible: Boolean,
    isMoveFileDialogVisible: Boolean,
    isFilePickerVisible: Boolean,
    isTagInfosDialogVisible: Boolean,
    fabState: de.charlex.compose.SpeedDialFloatingActionButtonState
) {


//    if (!homePageVisible &&
//        !isTextDialogVisible &&
//        !isYesNoDialogVisible &&
//        !isMoveFileDialogVisible &&
//        !isFilePickerVisible &&
//        !isTagInfosDialogVisible
//    )

    SubSpeedDialFloatingActionButtons(
        //modifier = Modifier.padding(bottom = 55.dp, end = 20.dp),
        state = fabState,
        items = listOf(
            FloatingActionButtonItem(
                icon = ImageVector.vectorResource(R.drawable.dossier_plus),
                label = "Ajouter dossier"
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
            FloatingActionButtonItem(
                icon = ImageVector.vectorResource(R.drawable.ftp),
                label = "Dossier -> NAS"
            ) {
                mainViewModel.viewModelScope.launch {
                    val files = mainViewModel.currentFolder.value.items.map { it.fullPath }
                    val intent = Intent(this@NewFolderFAB, MoveToNASService::class.java).apply {
                        putExtra("filesToTransfer", Gson().toJson(files))
                        putExtra("nasDirectory", this@NewFolderFAB.settingsViewModel.settingsManager.nasFolderFlow.firstOrNull())
                    }
                    this@NewFolderFAB.startService(intent)
                }
            }
        )
    )

//        Button(
//            onClick = {
//                mainViewModel.setDialogMessage("Nom du dossier à créer")
//                mainViewModel.dialogOnOkLambda =
//                    { newName, viewModel, mainActivity ->
//                        val currentFolderPath = viewModel.currentFolderPath.value
//                        val newFullName = "$currentFolderPath/$newName"
//
//                        if (!File(newFullName).exists()) {
//                            if (File(newFullName).mkdir()) {
//                                Toast.makeText(
//                                    mainActivity,
//                                    "Répertoire créé",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                                viewModel.refreshCurrentFolder()
//                            } else
//                                Toast.makeText(
//                                    mainActivity,
//                                    "Un problème est survenu",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                        }
//                    }
//
//                mainViewModel.setIsTextDialogVisible(true)
//            },
//            shape = RoundedCornerShape(30.dp),
//            modifier = Modifier
//                .padding(bottom = 55.dp, end = 20.dp)
//                .size(60.dp)
//                .alpha(0.5f),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = Color(0xFF006d77),
//                contentColor = Color(0xFF83c5be)
//            )
//        ) {
//            Icon(
//                painter = painterResource(R.drawable.plus),
//                contentDescription = null,
//                modifier = Modifier
//                    .size(50.dp)
//            )
//        }
}