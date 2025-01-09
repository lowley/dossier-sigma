package lorry.folder.items.dossiersigma.domain

import android.graphics.Bitmap
import java.util.UUID

class SigmaFile(
    path: String,
    name: String,
    picture: Bitmap?,
    id: String = UUID.randomUUID().toString()
) : Item(path = path, name = name, picture = picture, id = id
) {
    override fun toString(): String {
        return "File(name=$name, picture=${if (picture == null) "non" else "oui"}, id=${id.take(5)})"
    }

    fun copy(
        path: String = this.path,
        name: String = this.name,
        picture: Bitmap? = this.picture,
        id: String = this.id
    ): SigmaFile {
        return SigmaFile(path = path, name = name, picture = picture, id = id)
    }
}
    