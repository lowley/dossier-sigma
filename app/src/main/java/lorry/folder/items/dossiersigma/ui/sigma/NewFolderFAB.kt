package lorry.folder.items.dossiersigma.ui.sigma

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import lorry.folder.items.dossiersigma.R
import java.io.File

@Composable
fun SigmaActivity.NewFolderFAB(
    homePageVisible: Boolean,
    isTextDialogVisible: Boolean,
    isYesNoDialogVisible: Boolean,
    isMoveFileDialogVisible: Boolean,
    isFilePickerVisible: Boolean,
    isTagInfosDialogVisible: Boolean
) {
    if (!homePageVisible &&
        !isTextDialogVisible &&
        !isYesNoDialogVisible &&
        !isMoveFileDialogVisible &&
        !isFilePickerVisible &&
        !isTagInfosDialogVisible
    )
        Button(
            onClick = {
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
            shape = RoundedCornerShape(30.dp),
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