package lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.toolbars

import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.external.capsule.CapsuleComponent
import lorry.folder.items.dossiersigma.external.capsule.utilities.Flag
import lorry.folder.items.dossiersigma.headless.domain.ColoredTag
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.BottomToolContent
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.Tool
import java.util.UUID
import kotlin.collections.get

object TAGS_MENU : Tools(

) {
    override fun content() = BottomToolContent(
        listOf(
            /////////////
            // ajouter //
            /////////////
            Tool(
                text = { "Ajouter" },
                icon = R.drawable.plus,
                visible = { viewModel, mainActivity ->
                    val currentFolder = viewModel.folderContentComponent.currentFolderFlow.value
                    val selectedFolder = viewModel.folderContentComponent
                        .folderCacheFlow.value[viewModel.selectedItemFullPath.value]?.folder
                    val selectedFolderTag = selectedFolder?.tag

                    selectedFolder != null && selectedFolderTag == null
                },
                onClick = { viewModel, mainActivity ->
                    run {
                        val currentItem = viewModel.selectedItem.value
                        if (currentItem == null)
                            return@run

                        //viewModel.setSelectedItem(null)
                        viewModel.setDialogMessage("Entrez les informations du drapeau")
                        viewModel.dialogTagLambda = { tagInfos, viewModel, mainActivity ->
                            run {
                                if (tagInfos == null)
                                    return@run

                                val capsuleMgr = CapsuleComponent()

                                val newFlag = ColoredTag(
                                    title = tagInfos.title,
                                    color = tagInfos.color,
                                    id = UUID.randomUUID(),
                                )
                                capsuleMgr.save(
                                    Flag(newFlag),
                                    currentItem.fullPath
                                )

                                viewModel.folderContentComponent.reloadCurrentFolder()
                            }

                            bottomTools.component.setCurrentContent(DEFAULT)
                            viewModel.setSelectedItem(null, true)
                        }

                        viewModel.setIsTagInfosDialogVisible(true)
                    }
                }
            ),
            //////////////
            // modifier //
            //////////////
            Tool(
                text = { "Modifier" },
                icon = R.drawable.modifier,
                visible = { viewModel, mainActivity ->
                    val currentFolder = viewModel.folderContentComponent.currentFolderFlow.value
                    val selectedFolder = viewModel.folderContentComponent
                        .folderCacheFlow.value[viewModel.selectedItemFullPath.value]?.folder
                    val selectedFolderTag = selectedFolder?.tag

                    selectedFolderTag != null
                },
                onClick = { viewModel, mainActivity ->
//                        viewModel.setDialogMessage("Nom du dossier à créer")
//                        viewModel.dialogOnOkLambda = { newName, viewModel, mainActivity ->
//                            val currentFolderPath = viewModel.currentFolderPath.value
//                            val newFullName = "$currentFolderPath/$newName"
//                            if (!File(newFullName).exists()) {
//                                if (File(newFullName).mkdir()) {
//                                    Toast.makeText(mainActivity, "Répertoire créé", Toast.LENGTH_SHORT).show()
//                                    viewModel.refreshCurrentFolder()
//                                } else
//                                    Toast.makeText(
//                                        mainActivity,
//                                        "Un problème est survenu",
//                                        Toast.LENGTH_SHORT
//                                    )
//                                        .show()
//                            }
//                        }
//
//                        mainActivity.openTextDialog.value = true
                }
            ),
            ///////////////
            // supprimer //
            ///////////////
            Tool(
                text = { "item" },
                icon = R.drawable.moins,
                visible = { viewModel, mainActivity ->
                    val currentFolder = viewModel.folderContentComponent.currentFolderFlow.value
                    val selectedFolder = viewModel.folderContentComponent
                        .folderCacheFlow.value[viewModel.selectedItemFullPath.value]?.folder
                    val selectedFolderTag = selectedFolder?.tag

                    selectedFolderTag != null
                },
                onClick = { viewModel, mainActivity ->
                    run {
                        val currentItem = viewModel.selectedItem.value
                        if (currentItem == null)
                            return@run

                        val selectedFolder = viewModel.folderContentComponent
                            .folderCacheFlow.value[viewModel.selectedItemFullPath.value]?.folder
                        val selectedFolderTag = selectedFolder?.tag
                        val tool = DEFAULT.content()
                            .tools.value.firstOrNull { it.id == selectedFolderTag?.id }

                        if (tool == null) {
                            println("problème, tool inexistant")
                            return@run
                        }

                        //inutile car refresh plus loin
//                            if (viewModel.removeFlagCacheForKey(currentItem.fullPath) == null) {
//                                println("problème, suppression de tag impossible")
//                                return@run
//                            }

                        val capsuleMgr = CapsuleComponent()
                        capsuleMgr.save(
                            Flag(null),
                            currentItem.fullPath
                        )

                        val none = viewModel.folderContentComponent
                            .folderCacheFlow
                            ?.value
                            ?.none { it.value?.folder?.tag?.id == tool.id } == true

                        if (none)
                            DEFAULT.content().removeTool(tool)

                        viewModel.setSelectedItem(null, true)
                        viewModel.folderContentComponent.reloadCurrentFolder()
                        bottomTools.component.setCurrentContent(DEFAULT)

//                            viewModel.clearFlagCache()
//                            DEFAULT.content().updateTools(emptyList<Tool>())
                    }
                }
            ),
            ///////////////
            // supprimer //
            ///////////////
            Tool(
                text = { "étiquette" },
                icon = R.drawable.moins,
                visible = { viewModel, mainActivity ->
                    val selectedFolder = viewModel.folderContentComponent
                        .folderCacheFlow.value[viewModel.selectedItemFullPath.value]?.folder
                    val selectedFolderTag = selectedFolder?.tag

                    selectedFolderTag != null
                },
                onClick = { viewModel, mainActivity ->
                    run {
                        val selectedFolder = viewModel.folderContentComponent
                            .folderCacheFlow.value[viewModel.selectedItemFullPath.value]?.folder
                        val selectedFolderTag = selectedFolder?.tag

                        val tool = DEFAULT.content()
                            .tools.value.firstOrNull { it.id == selectedFolderTag?.id }

                        if (tool == null) {
                            println("problème, tool inexistant")
                            return@run
                        }


                        val itemsWithThisTag = viewModel.folderContentComponent
                            ?.currentFolderFlow
                            ?.value
                            ?.items
                            ?.filter { item ->
                                item?.tag?.id == tool.id
                            }

                        //on fait ça parce que par lazy loading au début de l'affichage
                        //du dossier de tous les items
//                            val itemsWithThisTag =
//                                viewModel.displayedItemsFlow.value.second.filter {
//                                    val capsuleMgr = CapsuleComponent()
//                                    val tagFile = capsuleMgr.getElement(
//                                        Flag.Companion,
//                                        it.fullPath
//                                    )
//
//                                    val tagCache = viewModel.flagCache.value[it.fullPath]
//
//                                    val tagFinal = tagCache ?: tagFile
//                                    tagFinal?.id == tool.id
//                                }

                        itemsWithThisTag?.forEach {
                            val capsuleMgr = CapsuleComponent()
                            capsuleMgr.save(Flag(null), it.fullPath)
                        }

                        //normalement toujours vrai
//                            if (!viewModel.flagCache.containsFlagAsValue(tool.id))
//                                DEFAULT.content(viewModel).removeTool(tool)

                        viewModel.setSelectedItem(null, true)
//                            viewModel.refreshCurrentFolder()
                        bottomTools.component.setCurrentContent(DEFAULT)

                        viewModel.folderContentComponent.reloadCurrentFolder()
                    }
                }
            ),
            ////////////////////
            // supprimer tous //
            ////////////////////
            Tool(
                text =
                    { "carnage" },
                icon = R.drawable.moins,
                visible =
                    { viewModel, mainActivity ->
                        val selectedFolder = viewModel.folderContentComponent
                            .folderCacheFlow.value[viewModel.selectedItemFullPath.value]?.folder
                        val selectedFolderTag = selectedFolder?.tag

                        selectedFolderTag != null
                    },
                onClick =
                    { viewModel, mainActivity ->
                        run {
                            val selectedFolder = viewModel.folderContentComponent
                                .folderCacheFlow.value[viewModel.selectedItemFullPath.value]?.folder
                            val selectedFolderTag = selectedFolder?.tag

                            val tool = DEFAULT.content()
                                .tools.value.firstOrNull { it.id == selectedFolderTag?.id }

                            if (tool == null) {
                                println("problème, tool inexistant")
                                return@run
                            }

                            val itemsWithThisTag = viewModel.folderContentComponent
                                ?.currentFolderFlow
                                ?.value
                                ?.items

                            //on fait ça parce que par lazy loading au début de l'affichage
                            //du dossier de tous les items
//                            val itemsWithThisTag =
//                                viewModel.displayedItemsFlow.value.second.filter {
//                                    val capsuleMgr = CapsuleComponent()
//                                    val tagFile = capsuleMgr.getElement(
//                                        Flag.Companion,
//                                        it.fullPath
//                                    )
//
//                                    val tagCache = viewModel.flagCache.value[it.fullPath]
//
//                                    val tagFinal = tagCache ?: tagFile
//                                    tagFinal?.id == tool.id
//                                }

                            itemsWithThisTag?.forEach {
                                val capsuleMgr = CapsuleComponent()
                                capsuleMgr.save(Flag(null), it.fullPath)
                            }

                            //normalement toujours vrai
//                            if (!viewModel.flagCache.containsFlagAsValue(tool.id))
//                                DEFAULT.content(viewModel).removeTool(tool)

                            viewModel.setSelectedItem(null, true)
//                            viewModel.refreshCurrentFolder()
                            bottomTools.component.setCurrentContent(DEFAULT)

                            viewModel.folderContentComponent.reloadCurrentFolder()
                        }
                    }
            )
        ),
        "TAGS_MENU"
    )
}