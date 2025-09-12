package lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.toolbars

import android.content.Intent
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.headless.services.MoveFileService
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.BottomToolContent
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.Tool
import lorry.folder.items.dossiersigma.ui.sigma.SigmaViewModel
import java.io.File

object MOVE_FILE : Tools() {
    override fun content(viewModel: SigmaViewModel?) = BottomToolContent(
        listOf(
            /////////////
            // annuler //
            /////////////
            Tool(
                text = { "Annuler" },
                icon = R.drawable.annuler,
                onClick = { viewModel, mainActivity ->
                    bottomTools.component.setCurrentContent(DEFAULT)
                    val item = bottomTools.component.movingItem
                    val movingParent = item?.fullPath?.substringBeforeLast("/")

                    if (movingParent != null)
                        viewModel.goToFolder(movingParent)
                    bottomTools.component.movingItem = null
                    viewModel.setSelectedItem(null, true)
//                        viewModel.refreshCurrentFolder()
                }
            ),
            ////////////
            // coller //
            ////////////
            Tool(
                text = {
                    val movePasteText = bottomTools.component.movePasteText.value
                    movePasteText
                },
                icon = R.drawable.coller,
                onClick = { viewModel, mainActivity ->
                    run {
                        bottomTools.component.itemToMove = viewModel.selectedItem.value
                        var dest = bottomTools.component.itemToMove

                        if (dest == null) {
                            return@run
//                                bottomTools.itemToMove = viewModel.currentFolder.value
//                                dest = bottomTools.itemToMove
                        }

                        //toast
                        println("MovingItem: choisir fichier destination")
                        //1.copie
                        val sourceFile = File(bottomTools.component.movingItem?.fullPath ?: "")
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
                            if (bottomTools.component.movingItem == null)
                                return@run
                            val isItemExists = viewModel.diskRepository.isFileOrFolderExists(
                                dest.fullPath,
                                bottomTools.component.movingItem!!
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
                            putExtra("source", bottomTools.component.movingItem?.fullPath ?: "")
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