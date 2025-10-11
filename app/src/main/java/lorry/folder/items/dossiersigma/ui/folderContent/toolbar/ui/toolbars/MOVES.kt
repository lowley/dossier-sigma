package lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.toolbars

import android.graphics.Bitmap
import androidx.core.content.FileProvider
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.google.gson.Gson
import kotlinx.coroutines.flow.firstOrNull
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.headless.moveToNasWorker.MoveToNASWorker
import lorry.folder.items.dossiersigma.headless.serviceVariants.moveToNAS.ManifestEntry
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.ToolbarContent
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.Tool
import java.io.File
import kotlin.collections.get

object MOVES : Tools() {
    override fun content() = ToolbarContent(
        listOf(
            ////////////
            // copier //
            ////////////
            Tool(
                text = { "Copier" },
                icon = R.drawable.copier,
                onClick = { viewModel, mainActivity ->


                    //vm.diskRepository.copyFile(sourceFile, destinationFile)
                }
            ),
            //////////////
            // déplacer //
            //////////////
            Tool(
                text = { "Déplacer" },
                icon = R.drawable.deplacer,
                onClick = { viewModel, mainActivity ->
                    toolBarManager.toolbarComponent.toolsViewModel.movingItem = viewModel.selectedItem.value
                    toolBarManager.toolbarComponent.toolsViewModel.rawFeed.setCurrentContent(MOVE_FILE)
                    viewModel.setSelectedItem(null, keepBottomToolsAsIs = true)
                }
            ),
            /////////////////////
            // déplacement NAS //
            /////////////////////
            Tool(
                text = {
                    val nasText = toolBarManager.toolbarComponent.toolsViewModel.rawFeed.copyNASText.value
                    nasText
                },
                icon = R.drawable.deplacer,
                onClick = { viewModel, mainActivity ->
                    run {
                        toolBarManager.toolbarComponent.toolsViewModel.itemToMove = viewModel.selectedItem.value

                        if (toolBarManager.toolbarComponent.toolsViewModel.itemToMove == null)
                            return@run

                        //toast
                        println("MovingItem: choisir fichier destination")

                        /**
                         * le fichier n'existe pas, on lance la copie,
                         * le reste est effectué dans
                         * @see lorry.folder.items.dossiersigma.headless.services.MoveFileService.onStartCommand
                         */

                        //encode/decode en json

                        /////////////
                        // current //
                        /////////////

                        val picture = toolBarManager.toolbarComponent.toolsViewModel.itemToMove
                            ?.picture

//                            val picture =
//                                viewModel.imageCache.value[viewModel.selectedItemFullPath.value]
                        val picture64 = if (picture != null && picture is Bitmap)
                            viewModel.base64Embedder.bitmapToBase64(picture as Bitmap)
                        else null

                        val filesToTransfer = toolBarManager.toolbarComponent.toolsViewModel.itemToMove?.fullPath?.let {
                            listOf(it to picture64)
                        } ?: emptyList()

                        //* aire des images enregistrées dans un fichier
                        //* pour transfert à CopieurTho2
                        val entries =
                            filesToTransfer.map<Pair<String, String?>, ManifestEntry> {
                                ManifestEntry(fullPath = it.first, picture64 = it.second)
                            }

                        // 2) Écrire le JSON dans un fichier temporaire de cache interne
                        val manifestFile =
                            File(mainActivity.cacheDir, "transfer_manifest.json").apply {
                                writeText(Gson().toJson(entries))
                            }

                        // 3) Obtenir l’URI de partage via FileProvider
                        val authority = "${mainActivity.packageName}.provider"
                        val contentUri =
                            FileProvider.getUriForFile(mainActivity, authority, manifestFile)
                        //* fin aire des images enregistrées dans un fichier

                        val nasDirectory =
                            mainActivity.settingsViewModel.settings.nasFolderFlow.firstOrNull()
                                ?: ""

                        val req = MoveToNASWorker.Companion.request(
                            manifestPath = manifestFile.absolutePath,
                            target = nasDirectory,
                            manifestUri = contentUri.toString(),
                            picture64 = picture64
                        )

                        WorkManager.Companion.getInstance(mainActivity)
                            .enqueueUniqueWork(
                                "move-to-nas",
                                ExistingWorkPolicy.KEEP,
                                req
                            )

//                            bottomTools.moveToNASComponent.startService(
//                                filesToTransfer = filesToTransfer,
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

                        ////////////
                        // legacy //
                        ////////////

//                            val intent = Intent(mainActivity, MoveToNASService::class.java).apply {
//                                putExtra(
//                                    "filesToTransfer", Gson().toJson(
//                                        listOf(
//                                            BottomTools.itemToMove?.fullPath ?: ""
//                                        )
//                                    )
//                                )
//                                putExtra(
//                                    "nasDirectory",
//                                    mainActivity.settingsViewModel.settingsManager.nasFolderFlow.firstOrNull()
//                                )
//                            }
//                            mainActivity.startService(intent)
                    }
                }
            )
        ),
        "MOVES"
    )
}