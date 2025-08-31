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

    @OptIn(ExperimentalTime::class)
    override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + containerMtime.hashCode()
        result = 31 * result + contentsMaxMtime.hashCode()
        return result
    }


}