package lorry.folder.items.dossiersigma.external.capsule.utilities

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject


class FileCapsuleManager @Inject constructor(
    private val targetPath: String,
    private val useOld: Boolean = false
) {
    suspend fun save(element: IElementInCapsule) {

        val capsuleIO = if (useOld)
            FileCapsuleIO()
        else FileMetadataManager()

        val target = File(targetPath)
        val existingCapsule = if (target.exists()) {
            capsuleIO.getCapsule(targetPath) ?: CapsuleData()
        } else {
            CapsuleData()
        }

        val updatedCapsule = element.update(existingCapsule)
        capsuleIO.replaceCapsule(targetPath, updatedCapsule)
    }

    suspend fun getCapsule(): CapsuleData {
        val compositeIO = FileMetadataManager()

        val target = File(targetPath)
        return withContext(Dispatchers.IO) {
            if (target.exists()) {
                compositeIO.getCapsule(targetPath) ?: CapsuleData()
            } else
                CapsuleData()
        }
    }

    suspend fun <T> getElement(reader: IElementReader<T>): T? {
        return withContext(Dispatchers.IO) {
            reader.fileGet(targetPath)
        }
    }
}