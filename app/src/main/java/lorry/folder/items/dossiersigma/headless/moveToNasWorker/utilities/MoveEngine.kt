package lorry.folder.items.dossiersigma.headless.moveToNasWorker.utilities

import android.content.Context
import jakarta.inject.Inject
import lorry.folder.items.dossiersigma.external.nas.DSI_FTP
import lorry.folder.items.dossiersigma.headless.serviceVariants.moveToNAS.ManifestEntry
import lorry.folder.items.dossiersigma.headless.serviceVariants.moveToNAS.NasUtilities
import lorry.folder.items.dossiersigma.headless.serviceVariants.moveToNAS.sendMessageToThoApp
import lorry.folder.items.dossiersigma.UI.sigma.SigmaViewModel

class MoveEngine @Inject constructor(
    private val dsFTP: DSI_FTP,
    val nasUtilities: NasUtilities,
    val context: Context,
){
    suspend fun copyAll(
        entries: List<ManifestEntry>,
        destDir: String,
        callback: IMoveProgress?,
        isCancelled: () -> Boolean,
        path: String,
        uri: String,
    ){
        callback?.onStart(entries.size)
        entries.onEachIndexed { itemIndex,  entry ->

            if (isCancelled())
                return@onEachIndexed

            dsFTP.copy(entry.fullPath, destDir){ percent ->
                callback?.onItemProgress(itemIndex, percent)
            }
            callback?.onItemDone(itemIndex)

            val verify = nasUtilities.verify(entry.fullPath, destDir)

            if (verify) {
                nasUtilities.delete(entry.fullPath)

                sendMessageToThoApp(
                    context,
                    entry.fullPath,
                    manifestUri = uri)
            }

            SigmaViewModel.requestRefresh()

            if (isCancelled())
                return@onEachIndexed
        }
    }
}