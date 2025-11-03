package lorry.folder.items.dossiersigma.external.capsule.utilities

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.basics.domain.SigmaPath
import lorry.folder.items.dossiersigma.basics.domain.str

class FolderCapsuleManager(
    private val targetPath: SigmaPath,
    private val useOld: Boolean = false
) {
    suspend fun save(element: IElementInCapsule) {
        val targetHtmlPath = targetPath.appendToPath(".folderPicture.html")
        FileCapsuleManager(targetHtmlPath.str, false).save(element, forFolder = true)
    }

    suspend fun getCapsule(): CapsuleData? {
        val targetHtmlPath = targetPath.appendToPath(".folderPicture.html")
        return FileCapsuleManager(targetHtmlPath.str, false).getCapsule()
    }

    suspend fun <T> getElement(reader: IElementReader<T>): T? {
        return withContext(Dispatchers.IO) {
            reader.fileGet(targetPath)
        }
    }
}