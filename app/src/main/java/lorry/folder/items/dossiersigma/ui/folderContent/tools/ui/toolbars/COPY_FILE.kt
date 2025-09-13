package lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.toolbars

import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.BottomToolContent
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.Tool

object COPY_FILE : Tools() {
    override fun content() = BottomToolContent(
        listOf(
            /////////////
            // annuler //
            /////////////
            Tool(
                text = { "Annuler" },
                icon = R.drawable.annuler,
                onClick = { viewModel, mainActivity ->
                    bottomTools.component.setCurrentContent(DEFAULT)
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
                    bottomTools.component.setCurrentContent(DEFAULT)
                }
            )
        ),
        "COPY_FILE"
    )
}