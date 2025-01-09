package lorry.folder.items.dossiersigma.domain

import android.graphics.Bitmap
import java.util.UUID

class SigmaFolder : Item {

    var items: List<Item> = emptyList()

    constructor(
        path: String,
        name: String,
        picture: Bitmap?,
        items: List<Item>,
        id: String = UUID.randomUUID().toString()
    ) : super(path, name, picture, id) {
        this.items = items
    }

    override fun toString(): String {
        return "Folder(name=$name, picture=${if (picture == null) "non" else "oui"}, id=${id.take(5)}, items: ${items.size})"
    }

    constructor(
        fullPath: String,
        picture: Bitmap?,
        items: List<Item>,
        id: String = UUID.randomUUID().toString()
    ) : super(
        path = fullPath.substringBeforeLast("/"),
        name = fullPath.substringAfterLast("/"),
        picture = picture,
        id = id
    ) {
        this.items = items
    }

    val isEmpty: Boolean
        get() = items.isEmpty()

    fun copy(
        path: String = this.path,
        name: String = this.name,
        picture: Bitmap? = this.picture,
        items: List<Item> = this.items,
        id: String = this.id
    ): SigmaFolder {
        val result = SigmaFolder(
            path = path, name = name, picture = picture, items = items.map { item ->
                when (item) {
                    is SigmaFolder -> item.copy()
                    is SigmaFile -> item.copy()
                    else -> throw IllegalArgumentException("Unknown item type: ${item::class}")
                }
            },
            id = id
        )
        return result
    }
}