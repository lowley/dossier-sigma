package lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.toolbars

object DEFAULT : Tools() {
    override fun content() = toolBarManager.bottomComponent.toolsViewModel.rawFeed.defaultContent
}