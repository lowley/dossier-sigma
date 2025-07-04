package lorry.folder.items.dossiersigma.data.dataSaver

class FolderCompositeManager (
    private val targetPath: String,
    ) {
        fun save(element: IElementInComposite) {
            val targetHtmlPath = "$targetPath/.folderPicture.html"
            FileCompositeManager(targetHtmlPath).save(element)
        }
    
        suspend fun get(): CompositeData?{
            val targetHtmlPath = "$targetPath/.folderPicture.html"
            return FileCompositeManager(targetHtmlPath).get()
        }
}