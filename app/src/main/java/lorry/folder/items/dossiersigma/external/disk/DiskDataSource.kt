package lorry.folder.items.dossiersigma.external.disk

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.SigmaApplication
import lorry.folder.items.dossiersigma.headless.domain.ItemDTO
import java.io.File
import javax.inject.Inject
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class DiskDataSource @Inject constructor() : IDiskDataSource {

    companion object {
        val TAG = "DskDS"

    }

    /**
     * Récupère le contenu d'un dossier
     * @param folderPath chemin du dossier
     * @return List<ItemDTO> liste des items du dossier
     * ou vide si dossier vide, introuvable ou erreur de sécurité
     */
    suspend override fun getFolderContent(folderPath: String): List<ItemDTO> {
        val folder = File(folderPath)
        var items: List<ItemDTO>

        try {
            withContext(Dispatchers.IO, block = {
                items = folder.listFiles()?.map { file ->
                    ItemDTO(
                        path = file.path.substringBeforeLast("/"),
                        name = file.name,
                        isFile = file.isFile,
                        lastModified = file.lastModified()
                    )
                } ?: emptyList()
            })
        } catch (ex: SecurityException) {
            Log.d(
                SigmaApplication.APPLICATION_NAME,
                "SecurityException error in DiskDataSource/getFolderContent: ${ex.message}"
            )
            items = emptyList()
        }

        return items
    }

    @OptIn(ExperimentalTime::class)
    suspend override fun getFolderLiteContent(folderPath: String): Triple<Instant, Instant, Instant> {
        val folder = File(folderPath)
        var items: List<ItemDTO>

        val max = try {
            withContext(Dispatchers.IO) {
                val max1 = folder.listFiles()?.maxOfOrNull { it.lastModified() }
                max1
            }
        } catch (ex: SecurityException) {
            Log.d(TAG, "")
            return Triple(Instant.fromEpochMilliseconds(0),
                Instant.fromEpochMilliseconds(0),
                Instant.fromEpochMilliseconds(0))

        }

        val maxLevel2FolderPictures = try {
            withContext(Dispatchers.IO) {
                val max = folder.listFiles()
                    ?.flatMap<File, File> { file ->
                        if (file.isDirectory)
                            file.listFiles()
                                ?.filter{ file2 -> file2.isFile == true && file2.path.endsWith(".folderPicture.html") }
                                ?.toList()?: emptyList()
                        else emptyList()
                    }?.mapNotNull<File, Long> { file:File -> file.lastModified() }
                    ?.maxOrNull()
                max
            }
        } catch (ex: SecurityException) {
            Log.d(TAG, "")
            return Triple(Instant.fromEpochMilliseconds(0),
                Instant.fromEpochMilliseconds(0),
                Instant.fromEpochMilliseconds(0))

        }

        val first = Instant.fromEpochMilliseconds(folder.lastModified())
        val last = Instant.fromEpochMilliseconds(max ?: 0)
        val lastLevel2FolderPicture = Instant.fromEpochMilliseconds(maxLevel2FolderPictures ?: 0)
        val result: Triple<Instant, Instant, Instant> = Triple(first, last, lastLevel2FolderPicture)

        return result
    }
}