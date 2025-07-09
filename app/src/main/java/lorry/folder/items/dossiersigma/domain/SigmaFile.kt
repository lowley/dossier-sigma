package lorry.folder.items.dossiersigma.domain

import android.graphics.Bitmap
import androidx.annotation.ReturnThis
import androidx.compose.ui.layout.ContentScale
import com.pointlessapps.rt_editor.utils.RichTextValueSnapshot
import lorry.folder.items.dossiersigma.data.dataSaver.Memo
import java.util.UUID

class SigmaFile(
    path: String,
    name: String,
    picture: Any?,
    id: String = UUID.randomUUID().toString(),
    modificationDate: Long,
    tag: ColoredTag?,
    scale: ContentScale?,
    memo: String? = null
) : Item(path = path, name = name, picture = picture, id = id, modificationDate = modificationDate, memo = memo, tag =  tag, scale = scale
) {

    fun copy(
        path: String = this.path,
        name: String = this.name,
        picture: Any? = this.picture,
        id: String = this.id,
        modificationDate: Long = this.modificationDate,
        tag: ColoredTag? = this.tag,
        scale: ContentScale? = this.scale,
        memo: String? = this.memo
    ): SigmaFile {
        return SigmaFile(path = path, name = name, picture = picture, id = id, modificationDate = 
            modificationDate, tag = tag, scale = scale, memo = memo)
    }

    override fun toString(): String {
        return "SigmaFile(name='$name', path='$path', picture=${picture != null}, id='${id.take(6)}', modificationDate=$modificationDate), tag=${tag}, scale=${scale},memo=${memo}"
    }
}
    