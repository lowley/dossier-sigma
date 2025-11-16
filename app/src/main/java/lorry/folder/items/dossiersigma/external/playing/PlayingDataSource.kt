package lorry.folder.items.dossiersigma.external.playing

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import lorry.folder.items.dossiersigma.basics.domain.SigmaPath
import lorry.folder.items.dossiersigma.headless.htmlLinks.DeepLinkActivity
import lorry.folder.items.dossiersigma.headless.htmlLinks.VideoPlayerPackageType
import lorry.folder.items.dossiersigma.headless.htmlLinks.VideoTitleType
import lorry.folder.items.dossiersigma.headless.htmlLinks.VideoUriType
import lorry.folder.items.dossiersigma.ui.settings.SettingsManager
import javax.inject.Inject

class PlayingDataSource @Inject constructor(
    val context: Context,
) : IPlayingDataSource {

    @Inject
    lateinit var settings: SettingsManager

    override suspend fun playFile(fullPath: SigmaPath, type: String, activity: Activity) {

        if (fullPath.endsWith(".html")) {

            val file = fullPath.toFile()
            val content = file.readText(Charsets.UTF_8)
            val startPattern = "kiwi?video="
            val endPattern = """">Lien automatique"""
            val playerPattern = "?player="

            if (content.isEmpty() || !content.contains(startPattern))
                return

            val interestingContent = content
                .substringAfter(startPattern)
                .substringBefore(endPattern)

            if (interestingContent.isEmpty())
                return

            val playerText = interestingContent
                .substringAfter(playerPattern)

            val readerPackage = when (playerText) {
                "vlc" -> "org.videolan.vlc"
                "bsplayer" -> "com.bsplayer.bspandroid.free"
                else -> "org.videolan.vlc"
            }

            val uriString = interestingContent
                .substringBefore(playerPattern)

            val title = uriString.substringAfter("20/videos/")

            DeepLinkActivity.startVlcOnce(
                videoUri = VideoUriType(uriString),
                readerPackage = VideoPlayerPackageType(readerPackage),
                title = VideoTitleType(title),
                activity = activity
            )

            return
        }

        /////////////////////////////

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
