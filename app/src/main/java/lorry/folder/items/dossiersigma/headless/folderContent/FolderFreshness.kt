package lorry.folder.items.dossiersigma.headless.folderContent

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class FolderFreshness @OptIn(ExperimentalTime::class) constructor(

    val path: String,
    private val containerMtime: Instant,      // mtime du dossier lui-même
    private val contentsMaxMtime: Instant,
) {
    companion object {
        @OptIn(ExperimentalTime::class)
        val DUMMY = FolderFreshness(
            path = "/storage/emulated/0/Movies",
            containerMtime = Instant.DISTANT_PAST,
            contentsMaxMtime = Instant.DISTANT_PAST,
        )
    }

    @OptIn(ExperimentalTime::class)
    fun isSameAs(other: FolderFreshness): Boolean =
        path == other.path &&
        containerMtime.toString() == other.containerMtime.toString() &&
        contentsMaxMtime.toString() == other.contentsMaxMtime.toString()
}