package lorry.folder.items.dossiersigma.domain

import androidx.compose.ui.layout.ContentScale
import com.pointlessapps.rt_editor.utils.RichTextValueSnapshot
import lorry.folder.items.dossiersigma.ui.components.TagInfosDialog
import java.util.UUID

class SigmaFolder : Item {

    var items: List<Item> = emptyList()

    constructor(
        path: String,
        name: String,
        picture: Any?,
        items: List<Item>,
        id: String = UUID.randomUUID().toString(),
        modificationDate: Long,
        tag: ColoredTag?,
        scale: ContentScale?,
        memo: RichTextValueSnapshot?
    ) : super(path, name, picture, id, modificationDate, tag,  scale, memo) {
        this.items = items
    }

    override fun toString(): String {
        return "Folder(name=$name, picture=${if (picture == null) "non" else "oui"}, id=${id.take(5)}, " +
                "items: ${items.size}, modification: ${modificationDate.toFormattedDate()}), tag: ${tag}, scale: ${scale}, memo: ${memo}"
    }

    constructor(
        fullPath: String,
        picture: Any?,
        items: List<Item>,
        id: String = UUID.randomUUID().toString(),
        modificationDate: Long,
        tag: ColoredTag?,
        scale: ContentScale?,
        memo: RichTextValueSnapshot
    ) : super(
        path = fullPath.substringBeforeLast("/"),
        name = fullPath.substringAfterLast("/"),
        picture = picture,
        id = id,
        modificationDate = modificationDate,
        tag = tag,
        scale = scale,
        memo = memo
    ) {
        this.items = items
    }

    val isEmpty: Boolean
        get() = items.isEmpty()

    fun copy(
        path: String = this.path,
        name: String = this.name,
        picture: Any? = this.picture,
        items: List<Item> = this.items,
        id: String = this.id,
        modificationDate: Long = this.modificationDate,
        tag: ColoredTag? = this.tag, 
        scale: ContentScale? = this.scale,
        memo: RichTextValueSnapshot? = this.memo
    ): SigmaFolder {
        val result = SigmaFolder(
            path = path, name = name, picture = picture, 
            items = items.map { item ->
                when (item) {
                    is SigmaFolder -> item.copy()
                    is SigmaFile -> item.copy()
                    else -> throw IllegalArgumentException("Unknown item type: ${item::class}")
                }
            },
            id = id,
            modificationDate = modificationDate,
            tag = tag,
            scale = scale,
            memo = memo
        )
        return result
    }


}