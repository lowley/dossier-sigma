package lorry.folder.items.dossiersigma.domain

import android.graphics.Bitmap
import java.util.UUID

class SigmaFile(
    path: String,
    name: String,
    picture: Any?,
    id: String = UUID.randomUUID().toString(),
    modificationDate: Long
) : Item(path = path, name = name, picture = picture, id = id, modificationDate = modificationDate
) {
    override fun toString(): String {
        return "File(name=$name, picture=${if (picture == null) "non" else "oui"}, id=${id.take(5)})"
    }

    fun copy(
        path: String = this.path,
        name: String = this.name,
        picture: Any? = this.picture,
        id: String = this.id,
        modificationDate: Long = this.modificationDate
    ): SigmaFile {
        return SigmaFile(path = path, name = name, picture = picture, id = id, modificationDate = modificationDate)
    }
}
    