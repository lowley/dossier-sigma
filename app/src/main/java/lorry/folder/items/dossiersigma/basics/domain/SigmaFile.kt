package lorry.folder.items.dossiersigma.basics.domain

import androidx.compose.ui.layout.ContentScale
import lorry.folder.items.dossiersigma.external.capsule.utilities.Country
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
    size: Long? = null,
    country: Country? = null
) : Item(parentPath = parentPath, name = name, picture = picture, id = id, modificationDate = modificationDate, memo = memo, tag =  tag, scale = scale, size = size, country = country
) {
    constructor(
        fullPath: SigmaPath,
        picture: Any?,
        id: String = UUID.randomUUID().toString(),
        modificationDate: Long,
        tag: ColoredTag?,
        scale: ContentScale?,
        memo: String? = null,
        country: Country? = null
    ) : this(
        parentPath = fullPath.dropLastSegmentOfPath(),
        name = fullPath.lastSegment,
        picture = picture,
        id = id,
        modificationDate = modificationDate,
        tag = tag,
        scale = scale,
        memo = memo,
        country = country
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
        size: Long? = this.size,
        country: Country? = this.country
    ): SigmaFile {
        return SigmaFile(parentPath = path, name = name, picture = picture, id = id, modificationDate =
            modificationDate, tag = tag, scale = scale, memo = memo, size = size, country = country)
    }

    override fun toString(): String {
        return "SigmaFile(name='$name', path='${this@SigmaFile.parentPath}', picture=${picture != null}, id='${id.take(6)}', modificationDate=$modificationDate), tag=$tag, scale=$scale,memo=$memo, size=$size, country=$country)"
    }
}
    