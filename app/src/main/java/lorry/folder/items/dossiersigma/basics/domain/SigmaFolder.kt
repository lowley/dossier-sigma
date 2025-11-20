package lorry.folder.items.dossiersigma.basics.domain

import androidx.compose.ui.layout.ContentScale
import lorry.folder.items.dossiersigma.external.capsule.utilities.Country
import java.util.UUID

class SigmaFolder(
    val items: List<Item>,
    parentPath: SigmaPath,
    name: String,
    picture: Any?,
    id: String = UUID.randomUUID().toString(),
    modificationDate: Long,
    tag: ColoredTag?,
    scale: ContentScale?,
    memo: String? = null,
    country: Country? = null
) : Item(parentPath, name, picture, id, modificationDate, tag, scale, memo, null, country) {

    override fun toString(): String {
        return "Folder(name=$name, picture=${if (picture == null) "non" else "oui"}, id=${
            id.take(5)
        }, items: ${items.size}, modification: ${modificationDate.toFormattedDate()}), tag: ${tag}, scale: ${scale}, memo: $memo, country: $country)"
    }

    constructor(
        fullPath: SigmaPath,
        picture: Any?,
        items: List<Item>,
        id: String = UUID.randomUUID().toString(),
        modificationDate: Long,
        tag: ColoredTag?,
        scale: ContentScale?,
        memo: String? = null,
        country: Country? = null
    ) : this(
        parentPath = SigmaPath(fullPath.dropLastSegment()),
        name = fullPath.lastSegment,
        picture = picture,
        id = id,
        modificationDate = modificationDate,
        tag = tag,
        scale = scale,
        memo = memo,
        items = items,
        country = country
    )

    val isEmpty: Boolean
        get() = items.isEmpty()

    fun copy(
        parentPath: SigmaPath = this.parentPath,
        name: String = this.name,
        picture: Any? = this.picture,
        items: List<Item> = this.items,
        id: String = this.id,
        modificationDate: Long = this.modificationDate,
        tag: ColoredTag? = this.tag,
        scale: ContentScale? = this.scale,
        memo: String? = this.memo,
        country: Country? = this.country
    ): SigmaFolder {
        val result = SigmaFolder(
            parentPath = parentPath, name = name, picture = picture,
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
            memo = memo,
            country = country
        )
        return result
    }

    companion object{
        fun ofItemsAndPersistedSigmaFolder(
            items: List<Item>,
            fullPath: SigmaPath,
        ): SigmaFolder {
            val result = SigmaFolder(
                items = items,
                fullPath = fullPath,
                picture = null,
                modificationDate = 0L,
                tag = null,
                scale = null,
                memo = null,
                country = null
            )

            return result
        }

        val DUMMY = SigmaFolder(
            items = listOf(),
            fullPath = SigmaPath(""),
            picture = null,
            modificationDate = 0L,
            tag = null,
            scale = null,
            memo = null,
            country = null
        )


    }
}