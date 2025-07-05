package lorry.folder.items.dossiersigma.data.dataSaver

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FolderCompositeManager(
    private val targetPath: String,
) {
    fun save(element: IElementInComposite) {
        val targetHtmlPath = "$targetPath/.folderPicture.html"
        FileCompositeManager(targetHtmlPath).save(element)
    }

    suspend fun getComposite(): CompositeData? {
        val targetHtmlPath = "$targetPath/.folderPicture.html"
        return FileCompositeManager(targetHtmlPath).getComposite()
    }

    suspend fun <T> getElement(reader: IElementReader<T>): T? {
        return withContext(Dispatchers.IO) {
            reader.fileGet(targetPath)
        }
    }
}