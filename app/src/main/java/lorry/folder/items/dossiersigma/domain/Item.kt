package lorry.folder.items.dossiersigma.domain

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import lorry.folder.items.dossiersigma.serviceComponents.utilities.CapsuleData
import lorry.folder.items.dossiersigma.serviceComponents.utilities.FileCapsuleManager
import lorry.folder.items.dossiersigma.serviceComponents.utilities.FolderCapsuleManager
import lorry.folder.items.dossiersigma.serviceComponents.utilities.IElementInCapsule
import lorry.folder.items.dossiersigma.serviceComponents.utilities.IElementReader
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

@Stable
abstract class Item(
    val path: String,
    val name: String,
    val picture: Any?,
    val id: String = UUID.randomUUID().toString(),
    val modificationDate: Long,
    val tag: ColoredTag? = null,
    val scale: ContentScale? = null,
    val memo: String? = null,
) {
    fun isFile(): Boolean {
        return this is SigmaFile
    }

    fun isFolder(): Boolean {
        return this is SigmaFolder
    }

    val fullPath: String
        get() = when (path.endsWith("/")) {
            true -> "$path$name"
            false -> "$path/$name"
        }

    val fileCapsuleManager = FileCapsuleManager(this.fullPath)
    val folderCapsuleManager = FolderCapsuleManager(this.fullPath)
    
    fun copy(
        path: String = this.path,
        name: String = this.name,
        picture: Any? = this.picture,
        tag: ColoredTag? = this.tag,
        scale: ContentScale? = this.scale,
        memo: String? = this.memo
    ): Item {
        if (this is SigmaFolder) {
            return this.copy(
                path = path,
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
                memo = memo
            )
        }
    }

    fun save(element: IElementInCapsule) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            if (this@Item is SigmaFolder) {
                folderCapsuleManager.save(element)
            } else {
                fileCapsuleManager.save(element)
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
                "hasUrl= ${picture is String}, path='$path', id='$id', modificationDate=$modificationDate, " +
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

