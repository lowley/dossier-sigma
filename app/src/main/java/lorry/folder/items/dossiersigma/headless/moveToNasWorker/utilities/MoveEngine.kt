package lorry.folder.items.dossiersigma.headless.moveToNasWorker.utilities

import android.content.Context
import javax.inject.Inject
import lorry.folder.items.dossiersigma.external.nas.DSI_FTP
import lorry.folder.items.dossiersigma.headless.domain.SigmaFile
import lorry.folder.items.dossiersigma.headless.serviceVariants.moveToNAS.ManifestEntry
import lorry.folder.items.dossiersigma.headless.serviceVariants.moveToNAS.NasUtilities
import lorry.folder.items.dossiersigma.headless.serviceVariants.moveToNAS.sendMessageToThoApp
import lorry.folder.items.dossiersigma.headless.shortcuts.ShortcutUseCase
import lorry.folder.items.dossiersigma.ui.sigma.SigmaViewModel

class MoveEngine @Inject constructor(
    private val dsFTP: DSI_FTP,
    val nasUtilities: NasUtilities,
    val context: Context,
    val shortcutUseCase: ShortcutUseCase
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

            val picture64 = entry.picture64

            if (isCancelled())
                return@onEachIndexed

            dsFTP.copy(entry.fullPath, destDir){ percent ->
                callback?.onItemProgress(itemIndex, percent)
            }
            callback?.onItemDone(itemIndex)

            val verify = nasUtilities.verify(entry.fullPath, destDir)

            if (verify) {
                nasUtilities.delete(entry.fullPath)

                shortcutUseCase.manageHtmlFilesInDestinations(
                    copyPictures = true,
                    rootDir = "/storage/emulated/0/Movies/sexe",
                    onlyOneFileShortcutFile = SigmaFile(
                        fullPath = entry.fullPath,
                        picture = picture64,
                        modificationDate = 0L,
                        tag = null,
                        scale = null,
                        memo = null
                    )
                )

//                sendMessageToThoApp(
//                    context,
//                    entry.fullPath,
//                    manifestUri = uri,
//                    index = itemIndex,
//                    total = entries.size)
            }

            SigmaViewModel.requestRefresh()

            if (isCancelled())
                return@onEachIndexed
        }
    }
}