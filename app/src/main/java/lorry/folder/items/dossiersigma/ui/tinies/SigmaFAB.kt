package lorry.folder.items.dossiersigma.ui.tinies

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.gson.Gson
import com.leinardi.android.speeddial.compose.FabWithLabel
import com.leinardi.android.speeddial.compose.SpeedDial
import com.leinardi.android.speeddial.compose.SpeedDialState
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.basics.domain.SigmaPath
import lorry.folder.items.dossiersigma.basics.domain.str
import lorry.folder.items.dossiersigma.basics.domain.toSigmaPath
import lorry.folder.items.dossiersigma.headless.moveToNasWorker.MoveToNASWorker
import lorry.folder.items.dossiersigma.headless.serviceVariants.moveToNAS.ManifestEntry
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity
import lorry.folder.items.dossiersigma.ui.sigma.SigmaColors
import java.io.File
import kotlin.collections.map

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SigmaActivity.SigmaFAB(
    homePageVisible: Boolean,
    isTextDialogVisible: Boolean,
    isYesNoDialogVisible: Boolean,
    isMoveFileDialogVisible: Boolean,
    isFilePickerVisible: Boolean,
    isTagInfosDialogVisible: Boolean,
    fabState: MutableState<SpeedDialState>,
    overlayVisible: MutableState<Boolean>,
    isSettingsPageVisible: Boolean,
    context: SigmaActivity,

    ) {

    val workManager = remember { WorkManager.getInstance(this@SigmaActivity) }
    val infos by workManager.getWorkInfosByTagLiveData("move-to-nas-active")
        .observeAsState(initial = emptyList())

    // ➜ pousse le progress vers bottomTools à chaque changement
    LaunchedEffect(infos) {
        val active = infos
            .firstOrNull { it.state == WorkInfo.State.RUNNING }
            ?: infos.firstOrNull { it.state == WorkInfo.State.ENQUEUED }

        if (active != null) {
            val items = active.progress.getInt(MoveToNASWorker.P_ITEMS, 0)
            val index = active.progress.getInt(MoveToNASWorker.P_INDEX, 0)
            val pct = active.progress.getInt(MoveToNASWorker.P_PCT, 0)

            toolsViewModel.rawFeed.updateNASProgress(
                percentage = pct.coerceAtLeast(0),
                fileIndex = index,
                fileCount = items
            )
        }
    }

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
        ) {

            SpeedDial(
                modifier = Modifier
                    .alpha(0.8f),
                state = fabState.value,
                fabOpenedBackgroundColor = SigmaColors.current.secondary,
                fabOpenedContentColor = SigmaColors.current.onSecondary,
                fabClosedBackgroundColor = SigmaColors.current.secondary,
                fabClosedContentColor = SigmaColors.current.onSecondary,
                onFabClick = { expanded ->
                    overlayVisible.value = !expanded
                    fabState.value = if (expanded) SpeedDialState.Collapsed
                    else SpeedDialState.Expanded
                }
            ) {
                item {
                    FabWithLabel(
                        modifier = Modifier
                            .alpha(1f),
                        labelBackgroundColor = SigmaColors.current.secondary,
//                            lerp(
//                            SigmaColors.current.primary,
//                            SigmaColors.current.secondary,
//                            0.3f
//                            )
                        fabBackgroundColor = SigmaColors.current.primary,
                        labelContent = {
                            Text(
                                modifier = Modifier,
                                text = "Dossier -> NAS",
                                color = SigmaColors.current.onSecondary
                            )
                        },
//                        fabBackgroundColor = SigmaColors.current.primary,
                        onClick = {
                            overlayVisible.value = false
                            fabState.value = SpeedDialState.Collapsed
                            copyEntireFolderToNAS()
                        }
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(25.dp),
                            painter = painterResource(id = R.drawable.chaine),
                            contentDescription = null,
                            tint = SigmaColors.current.secondary
                        )
                    }
                }

                item {
                    FabWithLabel(
                        modifier = Modifier
                            .alpha(1f),
                        labelBackgroundColor = SigmaColors.current.secondary,
//                            lerp(
//                            SigmaColors.current.primary,
//                            SigmaColors.current.secondary,
//                            0.3f
//                        ),
                        fabBackgroundColor = SigmaColors.current.primary,
                        labelContent = {
                            Text(
                                modifier = Modifier,
                                text = "Créer dossier",
                                color = SigmaColors.current.onSecondary
                            )
                        },
//                        fabBackgroundColor = SigmaColors.current.primary,
                        onClick = {
                            overlayVisible.value = false
                            fabState.value = SpeedDialState.Collapsed
                            createFolder()
                        }
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(25.dp),
                            painter = painterResource(id = R.drawable.add_folder),
                            contentDescription = null,
                            tint = SigmaColors.current.secondary
                        )
                    }
                }
            }
        }
}

