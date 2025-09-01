package lorry.folder.items.dossiersigma.headless.favoriteObservation.service

import android.os.FileObserver
import java.io.File

class SigmaFileObserver(
    val file: File,
    val doOnEvent: (event: Int, path: String?) -> Unit
) : RecursiveFileObserver(
    file,
    FileObserver.MOVED_FROM or
            FileObserver.MOVED_TO or
            FileObserver.CREATE or
            FileObserver.DELETE or
            FileObserver.MODIFY or
            FileObserver.MOVED_FROM,
    {
        val event = it.event
        val path = it.childPath
        doOnEvent(event, path)
    })