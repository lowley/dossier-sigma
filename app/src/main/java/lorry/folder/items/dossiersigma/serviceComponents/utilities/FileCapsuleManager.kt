package lorry.folder.items.dossiersigma.serviceComponents.utilities

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject


class FileCapsuleManager @Inject constructor(
    private val targetPath: String,
    private val useOld: Boolean = false
) {
    suspend fun save(element: IElementInComposite) {
        
        val compositeIO = if (useOld)
            FileCapsuleIO()
        else FileMetadataManager()

        val target = File(targetPath)
        val existingComposite = if (target.exists()) {
            compositeIO.getComposite(targetPath) ?: CapsuleData()
        } else {
            CapsuleData()
        }

        val updatedComposite = element.update(existingComposite)
        compositeIO.replaceComposite(targetPath, updatedComposite)
    }

    suspend fun getComposite(): CapsuleData {
        val compositeIO = FileMetadataManager()

        val target = File(targetPath)
        return withContext(Dispatchers.IO) {
            if (target.exists()) {
                compositeIO.getComposite(targetPath) ?: CapsuleData()
            } else {
                CapsuleData()
            }
        }
    }

    suspend fun <T> getElement(reader: IElementReader<T>): T? {
        return withContext(Dispatchers.IO) {
            reader.fileGet(targetPath)
        }
    }
}