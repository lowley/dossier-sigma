package lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.toolbars

import android.content.Intent
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.headless.services.MoveFileService
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.BottomToolContent
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.Tool
import java.io.File

object MOVE_FILE : Tools() {
    override fun content() = BottomToolContent(
        listOf(
            /////////////
            // annuler //
            /////////////
            Tool(
                text = { "Annuler" },
                icon = R.drawable.annuler,
                onClick = { viewModel, mainActivity ->
                    bottomTools.bottomComponent.toolsViewModel.rawFeed.setCurrentContent(DEFAULT)
                    val item = bottomTools.bottomComponent.toolsViewModel.movingItem
                    val movingParent = item?.fullPath?.substringBeforeLast("/")

                    if (movingParent != null)
                        viewModel.goToFolder(movingParent)
                    bottomTools.bottomComponent.toolsViewModel.movingItem = null
                    viewModel.setSelectedItem(null, true)
//                        viewModel.refreshCurrentFolder()
                }
            ),
            ////////////
            // coller //
            ////////////
            Tool(
                text = {
                    val movePasteText = bottomTools.bottomComponent.toolsViewModel.rawFeed.movePasteText.value
                    movePasteText
                },
                icon = R.drawable.coller,
                onClick = { viewModel, mainActivity ->
                    run {
                        bottomTools.bottomComponent.toolsViewModel.itemToMove = viewModel.selectedItem.value
                        var dest = bottomTools.bottomComponent.toolsViewModel.itemToMove

                        if (dest == null) {
                            return@run
//                                bottomTools.itemToMove = viewModel.currentFolder.value
//                                dest = bottomTools.itemToMove
                        }

                        //toast
                        println("MovingItem: choisir fichier destination")
                        //1.copie
                        val sourceFile = File(bottomTools.bottomComponent.toolsViewModel.movingItem?.fullPath ?: "")
                        //créer service avec notification(avec avancement)
                        //dans le service: copie
                        //passer au service une lambda pour l'action de retour(2.+3.)

                        //Toast pour informer de déplacement:
                        //début copie, fin déplacement/échec

                        if (dest!!.isFile()) {
                            if (sourceFile.path.substringAfterLast("/")
                                == dest.fullPath.substringAfterLast("/")
                            ) {
                                viewModel.setIsMoveFileDialogVisible(true)
                                return@run
                            }
                        }

                        if (dest.isFolder()) {
                            if (bottomTools.bottomComponent.toolsViewModel.movingItem == null)
                                return@run
                            val isItemExists = viewModel.diskRepository.isFileOrFolderExists(
                                dest.fullPath,
                                bottomTools.bottomComponent.toolsViewModel.movingItem!!
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
                            putExtra("source", bottomTools.bottomComponent.toolsViewModel.movingItem?.fullPath ?: "")
                            putExtra("destination", dest.fullPath)
                            putExtra("addSuffix", "")
                        }
                        mainActivity.startService(intent)
//                            viewModel.setSelectedItem(null, true)
                        viewModel.folderContentComponent.reloadCurrentFolder()
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