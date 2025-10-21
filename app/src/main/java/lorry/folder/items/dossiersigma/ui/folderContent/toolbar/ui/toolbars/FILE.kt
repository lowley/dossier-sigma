package lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.toolbars

import android.widget.Toast
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.headless.domain.SigmaPath
import lorry.folder.items.dossiersigma.headless.domain.lastSegment
import lorry.folder.items.dossiersigma.ui.browser.changeState
import lorry.folder.items.dossiersigma.ui.browser.manageImageClick
import lorry.folder.items.dossiersigma.ui.browser.utilities.BrowserTarget
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.ToolbarContent
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.Tool
import lorry.folder.items.dossiersigma.ui.sigma.SortingCriterion
import java.io.File
import kotlin.text.substringAfterLast
import kotlin.text.substringBeforeLast

object FILE : Tools() {
    override fun content() = ToolbarContent(
        toolInit = listOf(
            ///////////
            // moves //
            ///////////
            Tool(
                text = { "Déplacements" },
                icon = R.drawable.move,
                isColoredIcon = true,
                onClick = { viewModel, mainActivity ->
                    toolBarManager.toolbarComponent.toolsViewModel.rawFeed.setCurrentContent(MOVES)
                }
            ),
            ///////////////
            // tags menu //
            ///////////////
            Tool(
                text = { "Etiquettes" },
                icon = R.drawable.etiquette2,
                isColoredIcon = true,
                onClick = { viewModel, mainActivity ->
                    toolBarManager.toolbarComponent.toolsViewModel.rawFeed.setCurrentContent(TAGS_MENU)
                }
            ),
            //////////////////
            // image google //
            //////////////////
            Tool(
                text = { "Google" },
                icon = R.drawable.browser,
                onClick = { viewModel, mainActivity ->
                    run {
                        val selectedItem = viewModel.selectedItem.value
                        if (selectedItem == null)
                            return@run

                        toolBarManager.toolbarComponent.toolsViewModel.rawFeed.setCurrentContent(DEFAULT)

                        //le [[browserBody]] dépend de browserState (dataclass)
                        //ici il y a #[[browserModification]]
                        mainActivity.browser.changeState(
                            isOpen = true,
                            item = selectedItem,
                            target = BrowserTarget.GOOGLE,
                            onImageClicked = { url ->
                                viewModel.viewModelScope.launch {
                                    manageImageClick(viewModel, url)
                                    viewModel.setSelectedItem(null, true)
                                }

                            }
                        )
                    }
                }
            ),
            //////////////
            // recadrer //
            //////////////
            Tool(
                text = { "Placement" },
                icon = R.drawable.recadrer2,
                isColoredIcon = true,
                onClick = { viewModel, mainActivity ->
                    toolBarManager.toolbarComponent.toolsViewModel.rawFeed.setCurrentContent(CROP)
                }
            ),
            //////////////
            // renommer //
            //////////////
            Tool(
                text = { "Renommer" },
                icon = R.drawable.renommer,
                onClick = { viewModel, mainActivity ->
                    val currentItemFullPath = viewModel.selectedItem.value?.fullPath
                    val currentItemName = currentItemFullPath?.lastSegment ?: ""
                    //viewModel.setSelectedItem(null)
                    viewModel.setDialogMessage("Nouveau nom du dossier")
                    viewModel.setDialogInitialText(currentItemName)
                    viewModel.dialogOnOkLambda = { newName, viewModel, mainActivity ->
                        run {
                            if (currentItemFullPath == null || newName == currentItemName) {
                                Toast.makeText(
                                    mainActivity,
                                    "Le nouveau nom doît être différent de l'ancien",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@run
                            }

                            val newFullName = currentItemFullPath
                                .dropLastSegmentOfPath()
                                .combinedWith(newName)

                            println("NOM: $newFullName")
                            if (currentItemFullPath.toFile().exists()) {
                                if (currentItemFullPath.toFile().renameTo(newFullName.toFile())) {
                                    Toast.makeText(
                                        mainActivity,
                                        "Renommage effectué",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    viewModel.folderContentComponent.reloadCurrentFolder()
                                } else
                                    Toast.makeText(
                                        mainActivity,
                                        "Un problème lors du renommage est survenu",
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                            }
                        }

                        toolBarManager.toolbarComponent.toolsViewModel.rawFeed.setCurrentContent(DEFAULT)
                        viewModel.setSelectedItem(null, true)
                    }

                    viewModel.setIsTextDialogVisible(true)
                }
            ),
            /////////////////////
            // + dossier frère //
            /////////////////////
            Tool(
                text = { "+ frère" },
                icon = R.drawable.dossier,
                onClick = { viewModel, mainActivity ->
                    val parent = viewModel.folderContentComponent
                        .currentFolderFlow.value
                    val items = parent?.items ?: emptyList()

                    if (parent == null)
                        return@Tool

                    //viewModel.setSelectedItem(null)
                    viewModel.setDialogMessage("Nouveau nom du dossier")
                    viewModel.dialogOnOkLambda = { newName, viewModel, mainActivity ->
                        run {
                            val parentPath = parent.fullPath
                            val children = items
                                .map { item -> item.fullPath }
                            if (children.any { child -> child.lastSegment == newName }
                            ) {
                                Toast.makeText(
                                    mainActivity,
                                    "Un élément du dossier actuel porte le même nom",
                                    Toast.LENGTH_LONG
                                ).show()

                                return@run
                            }

                            val newFullPath = "$parentPath/$newName"

                            if (File(newFullPath).mkdir()) {
                                Toast.makeText(
                                    mainActivity,
                                    "Dossier créé",
                                    Toast.LENGTH_SHORT
                                ).show()
                                viewModel.folderContentComponent.reloadCurrentFolder()
                            } else
                                Toast.makeText(
                                    mainActivity,
                                    "Un problème lors de la création  du dossier frère est survenu",
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                        }

                        toolBarManager.toolbarComponent.toolsViewModel.rawFeed.setCurrentContent(DEFAULT)
                        viewModel.setSelectedItem(null, true)
                    }

                    viewModel.setIsTextDialogVisible(true)
                }
            ),
            ////////////////////
            // + dossier fils //
            ////////////////////
            Tool(
                text = { "+ fils" },
                icon = R.drawable.dossier,
                onClick = { viewModel, mainActivity ->
                    viewModel.setDialogMessage("Nouveau nom du dossier")
                    viewModel.dialogOnOkLambda = { newName, viewModel, mainActivity ->
                        run {
                            val selectedItemPath = viewModel.selectedItem.value?.fullPath
                            if (selectedItemPath == null)
                                return@run

                            var children: List<SigmaPath> = emptyList()

                            children = viewModel.diskRepository
                                .getFolderItems(
                                    selectedItemPath,
                                    SortingCriterion.ByDateDesc
                                )
                                .map { item -> item.fullPath }

                            if (children.any { child -> child.lastSegment == newName }
                            ) {
                                Toast.makeText(
                                    mainActivity,
                                    "Un élément du dossier sélectionné porte le même nom",
                                    Toast.LENGTH_LONG
                                ).show()

                                return@run
                            }

                            val newFullPath = selectedItemPath.combinedWith(newName)

                            if (newFullPath.toFile().mkdir() &&
                                newFullPath.toFile().exists()
                            ) {
                                Toast.makeText(
                                    mainActivity,
                                    "Dossier créé",
                                    Toast.LENGTH_SHORT
                                ).show()
                                viewModel.folderContentComponent.reloadCurrentFolder()
                            } else
                                Toast.makeText(
                                    mainActivity,
                                    "Un problème lors de la création  du dossier enfant est survenu",
                                    Toast.LENGTH_LONG
                                ).show()
                        }

                        toolBarManager.toolbarComponent.toolsViewModel.rawFeed.setCurrentContent(DEFAULT)
                        viewModel.setSelectedItem(null, true)

                    }

                    viewModel.setIsTextDialogVisible(true)

                }
            ),
            ///////////////
            // supprimer //
            ///////////////
            Tool(
                text = { "Supprimer" },
                icon = R.drawable.corbeille,
                onClick = { viewModel, mainActivity ->
                    val currentFolderPath = viewModel.selectedItemFullPath.value
                    //viewModel.setSelectedItem(null)
                    viewModel.setDialogMessage(
                        "Voulez-vous vraiment supprimer ce ${
                            if (viewModel
                                    .selectedItem.value?.isFile() != false
                            ) "fichier" else "dossier"
                        } ?"
                    )
                    viewModel.dialogYesNoLambda = { yesNo, viewModel, mainActivity ->
                        run {
                            if (!yesNo)
                                return@run

                            val item = viewModel.selectedItem.value
                            val itemFullPath = viewModel.selectedItemFullPath.value
                            if (item == null)
                                return@run

                            if (item.isFolder())
                                item.fullPath.toFile().deleteRecursively()
                            else item.fullPath.toFile().delete()

                            viewModel.setSelectedItem(null, true)

                            if (itemFullPath?.toFile()?.exists() == true)
                                Toast.makeText(
                                    mainActivity,
                                    "Un problème lors de la suppression est survenu",
                                    Toast.LENGTH_LONG
                                ).show()
                            else Toast.makeText(
                                mainActivity,
                                "Suppression effectuée",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        viewModel.folderContentComponent.reloadCurrentFolder()
                        toolBarManager.toolbarComponent.toolsViewModel.rawFeed.setCurrentContent(DEFAULT)
                    }

                    viewModel.setIsYesNoDialogVisible(true)
                }

            ),
        ),
        "FILE"
    )
}