package lorry.folder.items.dossiersigma.headless.favoriteObservation.service

import android.os.FileObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.headless.domain.SigmaPath
import java.io.File

class SigmaFileObserver(
    val file: File,
    val doOnEvent: suspend (event: Int, path: SigmaPath?) -> Unit
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
        val path = SigmaPath(it.absolute.absolutePath)
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        scope.launch {
            doOnEvent(event, path)
        }
    })