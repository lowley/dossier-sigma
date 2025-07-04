package lorry.folder.items.dossiersigma.data.dataSaver

import android.graphics.Bitmap
import androidx.compose.ui.layout.ContentScale
import com.google.gson.Gson
import kotlinx.serialization.Serializable
import lorry.folder.items.dossiersigma.data.base64.IVideoInfoEmbedder
import lorry.folder.items.dossiersigma.domain.ColoredTag
import javax.inject.Inject

@Serializable
data class CompositeData(
    val picture: String? = null,       // base64
    val flag: String? = null,     // par ex. "#FF0000"
    val scale: String? = null,   // id, style, etc.
    val memo: String? = null          // JSON string ou HTML enrichi
)

interface IElementInComposite {

    suspend fun update(composite: CompositeData): CompositeData
//    abstract suspend fun get(): Any
//
}

data class Picture @Inject constructor(
    val picture: Bitmap,
    val videoInfoEmbedder: IVideoInfoEmbedder,
) : IElementInComposite {
    override suspend fun update(composite: CompositeData): CompositeData {
        val base64 = videoInfoEmbedder.bitmapToBase64(picture)
        return composite.copy(picture = base64)
    }
}


data class Flag @Inject constructor(
    val flag: ColoredTag
) : IElementInComposite {
    override suspend fun update(composite: CompositeData): CompositeData {
        val flagAsString = Gson().toJson(flag)
        return composite.copy(flag = flagAsString)
    }
}

data class Scale @Inject constructor(
    val scale: ContentScale
) : IElementInComposite {
    override suspend fun update(composite: CompositeData): CompositeData {
        val scaleAsString = Gson().toJson(scale)
        return composite.copy(scale = scaleAsString)
    }
}

data class Memo @Inject constructor(
    val scale: ContentScale
) : IElementInComposite {
    override suspend fun update(composite: CompositeData): CompositeData {
        val scaleAsString = Gson().toJson(scale)
        return composite.copy(scale = scaleAsString)
    }
}