/////////////////////////////////////////
// reste du code: méthodes utilitaires //
/////////////////////////////////////////

private fun SigmaActivity.copyEntireFolderToNAS() {
    mainViewModel.viewModelScope.launch {
        val currentFolderItems = mainViewModel.folderContentComponent
                    .currentFolderFlow.value?.items ?: emptyList()

        var picture64: String? = null

        val files = currentFolderItems.map {
            val picture = it.picture
            picture64 = if (picture != null && picture is Bitmap)
                mainViewModel.base64Embedder.bitmapToBase64(picture as Bitmap)
            else null

            it.fullPath to picture64
        }

        //* aire des images enregistrées dans un fichier
        //* pour transfert à CopieurTho2
        val entries = files.map<Pair<SigmaPath, String?>, ManifestEntry> {
            ManifestEntry(fullPath = it.first, picture64 = it.second)
        }

        // 2) Écrire le JSON dans un fichier temporaire de cache interne
        val manifestFile = File(cacheDir, "transfer_manifest.json").apply {
            writeText(Gson().toJson(entries))
        }

        // 3) Obtenir l’URI de partage via FileProvider
        val authority = "${packageName}.provider"
        val contentUri =
            FileProvider.getUriForFile(
                this@SigmaActivity,
                authority,
                manifestFile
            )
        //* fin aire des images enregistrées dans un fichier

        val nasDirectory =
            this@SigmaActivity.settingsViewModel.settings.nasFolderFlow.firstOrNull() ?: "".toSigmaPath()

        val req = MoveToNASWorker.request(
            manifestPath = manifestFile.absolutePath,
            target = nasDirectory.str,
            manifestUri = contentUri.toString(),
            picture64 = picture64
        )

        WorkManager.getInstance(this@SigmaActivity)
            .enqueueUniqueWork(
                "move-to-nas",
                ExistingWorkPolicy.KEEP,
                req
            )

        // Prends la plus récente en cours
//                            val active = infos
//                                .filter { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
//                                .maxByOrNull { it.runAttemptCount } ?: infos.maxByOrNull { it.runAttemptCount }
//
//                            val items = active?.progress?.getInt(MoveToNASWorker.P_ITEMS, 0) ?: 0
//                            val index = active?.progress?.getInt(MoveToNASWorker.P_INDEX, 0) ?: 0
//                            val pct   = active?.progress?.getInt(MoveToNASWorker.P_PCT, -1) ?: -1
//
//                                    context.bottomTools.updateNASProgress(
//                                        percentage = pct ?: 0,
//                                        fileIndex = index ?: 0,
//                                        fileCount = items ?: 0
//                                    )

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
}

private fun SigmaActivity.createFolder() {
    mainViewModel.setDialogMessage("Nom du dossier à créer")
    mainViewModel.dialogOnOkLambda =
        { newName, viewModel, mainActivity ->
            val currentFolderPath = viewModel.folderContentComponent
                ?.currentFolderFlow?.value
                ?.fullPath

            val newFullName = "$currentFolderPath/$newName"

            if (!File(newFullName).exists()) {
                if (File(newFullName).mkdir()) {
                    Toast.makeText(
                        mainActivity,
                        "Répertoire créé",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.folderContentComponent.reloadCurrentFolder()
                } else
                    Toast.makeText(
                        mainActivity,
                        "Un problème est survenu",
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }

    mainViewModel.setIsTextDialogVisible(true)
}

