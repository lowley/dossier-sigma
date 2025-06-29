package lorry.folder.items.dossiersigma.domain

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
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
    var tag: ColoredTag? = null
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

    fun copy(
        path: String = this.path,
        name: String = this.name,
        picture: Any? = this.picture
    ): Item {
        if (this is SigmaFolder) {
            return (this as SigmaFolder).copy(
                path = path,
                name = name,
                picture = picture,
                id = id,
                modificationDate = modificationDate
            )
        } else {
            return (this as SigmaFile).copy(
                path = path,
                name = name,
                picture = picture,
                id = id,
                modificationDate = modificationDate
            )
        }
    }

    override fun toString(): String {
        return "Item(type=${if (isFile()) "File" else "Folder"}, name='$name', picture=${picture != null}, hasUrl= ${picture is String}, path='$path', id='$id', modificationDate=$modificationDate, fullPath='$fullPath')"
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

