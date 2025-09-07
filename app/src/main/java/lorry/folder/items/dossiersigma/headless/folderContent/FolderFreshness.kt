package lorry.folder.items.dossiersigma.headless.folderContent

import lorry.folder.items.dossiersigma.headless.domain.Item
import lorry.folder.items.dossiersigma.headless.domain.SigmaFolder
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class FolderFreshness @OptIn(ExperimentalTime::class) constructor(

    val path: String,
    private val containerMtime: Instant,      // mtime du dossier lui-même
    private val contentsMaxMtime: Instant,
    private val secondLevelFolderPictureMTime: Instant
) {
    companion object {
        @OptIn(ExperimentalTime::class)
        val DUMMY = FolderFreshness(
            path = "/storage/emulated/0/Movies",
            containerMtime = Instant.DISTANT_PAST,
            contentsMaxMtime = Instant.DISTANT_PAST,
            secondLevelFolderPictureMTime = Instant.DISTANT_PAST,
        )
    }

    @OptIn(ExperimentalTime::class)
    fun isSameAs(other: FolderFreshness?): Boolean =
        other != null &&
        path == other.path &&
        containerMtime.toString() == other.containerMtime.toString() &&
        contentsMaxMtime.toString() == other.contentsMaxMtime.toString()

    @OptIn(ExperimentalTime::class)
    override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + containerMtime.hashCode()
        result = 37 * result + contentsMaxMtime.hashCode()
        return result
    }

    override fun toString(): String {
        return "FolderFreshness(path='$path', hashCode=${hashCode()})"
    }
}

@OptIn(ExperimentalTime::class)
fun SigmaFolder.computeFreshness(): FolderFreshness {
    val containerMTime = Instant.fromEpochMilliseconds(this.modificationDate)
    val contentMTime = this.items.maxBy { item ->
        item.modificationDate
    }.let { Instant.fromEpochMilliseconds(it.modificationDate) }
    val secondLevelFolderPictureMTime = this.items.flatMap { item1 ->
        if (item1.isFolder())
            (item1 as SigmaFolder).items.filter { item2 ->
                item2.path.endsWith(".folderPicture.html")
            }
        else emptyList()
    }.maxByOrNull { item: Item -> item.modificationDate }
        ?.let { Instant.fromEpochMilliseconds(it.modificationDate) }
        ?: Instant.DISTANT_PAST

    val path = this.path

    return FolderFreshness(
        path = path,
        containerMtime = containerMTime,
        contentsMaxMtime = contentMTime,
        secondLevelFolderPictureMTime = secondLevelFolderPictureMTime
    )
}