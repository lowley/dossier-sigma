package lorry.folder.items.dossiersigma.serviceComponents.utilities

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FolderCapsuleManager(
    private val targetPath: String,
    private val useOld: Boolean = false
) {
    suspend fun save(element: IElementInComposite) {
        val targetHtmlPath = "$targetPath/.folderPicture.html"
        FileCapsuleManager(targetHtmlPath).save(element)
    }

    suspend fun getComposite(): CapsuleData? {
        val targetHtmlPath = "$targetPath/.folderPicture.html"
        return FileCapsuleManager(targetHtmlPath).getComposite()
    }

    suspend fun <T> getElement(reader: IElementReader<T>): T? {
        return withContext(Dispatchers.IO) {
            reader.fileGet(targetPath)
        }
    }
}