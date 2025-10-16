package lorry.folder.items.dossiersigma.headless.domain

import androidx.compose.ui.layout.ContentScale
import java.util.UUID

class SigmaFile(
    path: String,
    name: String,
    picture: Any?,
    id: String = UUID.randomUUID().toString(),
    modificationDate: Long,
    tag: ColoredTag?,
    scale: ContentScale?,
    memo: String? = null,
    size: Long? = null
) : Item(parentPath = path, name = name, picture = picture, id = id, modificationDate = modificationDate, memo = memo, tag =  tag, scale = scale, size = size
) {
    constructor(
        fullPath: String,
        picture: Any?,
        id: String = UUID.randomUUID().toString(),
        modificationDate: Long,
        tag: ColoredTag?,
        scale: ContentScale?,
        memo: String? = null,
    ) : this(
        path = fullPath.substringBeforeLast("/"),
        name = fullPath.substringAfterLast("/"),
        picture = picture,
        id = id,
        modificationDate = modificationDate,
        tag = tag,
        scale = scale,
        memo = memo,
    )

    fun copy(
        path: String = this.parentPath,
        name: String = this.name,
        picture: Any? = this.picture,
        id: String = this.id,
        modificationDate: Long = this.modificationDate,
        tag: ColoredTag? = this.tag,
        scale: ContentScale? = this.scale,
        memo: String? = this.memo,
        size: Long? = this.size
    ): SigmaFile {
        return SigmaFile(path = path, name = name, picture = picture, id = id, modificationDate = 
            modificationDate, tag = tag, scale = scale, memo = memo, size = size)
    }

    override fun toString(): String {
        return "SigmaFile(name='$name', path='$parentPath', picture=${picture != null}, id='${id.take(6)}', modificationDate=$modificationDate), tag=$tag, scale=$scale,memo=$memo, size=$size)"
    }
}
    