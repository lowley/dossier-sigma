package lorry.folder.items.dossiersigma.serviceComponents

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.serviceComponents.utilities.CapsuleData
import lorry.folder.items.dossiersigma.serviceComponents.utilities.FileCapsuleManager
import lorry.folder.items.dossiersigma.serviceComponents.utilities.FolderCapsuleManager
import lorry.folder.items.dossiersigma.serviceComponents.utilities.IElementInCapsule
import lorry.folder.items.dossiersigma.serviceComponents.utilities.IElementReader
import java.io.File
import javax.inject.Inject

class CapsuleComponent @Inject constructor(
    private val targetPath: String,
    private val useOld: Boolean = false
): ICapsuleComponent {
    override suspend fun save(element: IElementInCapsule) {
        val file = File(targetPath)
        if (!file.exists())
            return

        if (file.isFile())
            FileCapsuleManager(targetPath, useOld).save(element)
        else {
            FolderCapsuleManager(targetPath, useOld).save(element)
        }
    }

    override suspend fun getComposite(): CapsuleData? {
        val file = File(targetPath)
        if (!file.exists())
            return null

        return if (file.isFile) {
            FileCapsuleManager(targetPath, useOld).getCapsule()
        } else {
            FolderCapsuleManager(targetPath, useOld).getCapsule()
        }
    }

    /**
     * lecture à chaque fois de l'info dans le fichier/dossier
     */
    override suspend fun <T> getElement(reader: IElementReader<T>): T? {
        return withContext(Dispatchers.IO) {
            val file = File(targetPath)
            if (!file.exists())
                return@withContext null

            return@withContext if (file.isFile)
                reader.fileGet(targetPath)
            else
                reader.folderGet(targetPath)
        }
    }
}