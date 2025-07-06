package lorry.folder.items.dossiersigma.data.dataSaver

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class CompositeManager @Inject constructor(
    private val targetPath: String,
) {
    suspend fun save(element: IElementInComposite) {
        val file = File(targetPath)
        if (!file.exists())
            return

        if (file.isFile())
            FileCompositeManager(targetPath).save(element)
        else {
            FolderCompositeManager(targetPath).save(element)
        }
    }

    suspend fun getComposite(): CompositeData? {
        val file = File(targetPath)
        if (!file.exists())
            return null

        return if (file.isFile) {
            FileCompositeManager(targetPath).getComposite()
        } else {
            FolderCompositeManager(targetPath).getComposite()
        }
    }

    /**
     * lecture à chaque fois de l'info dans le fichier/dossier
     */
    suspend fun <T> getElement(reader: IElementReader<T>): T? {
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