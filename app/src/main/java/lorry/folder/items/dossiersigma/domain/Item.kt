package lorry.folder.items.dossiersigma.domain

import android.graphics.Bitmap
import java.util.UUID

abstract class Item(
    val path: String,
    val name: String,
    val picture: Bitmap?,
    val id: String = UUID.randomUUID().toString()
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
        picture: Bitmap? = this.picture
    ): Item {
        if (this is SigmaFolder) {
            return (this as SigmaFolder).copy(
                path = path,
                name = name,
                picture = picture,
                id = id
            )
        } else {
            return (this as SigmaFile).copy(
                path = path,
                name = name,
                picture = picture,
                id = id
            )
        }
    }
}


