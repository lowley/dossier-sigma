package lorry.folder.items.dossiersigma.data.dataSaver

import android.graphics.Bitmap
import androidx.compose.ui.layout.ContentScale
import com.google.gson.Gson
import com.pointlessapps.rt_editor.utils.RichTextValueSnapshot
import kotlinx.serialization.Serializable
import lorry.folder.items.dossiersigma.data.base64.IVideoInfoEmbedder
import lorry.folder.items.dossiersigma.data.base64.VideoInfoEmbedder
import lorry.folder.items.dossiersigma.domain.ColoredTag
import javax.inject.Inject

@Serializable
data class CompositeData(
    val initialPicture: String? = null,       // base64
    val croppedPicture: String? = null,       // base64
    val flag: String? = null,
    val scale: String? = null,
    val memo: String? = null
){
    val videoInfoEmbedder = VideoInfoEmbedder()
    
    suspend fun getInitialPicture(): Bitmap? {
        val base64 = initialPicture ?: return null
        return videoInfoEmbedder.base64ToBitmap(base64)
    }

    suspend fun getCroppedPicture(): Bitmap? {
        val base64 = croppedPicture ?: return null
        return videoInfoEmbedder.base64ToBitmap(base64)
    }

    fun getFlag(): ColoredTag? {
        return if (flag == null)
            null
        else 
            Gson().fromJson(flag, ColoredTag::class.java)
    }

    fun getScale(): ContentScale? {
        return if (scale == null)
            null 
        else
            Gson().fromJson(scale, ContentScale::class.java)
    }

    fun getMemo(): RichTextValueSnapshot? {
        return if (memo == null)
            null
        else
            Gson().fromJson(memo, RichTextValueSnapshot::class.java)
    }
}

interface IElementInComposite {

    suspend fun update(composite: CompositeData): CompositeData
}

data class InitialPicture @Inject constructor(
    val initialPicture: Bitmap,
    val videoInfoEmbedder: IVideoInfoEmbedder,
) : IElementInComposite {
    override suspend fun update(composite: CompositeData): CompositeData {
        val base64 = videoInfoEmbedder.bitmapToBase64(initialPicture)
        return composite.copy(initialPicture = base64)
    }

    companion object : IElementReader<Bitmap> {
        
        var videoInfoEmbedder = VideoInfoEmbedder()

        override suspend fun fileGet(filePath: String): Bitmap? {
            val fileCompositeManager = FileCompositeManager(filePath)
            val composite = fileCompositeManager.getComposite()
            val base64 = composite.initialPicture ?: return null
            return videoInfoEmbedder.base64ToBitmap(base64)
        }

        override suspend fun folderGet(folderPath: String): Bitmap? {
            val filePath = "$folderPath/.folderPicture.html"
            return fileGet(filePath)
        }
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

    companion object : IElementReader<Bitmap> {
        
        var videoInfoEmbedder = VideoInfoEmbedder()

        override suspend fun fileGet(filePath: String): Bitmap? {
            val fileCompositeManager = FileCompositeManager(filePath)
            val composite = fileCompositeManager.getComposite()
            val base64 = composite.croppedPicture ?: return null
            return videoInfoEmbedder.base64ToBitmap(base64)
        }

        override suspend fun folderGet(folderPath: String): Bitmap? {
            val filePath = "$folderPath/.folderPicture.html"
            return fileGet(filePath)
        }
    }
}

data class Flag @Inject constructor(
    val flag: ColoredTag
) : IElementInComposite {
    override suspend fun update(composite: CompositeData): CompositeData {
        val flagAsString = Gson().toJson(flag)
        return composite.copy(flag = flagAsString)
    }

    companion object : IElementReader<ColoredTag> {
        
        var videoInfoEmbedder = VideoInfoEmbedder()

        override suspend fun fileGet(filePath: String): ColoredTag? {
            val fileCompositeManager = FileCompositeManager(filePath)
            val composite = fileCompositeManager.getComposite()
            if (composite.flag == null)
                return null

            return Gson().fromJson(composite.flag, ColoredTag::class.java)
        }

        override suspend fun folderGet(folderPath: String): ColoredTag? {
            val filePath = "$folderPath/.folderPicture.html"
            return fileGet(filePath)
        }
    }
}

data class Scale @Inject constructor(
    val scale: ContentScale
) : IElementInComposite {
    override suspend fun update(composite: CompositeData): CompositeData {
        val scaleAsString = Gson().toJson(scale)
        return composite.copy(scale = scaleAsString)
    }

    companion object : IElementReader<ContentScale> {
        
        var videoInfoEmbedder = VideoInfoEmbedder()

        override suspend fun fileGet(filePath: String): ContentScale? {
            val fileCompositeManager = FileCompositeManager(filePath)
            val composite = fileCompositeManager.getComposite()
            if (composite.scale == null)
                return null

            return Gson().fromJson(composite.flag, ContentScale::class.java)
        }

        override suspend fun folderGet(folderPath: String): ContentScale? {
            val filePath = "$folderPath/.folderPicture.html"
            return fileGet(filePath)
        }
    }
}

data class Memo @Inject constructor(
    val memo: RichTextValueSnapshot
) : IElementInComposite {
    override suspend fun update(composite: CompositeData): CompositeData {
        val memoAsString = Gson().toJson(memo)
        return composite.copy(memo = memoAsString)
    }

    companion object : IElementReader<RichTextValueSnapshot> {
        
        var videoInfoEmbedder = VideoInfoEmbedder()

        override suspend fun fileGet(filePath: String): RichTextValueSnapshot? {
            val fileCompositeManager = FileCompositeManager(filePath)
            val composite = fileCompositeManager.getComposite()
            if (composite.memo == null)
                return null

            return Gson().fromJson(composite.memo, RichTextValueSnapshot::class.java)
        }

        override suspend fun folderGet(folderPath: String): RichTextValueSnapshot? {
            val filePath = "$folderPath/.folderPicture.html"
            return fileGet(filePath)
        }
    }
}

interface IElementReader<T> {
    suspend fun fileGet(filePath: String): T?
    suspend fun folderGet(folderPath: String): T?
}
