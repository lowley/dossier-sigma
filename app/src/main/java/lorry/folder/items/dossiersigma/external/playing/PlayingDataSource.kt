package lorry.folder.items.dossiersigma.external.playing

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import arrow.core.Either
import arrow.core.left
import arrow.core.right
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
    val startPattern = "kiwi?video="
    val endPattern = """">Lien automatique"""
    val playerPattern = "?player="

    override suspend fun playFile(fullPath: SigmaPath, type: String, activity: Activity) {

        if (type == "text/html" && fullPath.endsWith(".html"))
            startHtmlIntent(fullPath, activity)
        else
            startOtherIntent(fullPath, type, activity)
    }

    private fun startHtmlIntent(
        fullPath: SigmaPath,
        activity: Activity
    ) {

        val infos: Either<HtmlParsingError, Map<LinkInfo, String>> = extractInfos(fullPath)

        infos.fold(
            ifLeft = { error ->
                println("Erreur de parsing du Html")
            },
            ifRight = { datas ->

                DeepLinkActivity.startVlcOnce(
                    videoUri = VideoUriType(datas[LinkInfo.Uri]!!),
                    readerPackage = VideoPlayerPackageType(datas[LinkInfo.ViewerAppPackage]!!),
                    title = VideoTitleType(datas[LinkInfo.Title]!!),
                    activity = activity
                )

            }
        )
    }

    private fun extractInfos(fullPath: SigmaPath): Either<HtmlParsingError, Map<LinkInfo, String>> {

        val file = fullPath.toFile()
        val content = file.readText(Charsets.UTF_8)

        if (content.isEmpty() || !content.contains(startPattern))
            HtmlParsingError.NoLinkInHtml.left()

        val interestingContent = content
            .substringAfter(startPattern)
            .substringBefore(endPattern)

        if (interestingContent.isEmpty())
            HtmlParsingError.MalformedLink.left()

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

        if (uriString.isEmpty() || title.isEmpty())
            HtmlParsingError.IncorrectValue.left()

        return hashMapOf(
            LinkInfo.Uri to uriString,
            LinkInfo.ViewerAppPackage to readerPackage,
            LinkInfo.Title to title
        ).right()
    }

    private fun startOtherIntent(
        fullPath: SigmaPath,
        type: String,
        activity: Activity
    ) {

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

    sealed interface HtmlParsingError {
        object NoLinkInHtml
        object MalformedLink

        object IncorrectValue
    }

    enum class LinkInfo {
        Uri,
        ViewerAppPackage,
        Title
    }
}
