package lorry.folder.items.dossiersigma.data.dataSaver

import android.graphics.Bitmap
import androidx.compose.ui.layout.ContentScale
import com.google.gson.Gson
import com.pointlessapps.rt_editor.utils.RichTextValueSnapshot
import kotlinx.serialization.Serializable
import lorry.folder.items.dossiersigma.data.base64.IVideoInfoEmbedder
import lorry.folder.items.dossiersigma.domain.ColoredTag
import javax.inject.Inject

@Serializable
data class CompositeData(
    val initialPicture: String? = null,       // base64
    val croppedPicture: String? = null,       // base64
    val flag: String? = null,     
    val scale: String? = null,  
    val memo: String? = null    
)

interface IElementInComposite {

    suspend fun update(composite: CompositeData): CompositeData
    abstract suspend fun fileGet(filePath: String): Any?
    abstract suspend fun folderGet(folderPath: String): Any?
}

data class Picture @Inject constructor(
    val initialPicture: Bitmap,
    val videoInfoEmbedder: IVideoInfoEmbedder,
) : IElementInComposite {
    override suspend fun update(composite: CompositeData): CompositeData {
        val base64 = videoInfoEmbedder.bitmapToBase64(initialPicture)
        return composite.copy(initialPicture = base64)
    }

    override suspend fun fileGet(filePath: String): Bitmap? {
        val fileCompositeManager = FileCompositeManager(filePath)
        val composite = fileCompositeManager.get()
        if (composite.initialPicture == null)
            return null
        
        return videoInfoEmbedder.base64ToBitmap(composite.initialPicture)
    }

    override suspend fun folderGet(folderPath: String): Bitmap? {
        val filePath = "$folderPath/.folderPicture.html"
        return fileGet(filePath)
    }
}

data class CroppedPicture @Inject constructor(
    val croppedPicture: Bitmap,
    val videoInfoEmbedder: IVideoInfoEmbedder,
) : IElementInComposite {
    override suspend fun update(composite: CompositeData): CompositeData {
        val base64 = videoInfoEmbedder.bitmapToBase64(croppedPicture)
        return composite.copy(croppedPicture = base64)
    }

    override suspend fun fileGet(filePath: String): Bitmap? {
        val fileCompositeManager = FileCompositeManager(filePath)
        val composite = fileCompositeManager.get()
        if (composite.croppedPicture == null)
            return null

        return videoInfoEmbedder.base64ToBitmap(composite.croppedPicture)
    }

    override suspend fun folderGet(folderPath: String): Bitmap? {
        val filePath = "$folderPath/.folderPicture.html"
        return fileGet(filePath)
    }
}

data class Flag @Inject constructor(
    val flag: ColoredTag
) : IElementInComposite {
    override suspend fun update(composite: CompositeData): CompositeData {
        val flagAsString = Gson().toJson(flag)
        return composite.copy(flag = flagAsString)
    }

    override suspend fun fileGet(filePath: String): ColoredTag? {
        val fileCompositeManager = FileCompositeManager(filePath)
        val composite = fileCompositeManager.get()
        if (composite.flag == null)
            return null

        return Gson().fromJson(composite.flag, ColoredTag::class.java)
    }

    override suspend fun folderGet(folderPath: String): ColoredTag? {
        val filePath = "$folderPath/.folderPicture.html"
        return fileGet(filePath)
    }
}

data class Scale @Inject constructor(
    val scale: ContentScale
) : IElementInComposite {
    override suspend fun update(composite: CompositeData): CompositeData {
        val scaleAsString = Gson().toJson(scale)
        return composite.copy(scale = scaleAsString)
    }

    override suspend fun fileGet(filePath: String): ContentScale? {
        val fileCompositeManager = FileCompositeManager(filePath)
        val composite = fileCompositeManager.get()
        if (composite.scale == null)
            return null

        return Gson().fromJson(composite.flag, ContentScale::class.java)
    }

    override suspend fun folderGet(folderPath: String): ContentScale? {
        val filePath = "$folderPath/.folderPicture.html"
        return fileGet(filePath)
    }
}

data class Memo @Inject constructor(
    val memo: RichTextValueSnapshot
) : IElementInComposite {
    override suspend fun update(composite: CompositeData): CompositeData {
        val memoAsString = Gson().toJson(memo)
        return composite.copy(memo = memoAsString)
    }

    override suspend fun fileGet(filePath: String): RichTextValueSnapshot? {
        val fileCompositeManager = FileCompositeManager(filePath)
        val composite = fileCompositeManager.get()
        if (composite.memo == null)
            return null

        return Gson().fromJson(composite.memo, RichTextValueSnapshot::class.java)
    }

    override suspend fun folderGet(folderPath: String): RichTextValueSnapshot? {
        val filePath = "$folderPath/.folderPicture.html"
        return fileGet(filePath)
    }
}

