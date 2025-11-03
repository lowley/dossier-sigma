package lorry.folder.items.dossiersigma.basics.domain

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import lorry.folder.items.dossiersigma.external.capsule.utilities.CapsuleData
import lorry.folder.items.dossiersigma.external.capsule.utilities.FileCapsuleManager
import lorry.folder.items.dossiersigma.external.capsule.utilities.FolderCapsuleManager
import lorry.folder.items.dossiersigma.external.capsule.utilities.IElementInCapsule
import lorry.folder.items.dossiersigma.external.capsule.utilities.IElementReader
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

@Stable
abstract class Item(
    val parentPath: SigmaPath,
    val name: String,
    val picture: Any?,
    val id: String = UUID.randomUUID().toString(),
    val modificationDate: Long,
    val tag: ColoredTag? = null,
    val scale: ContentScale? = null,
    val memo: String? = null,
    val size: Long? = null
) {
    fun isFile(): Boolean {
        return this is SigmaFile
    }

    fun isFolder(): Boolean {
        return this is SigmaFolder
    }

    val fullPath: SigmaPath
        get() = parentPath.combinedWith(name)

    val fileCapsuleManager = FileCapsuleManager(fullPath.str, false)
    val folderCapsuleManager = FolderCapsuleManager(fullPath, false)
    
    fun copy(
        path: SigmaPath = this.parentPath,
        name: String = this.name,
        picture: Any? = this.picture,
        tag: ColoredTag? = this.tag,
        scale: ContentScale? = this.scale,
        memo: String? = this.memo,
        size: Long? = this.size
    ): Item {
        if (this is SigmaFolder) {
            return this.copy(
                parentPath = path,
                name = name,
                picture = picture,
                id = id,
                modificationDate = modificationDate,
                tag = tag,
                scale = scale,
                memo = memo
            )
        } 
        else {
            return (this as SigmaFile).copy(
                path = path,
                name = name,
                picture = picture,
                id = id,
                modificationDate = modificationDate,
                tag = tag,
                scale = scale,
                memo = memo,
                size = size
            )
        }
    }

    fun save(element: IElementInCapsule) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            if (this@Item is SigmaFolder) {
                folderCapsuleManager.save(element)
            } else {
                fileCapsuleManager.save(element, forFolder = false)
            }
        }
    }

    suspend fun getComposite(): CapsuleData? {
        return if (this is SigmaFolder) {
            folderCapsuleManager.getCapsule()
        } else {
            fileCapsuleManager.getCapsule()
        }
    }

    suspend fun <T> getElement(reader: IElementReader<T>): T? {
        return if (this is SigmaFolder) {
            folderCapsuleManager.getElement<T>(reader)
        } else {
            fileCapsuleManager.getElement<T>(reader)
        }
    }

    fun isMemoUnchanged(): Boolean = memo == null || memo!!.isEmpty()
    
    override fun toString(): String {
        return "Item(type=${if (isFile()) "File" else "Folder"}, name='$name', picture=${picture != null}, " +
                "hasUrl= ${picture is String}, path='$parentPath', id='$id', modificationDate=$modificationDate, " +
                "tag=$tag, scale=$scale, memo=$memo, fullPath='$fullPath')"
    }
}

fun Long.toFormattedDate(): String {
    val instant = Instant.ofEpochMilli(this)
    val formatter = DateTimeFormatter.ofPattern("HH:mm, dd-MM-yyyy")
    val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    val affichage = dateTime.format(formatter)
    return affichage
}

@Serializable
data class ColoredTag(
    val id: UUID? = UUID.randomUUID(),
    val color: Color,
    val title: String,
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ColoredTag

        if (id != other.id) return false
        if (color != other.color) return false
        if (title != other.title) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + color.hashCode()
        result = 31 * result + title.hashCode()
        return result
    }
}

object EmptyItem: Item(
    parentPath = SigmaPath(""),
    name = "",
    picture = null,
    id = UUID.randomUUID().toString(),
    modificationDate = 0L,
    tag = null,
    scale = null,
    memo = null,
    size = null
)

@Serializable
@JvmInline
value class SigmaPath(val value: String){

    fun withSlash(): String{
        return when (value.endsWith("/")){
            true -> value
            false -> value + "/"
        }
    }

    fun withoutSlash(): String{
        return when (value.endsWith("/")){
            true -> value.dropLast(1)
            false -> value
        }
    }

    fun combinedWith(name: String): SigmaPath = SigmaPath(this.withSlash() + name.withoutInitialSlash())

    fun endsWith(searched: String): Boolean
        = value.endsWith(searched)

    fun replaceLastsegmentBy(segment: String): String =
        this.withoutSlash().replaceAfterLast("/", segment)

    fun append(segment: String): String = this.withSlash() + segment.withoutInitialSlash()
    fun appendToPath(segment: String): SigmaPath = SigmaPath(this.withSlash() + segment.withoutInitialSlash())
    fun dropLastSegment(): String = this.withoutSlash().substringBeforeLast("/")
    fun dropLastSegmentOfPath(): SigmaPath = SigmaPath(this.withoutSlash().substringBeforeLast("/"))

    fun toFile(): File = File(this.str)

    fun equalsTo(other: Any?): Boolean {
        if (!(other is SigmaPath))
            return false
        if (other == null)
            return false

        return this.value == other.value
    }
}

inline val SigmaPath.str get() = value
inline val SigmaPath.lastSegment get() = this.withoutSlash().substringAfterLast("/")
fun String.toSigmaPath() = SigmaPath(this)
fun String.withoutInitialSlash() = this.removePrefix("/")
fun <T> List<SigmaPath>.mapSigmaPaths(fn: (SigmaPath) -> T): List<T>{
    return this.map{ path ->
        fn(path)
    }
}

fun <T> MutableState<SigmaPath>.mapSigmaPaths(fn: (SigmaPath) -> T): MutableState<T>{
    val path = this.value
    return mutableStateOf(fn(path))
}
