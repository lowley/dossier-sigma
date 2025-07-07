package lorry.folder.items.dossiersigma.data.dataSaver

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject


class FileCompositeManager @Inject constructor(
    private val targetPath: String,
    private val useOld: Boolean = false
) {
    suspend fun save(element: IElementInComposite) {
        
        val compositeIO = if (useOld)
            FileCompositeIO()
        else FileMetadataManager()

        val target = File(targetPath)
        val existingComposite = if (target.exists()) {
            compositeIO.getComposite(targetPath) ?: CompositeData()
        } else {
            CompositeData()
        }

        val updatedComposite = element.update(existingComposite)
        compositeIO.replaceComposite(targetPath, updatedComposite)
    }

    suspend fun getComposite(): CompositeData {
        val compositeIO = FileMetadataManager()

        val target = File(targetPath)
        return withContext(Dispatchers.IO) {
            if (target.exists()) {
                compositeIO.getComposite(targetPath) ?: CompositeData()
            } else {
                CompositeData()
            }
        }
    }

    suspend fun <T> getElement(reader: IElementReader<T>): T? {
        return withContext(Dispatchers.IO) {
            reader.fileGet(targetPath)
        }
    }
}