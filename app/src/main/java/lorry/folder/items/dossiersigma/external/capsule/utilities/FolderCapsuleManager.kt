package lorry.folder.items.dossiersigma.external.capsule.utilities

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FolderCapsuleManager(
    private val targetPath: String,
    private val useOld: Boolean = false
) {
    suspend fun save(element: IElementInCapsule) {
        val targetHtmlPath = "$targetPath/.folderPicture.html"
        FileCapsuleManager(targetHtmlPath).save(element)
    }

    suspend fun getCapsule(): CapsuleData? {
        val targetHtmlPath = "$targetPath/.folderPicture.html"
        return FileCapsuleManager(targetHtmlPath).getCapsule()
    }

    suspend fun <T> getElement(reader: IElementReader<T>): T? {
        return withContext(Dispatchers.IO) {
            reader.fileGet(targetPath)
        }
    }
}