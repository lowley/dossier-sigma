package lorry.folder.items.dossiersigma.headless.favoriteObservation.utilities

import android.os.FileObserver
import java.io.File

class SigmaFileObserver(
    val file: File,
    val doOnEvent: (event: Int, path: String?) -> Unit
) : FileObserver(
    file,
    MOVED_FROM or
            MOVED_TO or
            CREATE or
            DELETE or
            MODIFY or
            MOVED_FROM
) {

    override fun onEvent(event: Int, path: String?) {

        doOnEvent(event, path)
    }

    override fun startWatching() {

        super.startWatching()
    }

    override fun stopWatching() {

        super.stopWatching()
    }
}