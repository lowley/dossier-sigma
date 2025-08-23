package lorry.folder.items.dossiersigma.ui.centralArea

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.google.gson.Gson
import de.charlex.compose.SpeedDialData
import de.charlex.compose.SpeedDialFloatingActionButton
import de.charlex.compose.SpeedDialFloatingActionButtonState
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.headless.moveToNasWorker.MoveToNASWorker
import lorry.folder.items.dossiersigma.headless.serviceVariants.moveToNAS.ManifestEntry
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity
import lorry.folder.items.dossiersigma.ui.sigma.SigmaColors
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
    isSettingsPageVisible: Boolean,
    context: SigmaActivity
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
            Modifier.Companion
                .padding(bottom = 5.dp, end = 20.dp)
                .height(65.dp)
        ) {

            SpeedDialFloatingActionButton(
                modifier = Modifier.Companion,
                initialExpanded = false,
                animationDuration = 300,
                animationDelayPerSelection = 100,
                showLabels = true,
                fabBackgroundColor = SigmaColors.current.secondary,
                fabContentColor = SigmaColors.current.onPrimary,
                speedDialBackgroundColor = SigmaColors.current.secondary,
                speedDialContentColor = SigmaColors.current.tertiary,
                speedDialData = listOf(
                    SpeedDialData(
                        label = "Dossier -> NAS",
                        painter = painterResource(id = R.drawable.ftp),
                    ) {
                        mainViewModel.viewModelScope.launch {
                            val files = mainViewModel.displayedItemsFlow.value.second.map {
                                val picture = mainViewModel.imageCache.value[it.fullPath]
                                val picture64 = if (picture != null && picture is Bitmap)
                                    mainViewModel.base64Embedder.bitmapToBase64(picture as Bitmap)
                                else null

                                it.fullPath to picture64
                            }

                            //* aire des images enregistrées dans un fichier
                            //* pour transfert à CopieurTho2
                            val entries = files.map<Pair<String, String?>, ManifestEntry> {
                                ManifestEntry(fullPath = it.first, picture64 = it.second)
                            }

                            // 2) Écrire le JSON dans un fichier temporaire de cache interne
                            val manifestFile = File(cacheDir, "transfer_manifest.json").apply {
                                writeText(Gson().toJson(entries))
                            }

                            // 3) Obtenir l’URI de partage via FileProvider
                            val authority = "${packageName}.provider"
                            val contentUri =
                                FileProvider.getUriForFile(this@SigmaFAB, authority, manifestFile)
                            //* fin aire des images enregistrées dans un fichier

                            val nasDirectory =
                                this@SigmaFAB.settingsViewModel.settingsManager.nasFolderFlow.firstOrNull()
                                    ?: ""

                            val req = MoveToNASWorker.request(
                                sources = files,
                                manifestPath = manifestFile.absolutePath,
                                target = nasDirectory,
                                manifestUri = contentUri.toString()
                            )

                            WorkManager.getInstance(this@SigmaFAB)
                                .enqueueUniqueWork(
                                    "move-to-nas",
                                    ExistingWorkPolicy.APPEND_OR_REPLACE,
                                    req
                                )

                            WorkManager.getInstance(context)
                                .getWorkInfoByIdLiveData(req.id)
                                .observe(context) { info ->
                                    val items = info?.progress?.getInt(MoveToNASWorker.P_ITEMS, 0)
                                    val index = info?.progress?.getInt(MoveToNASWorker.P_INDEX, 0)
                                    val pct = info?.progress?.getInt(MoveToNASWorker.P_PCT, -1)

                                    context.bottomTools.updateNASProgress(
                                        percentage = pct ?: 0,
                                        fileIndex = index ?: 0,
                                        fileCount = items ?: 0
                                    )
                                }

//                            bottomTools.moveToNASComponent.startService(
//                                filesToTransfer = files,
//                                manifestUri = contentUri.toString(),
//                                nasDirectory = nasDirectory,
//                                changeBottomTools = { percentage: Int, index: Int, total: Int ->
//                                    bottomTools.updateNASProgress(
//                                        percentage = percentage,
//                                        fileIndex = index,
//                                        fileCount = total
//                                    )
//                                }
//                            )
                        }
                    },
                    SpeedDialData(
                        label = "Ajouter dossier",
                        painter = painterResource(id = R.drawable.dossier_plus)
                    ) {
                        mainViewModel.setDialogMessage("Nom du dossier à créer")
                        mainViewModel.dialogOnOkLambda =
                            { newName, viewModel, mainActivity ->
                                val currentFolderPath = viewModel.LastFolderFreshness.value.path
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