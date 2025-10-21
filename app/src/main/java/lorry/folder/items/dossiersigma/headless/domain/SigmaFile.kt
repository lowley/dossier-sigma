package lorry.folder.items.dossiersigma.headless.domain

import androidx.compose.ui.layout.ContentScale
import java.util.UUID

class SigmaFile(
    parentPath: SigmaPath,
    name: String,
    picture: Any?,
    id: String = UUID.randomUUID().toString(),
    modificationDate: Long,
    tag: ColoredTag?,
    scale: ContentScale?,
    memo: String? = null,
    size: Long? = null
) : Item(parentPath = parentPath, name = name, picture = picture, id = id, modificationDate = modificationDate, memo = memo, tag =  tag, scale = scale, size = size
) {
    constructor(
        fullPath: SigmaPath,
        picture: Any?,
        id: String = UUID.randomUUID().toString(),
        modificationDate: Long,
        tag: ColoredTag?,
        scale: ContentScale?,
        memo: String? = null,
    ) : this(
        parentPath = fullPath.dropLastSegmentOfPath(),
        name = fullPath.lastSegment,
        picture = picture,
        id = id,
        modificationDate = modificationDate,
        tag = tag,
        scale = scale,
        memo = memo,
    )

    fun copy(
        path: SigmaPath = this.parentPath,
        name: String = this.name,
        picture: Any? = this.picture,
        id: String = this.id,
        modificationDate: Long = this.modificationDate,
        tag: ColoredTag? = this.tag,
        scale: ContentScale? = this.scale,
        memo: String? = this.memo,
        size: Long? = this.size
    ): SigmaFile {
        return SigmaFile(parentPath = path, name = name, picture = picture, id = id, modificationDate =
            modificationDate, tag = tag, scale = scale, memo = memo, size = size)
    }

    override fun toString(): String {
        return "SigmaFile(name='$name', path='${this@SigmaFile.parentPath}', picture=${picture != null}, id='${id.take(6)}', modificationDate=$modificationDate), tag=$tag, scale=$scale,memo=$memo, size=$size)"
    }
}
    