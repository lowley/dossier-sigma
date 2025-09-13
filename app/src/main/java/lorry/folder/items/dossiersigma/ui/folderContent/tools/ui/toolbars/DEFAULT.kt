package lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.toolbars

object DEFAULT : Tools() {
    override fun content() = bottomTools.bottomComponent.toolsViewModel.rawFeed.defaultContent
}