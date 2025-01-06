package lorry.folder.items.dossiersigma.data.disk

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.SigmaApplication
import lorry.folder.items.dossiersigma.data.interfaces.IDiskDataSource
import lorry.folder.items.dossiersigma.data.interfaces.ItemDTO
import java.io.File
import java.util.logging.Logger
import javax.inject.Inject

class DiskDataSource @Inject constructor() : IDiskDataSource {

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
                        name = file.name,
                        isFile = file.isFile
                    )
                } ?: emptyList()
            })
        }
        catch(ex: SecurityException){
            Log.d(SigmaApplication.APPLICATION_NAME, "SecurityException error in DiskDataSource/getFolderContent: ${ex.message}")
            items = emptyList()
        }

        return items
    }
}