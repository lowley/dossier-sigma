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
    suspend override fun getFolderLiteContent(folderPath: String): Pair<Instant, Instant> {
        val folder = File(folderPath)
        var items: List<ItemDTO>

        val max = try {
            withContext(Dispatchers.IO) {
                val max1 = folder.listFiles()?.maxOfOrNull { it.lastModified() }
                max1
            }
        }
        catch(ex: SecurityException) {
            Log.d(TAG, "")
            return Pair(Instant.fromEpochMilliseconds(0), Instant.fromEpochMilliseconds(0))

        }

        val last = Instant.fromEpochMilliseconds(max ?: 0)
        val first = Instant.fromEpochMilliseconds(folder.lastModified())
        val pair: Pair<Instant, Instant> = Pair(first, last)

    return pair
}
}