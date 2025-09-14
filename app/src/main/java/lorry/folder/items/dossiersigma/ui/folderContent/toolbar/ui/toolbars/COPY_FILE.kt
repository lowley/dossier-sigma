package lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.toolbars

import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.ToolbarContent
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.Tool

object COPY_FILE : Tools() {
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
                }
            ),
            ////////////
            // coller //
            ////////////
            Tool(
                text = { "Coller" },
                icon = R.drawable.coller,
                onClick = { viewModel, mainActivity ->
                    //vm.diskRepository.copyFile(sourceFile, destinationFile)
                    toolBarManager.toolbarComponent.toolsViewModel.rawFeed.setCurrentContent(DEFAULT)
                }
            )
        ),
        "COPY_FILE"
    )
}