package lorry.folder.items.dossiersigma.external.playing

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import lorry.folder.items.dossiersigma.headless.domain.SigmaPath
import lorry.folder.items.dossiersigma.ui.settings.SettingsManager
import java.io.File
import javax.inject.Inject

class PlayingDataSource @Inject constructor(
    val context: Context,
) : IPlayingDataSource {

    @Inject lateinit var settings: SettingsManager

    override suspend fun playFile(fullPath: SigmaPath, type: String) {

            val file = fullPath.toFile()

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, type)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }

            context.startActivity(intent)
    }
}
