package lorry.folder.items.dossiersigma.external.playing

import android.app.Activity
import lorry.folder.items.dossiersigma.basics.domain.SigmaPath

interface IPlayingDataSource {

    suspend fun playFile(fullPath: SigmaPath, type: String, activity: Activity)


}