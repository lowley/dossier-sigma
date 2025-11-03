package lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.toolbars

import android.content.Intent
import androidx.core.content.ContextCompat
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.basics.domain.lastSegment
import lorry.folder.items.dossiersigma.basics.domain.str
import lorry.folder.items.dossiersigma.headless.services.MoveFileService
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.ToolbarContent
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.Tool

object MOVE_FILE : Tools() {
    override fun content() = ToolbarContent(
        listOf(
            /////////////
            // annuler //
            /////////////
            Tool(
                text = { "Annuler" },
                icon = R.drawable.annuler,
                onClick = { viewModel, mainActivity ->
                    toolBarManager.toolbarComponent.toolsViewModel.rawFeed.setCurrentContent(DEFAULT)
                    val item = toolBarManager.toolbarComponent.toolsViewModel.movingItem
                    val movingParent = item?.fullPath?.dropLastSegmentOfPath()

                    if (movingParent != null)
                        viewModel.goToFolder(movingParent)
                    toolBarManager.toolbarComponent.toolsViewModel.movingItem = null
                    viewModel.setSelectedItem(null, true)
//                        viewModel.refreshCurrentFolder()
                }
            ),
            ////////////
            // coller //
            ////////////
            Tool(
                text = {
                    val movePasteText = toolBarManager.toolbarComponent.toolsViewModel.rawFeed.movePasteText.value
                    movePasteText
                },
                icon = R.drawable.coller,
                onClick = { viewModel, mainActivity ->
                    run {
                        val selectedItem = viewModel.selectedItem.value
                        toolBarManager.toolbarComponent.toolsViewModel.itemToMove = selectedItem
                        val dest = selectedItem ?: viewModel.folderContentComponent?.currentFolderFlow?.value

                        if (dest == null) {
                            return@run
//                                bottomTools.itemToMove = viewModel.currentFolder.value
//                                dest = bottomTools.itemToMove
                        }

                        //toast
                        println("MovingItem: choisir fichier destination")
                        //1.copie
                        val sourceFile = toolBarManager.toolbarComponent.toolsViewModel.movingItem?.fullPath?.toFile()
                            ?: return@run
                        //créer service avec notification(avec avancement)
                        //dans le service: copie
                        //passer au service une lambda pour l'action de retour(2.+3.)

                        //Toast pour informer de déplacement:
                        //début copie, fin déplacement/échec

                        if (dest!!.isFile()) {
                            if (sourceFile.path.substringAfterLast("/")
                                == dest.fullPath.lastSegment
                            ) {
                                viewModel.setIsMoveFileDialogVisible(true)
                                return@run
                            }
                        }

                        if (dest.isFolder()) {
                            if (toolBarManager.toolbarComponent.toolsViewModel.movingItem == null)
                                return@run
                            val isItemExists = viewModel.diskRepository.isFileOrFolderExists(
                                dest.fullPath,
                                toolBarManager.toolbarComponent.toolsViewModel.movingItem!!
                            )
                            if (isItemExists) {
                                viewModel.setIsMoveFileDialogVisible(true)
                                return@run
                            }
                        }

                        /**
                         * le fichier n'existe pas, on lance la copie,
                         * le reste est effectué dans
                         * @see MoveFileService.onStartCommand
                         */
                        val intent = Intent(mainActivity, MoveFileService::class.java).apply {
                            putExtra("source", toolBarManager.toolbarComponent.toolsViewModel.movingItem?.fullPath?.str ?: "")
                            putExtra("destination", dest.fullPath.str)
                            putExtra("addSuffix", "")
                        }
                        ContextCompat.startForegroundService(mainActivity, intent)
//                            viewModel.setSelectedItem(null, true)
                        viewModel.folderContentComponent.reloadCurrentFolderByRefresh2()
                        //2.vérif copie bien réalisée:
                        //dest existe
                        //tailles égales

                        //3.si ok: suppression source


                        //vm.diskRepository.copyFile(sourceFile, destinationFile)
//                        bottomTools.setCurrentContent(DEFAULT, viewModel)
//                        MovingItem = null
                    }
                }
            )
        ),
        "MOVE_FILE"
    )
}