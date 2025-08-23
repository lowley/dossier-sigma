package lorry.folder.items.dossiersigma.headless.moveToNasWorker.utilities

import jakarta.inject.Inject
import lorry.folder.items.dossiersigma.external.nas.DSI_FTP

class MoveEngine @Inject constructor(
    private val dsFTP: DSI_FTP
){
    suspend fun copyAll(
        sources: List<String>,
        destDir: String,
        callback: IMoveProgress?,
        isCancelled: () -> Boolean
    ){
        callback?.onStart(sources.size)
        sources.onEachIndexed { itemIndex,  source ->

            if (isCancelled())
                return@onEachIndexed

            dsFTP.copy(source, destDir){ percent ->
                callback?.onItemProgress(itemIndex, percent)
            }
            callback?.onItemDone(itemIndex)
            if (isCancelled())
                return@onEachIndexed
        }
    }
}