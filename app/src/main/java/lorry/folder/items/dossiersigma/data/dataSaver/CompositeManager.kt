package lorry.folder.items.dossiersigma.data.dataSaver

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.serviceComponents.ICapsuleComponent
import java.io.File
import javax.inject.Inject

class CompositeManager @Inject constructor(
    private val targetPath: String,
    private val useOld: Boolean = false
): ICapsuleComponent {
    override suspend fun save(element: IElementInComposite) {
        val file = File(targetPath)
        if (!file.exists())
            return

        if (file.isFile())
            FileCompositeManager(targetPath, useOld).save(element)
        else {
            FolderCompositeManager(targetPath, useOld).save(element)
        }
    }

    override suspend fun getComposite(): CompositeData? {
        val file = File(targetPath)
        if (!file.exists())
            return null

        return if (file.isFile) {
            FileCompositeManager(targetPath, useOld).getComposite()
        } else {
            FolderCompositeManager(targetPath, useOld).getComposite()
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