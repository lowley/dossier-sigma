package lorry.folder.items.dossiersigma.domain

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import com.pointlessapps.rt_editor.utils.RichTextValueSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import lorry.folder.items.dossiersigma.data.dataSaver.CompositeData
import lorry.folder.items.dossiersigma.data.dataSaver.FileCompositeManager
import lorry.folder.items.dossiersigma.data.dataSaver.FolderCompositeManager
import lorry.folder.items.dossiersigma.data.dataSaver.IElementInComposite
import lorry.folder.items.dossiersigma.data.dataSaver.IElementReader
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

@Stable
abstract class Item(
    var path: String,
    var name: String,
    var picture: Any?,
    var id: String = UUID.randomUUID().toString(),
    var modificationDate: Long,
    var tag: ColoredTag? = null,
    var scale: ContentScale? = null,
    var memo: String? = null
    
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

    val fileCompositeManager = FileCompositeManager(this.fullPath)
    val folderCompositeManager = FolderCompositeManager(this.fullPath)
    
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

    fun save(element: IElementInComposite) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            if (this@Item is SigmaFolder) {
                folderCompositeManager.save(element)
            } else {
                fileCompositeManager.save(element)
            }
        }
    }

    suspend fun getComposite(): CompositeData? {
        return if (this is SigmaFolder) {
            folderCompositeManager.getComposite()
        } else {
            fileCompositeManager.getComposite()
        }
    }

    suspend fun <T> getElement(reader: IElementReader<T>): T? {
        return if (this is SigmaFolder) {
            folderCompositeManager.getElement<T>(reader)
        } else {
            fileCompositeManager.getElement<T>(reader)
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
    val title: String
)

