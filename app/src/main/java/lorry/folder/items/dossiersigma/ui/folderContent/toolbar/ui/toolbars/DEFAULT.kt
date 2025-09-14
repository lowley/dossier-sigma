package lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.toolbars

object DEFAULT : Tools() {
    override fun content() = toolBarManager.toolbarComponent.toolsViewModel.rawFeed.defaultContent
}