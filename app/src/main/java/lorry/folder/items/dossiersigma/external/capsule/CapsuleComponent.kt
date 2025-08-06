package lorry.folder.items.dossiersigma.external.capsule

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.external.capsule.utilities.CapsuleData
import lorry.folder.items.dossiersigma.external.capsule.utilities.FileCapsuleManager
import lorry.folder.items.dossiersigma.external.capsule.utilities.FolderCapsuleManager
import lorry.folder.items.dossiersigma.external.capsule.utilities.IElementInCapsule
import lorry.folder.items.dossiersigma.external.capsule.utilities.IElementReader
import java.io.File
import javax.inject.Inject

class CapsuleComponent @Inject constructor(): ICapsuleComponent {

    override suspend fun save(
        element: IElementInCapsule,
        targetPath: String,
        useOld: Boolean
    ) {
        val file = File(targetPath)
        if (!file.exists())
            return

        if (file.isFile())
            FileCapsuleManager(targetPath, useOld).save(element)
        else {
            FolderCapsuleManager(targetPath, useOld).save(element)
        }
    }

    override suspend fun getCapsule(
        targetPath: String,
        useOld: Boolean
    ): CapsuleData? {
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
    override suspend fun <T> getElement(
        reader: IElementReader<T>,
        targetPath: String
        ): T? {
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