package lorry.folder.items.dossiersigma.headless.shortcuts

import java.util.Calendar

data class ThoFile(
    val name: String,
    val timestamp: Calendar,
    val size: Long,
    val fullPath: String,
    val isVideoFile: Boolean,
    val isHtmlFile: Boolean,
    val pictureBase64: String?
) {
    companion object {
        val EMPTY = ThoFile("", Calendar.getInstance(), 0, "", false, false, null)

    }
}