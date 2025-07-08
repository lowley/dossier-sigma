package lorry.folder.items.dossiersigma.data.dataSaver

import android.graphics.Bitmap
import androidx.compose.ui.layout.ContentScale
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.pointlessapps.rt_editor.model.Style
import com.pointlessapps.rt_editor.model.Style.ParagraphStyle
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
    val memo: String? = null,
    val memo2: String? = null
) {
    val videoInfoEmbedder = VideoInfoEmbedder()
    @Transient
    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Style::class.java, StyleAdapter())
        .create()
    
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
            gson.fromJson(flag, ColoredTag::class.java)
    }

    fun getScale(): ContentScale? {
        return if (scale == null)
            null
        else
            gson.fromJson(scale, ContentScale::class.java)
    }

    fun getMemo(): RichTextValueSnapshot? {
        return if (memo == null)
            null
        else
            gson.fromJson(memo, RichTextValueSnapshot::class.java)
    }

    override fun toString(): String {
        return "CompositeData(initialPicture=${initialPicture?.takeLast(10)}, " +
                "croppedPicture=${croppedPicture?.takeLast(10)}, " +
                "flag=$flag, scale=$scale, memo=${memo?.substring(memo.indexOf("text"))?.take(20)})"
    }
}

data class MemoData(
    val text: String,
    val spanStyles: List<Style>,
    val paragraphStyles: List<ParagraphStyle>
    
    
)

interface IElementInComposite {

    suspend fun update(composite: CompositeData): CompositeData
}

data class InitialPicture @Inject constructor(
    val initialPicture: Bitmap?,
    val videoInfoEmbedder: IVideoInfoEmbedder,
) : IElementInComposite {
    override suspend fun update(composite: CompositeData): CompositeData {
        val base64 = if (initialPicture != null)
            videoInfoEmbedder.bitmapToBase64(initialPicture)
        else null
        return composite.copy(initialPicture = base64)
    }

    companion object : IElementReader<Bitmap> {

        var videoInfoEmbedder = VideoInfoEmbedder()

        override suspend fun fileGet(filePath: String, useOld: Boolean): Bitmap? {
            val fileCompositeManager = FileCompositeManager(filePath, useOld)
            val composite = fileCompositeManager.getComposite()
            val base64 = composite.initialPicture ?: return null
            return videoInfoEmbedder.base64ToBitmap(base64)
        }

        override suspend fun folderGet(folderPath: String, useOld: Boolean): Bitmap? {
            val filePath = "$folderPath/.folderPicture.html"
            return fileGet(filePath, useOld)
        }
    }
}

data class CroppedPicture @Inject constructor(
    val croppedPicture: Bitmap?,
    val videoInfoEmbedder: IVideoInfoEmbedder,
) : IElementInComposite {
    override suspend fun update(composite: CompositeData): CompositeData {
        val base64 = if (croppedPicture != null)
            videoInfoEmbedder.bitmapToBase64(croppedPicture)
        else null
        return composite.copy(croppedPicture = base64)
    }

    companion object : IElementReader<Bitmap> {

        var videoInfoEmbedder = VideoInfoEmbedder()

        override suspend fun fileGet(filePath: String, useOld: Boolean): Bitmap? {
            val fileCompositeManager = FileCompositeManager(filePath, useOld)
            val composite = fileCompositeManager.getComposite()
            val base64 = composite.croppedPicture ?: return null
            return videoInfoEmbedder.base64ToBitmap(base64)
        }

        override suspend fun folderGet(folderPath: String, useOld: Boolean): Bitmap? {
            val filePath = "$folderPath/.folderPicture.html"
            return fileGet(filePath, useOld)
        }
    }
}

data class Flag @Inject constructor(
    val flag: ColoredTag?
) : IElementInComposite {
    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Style::class.java, StyleAdapter())
        .create()
    
    override suspend fun update(composite: CompositeData): CompositeData {
        val flagAsString = gson.toJson(flag)
        return composite.copy(flag = flagAsString)
    }

    companion object : IElementReader<ColoredTag> {

        var videoInfoEmbedder = VideoInfoEmbedder()

        override suspend fun fileGet(filePath: String, useOld: Boolean): ColoredTag? {
            val fileCompositeManager = FileCompositeManager(filePath, useOld)
            val composite = fileCompositeManager.getComposite()
            if (composite.flag == null)
                return null

            return Gson().fromJson(composite.flag, ColoredTag::class.java)
        }

        override suspend fun folderGet(folderPath: String, useOld: Boolean): ColoredTag? {
            val filePath = "$folderPath/.folderPicture.html"
            return fileGet(filePath, useOld)
        }
    }
}

data class Scale @Inject constructor(
    val scale: ContentScale?
) : IElementInComposite {
    override suspend fun update(composite: CompositeData): CompositeData {
        val scaleAsString = scaleToString(scale)
        return composite.copy(scale = scaleAsString)
    }

    companion object : IElementReader<ContentScale> {

        var videoInfoEmbedder = VideoInfoEmbedder()
        val gson: Gson = GsonBuilder()
            .registerTypeAdapter(Style::class.java, StyleAdapter())
            .create()
        
        override suspend fun fileGet(filePath: String, useOld: Boolean): ContentScale? {
            val fileCompositeManager = FileCompositeManager(filePath, useOld)
            val composite = fileCompositeManager.getComposite()
            if (composite.scale == null)
                return null

            val scaleAsString = gson.fromJson(composite.scale, String::class.java)

            val scale = StringToScale(scaleAsString)
            return scale
        }

        override suspend fun folderGet(folderPath: String, useOld: Boolean): ContentScale? {
            val filePath = "$folderPath/.folderPicture.html"
            return fileGet(filePath, useOld)
        }
    }
}

data class Memo @Inject constructor(
    val memo: RichTextValueSnapshot?
) : IElementInComposite {
    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Style::class.java, StyleAdapter())
        .create()
    
    override suspend fun update(composite: CompositeData): CompositeData {
        val memoAsString = gson.toJson(memo)
        return composite.copy(memo = memoAsString)
    }

    companion object : IElementReader<RichTextValueSnapshot> {

        var videoInfoEmbedder = VideoInfoEmbedder()
        val gson: Gson = GsonBuilder()
            .registerTypeAdapter(Style::class.java, StyleAdapter())
            .create()
        
        override suspend fun fileGet(filePath: String, useOld: Boolean): RichTextValueSnapshot? {
            val fileCompositeManager = FileCompositeManager(filePath, useOld)
            val composite = fileCompositeManager.getComposite()
            if (composite.memo == null)
                return null

            return gson.fromJson(composite.memo, RichTextValueSnapshot::class.java)
        }

        override suspend fun folderGet(folderPath: String, useOld: Boolean): RichTextValueSnapshot? {
            val filePath = "$folderPath/.folderPicture.html"
            return fileGet(filePath, useOld)
        }
    }
}

interface IElementReader<T> {
    suspend fun fileGet(filePath: String, useOld: Boolean = false): T?
    suspend fun folderGet(folderPath: String, useOld: Boolean = false): T?
}

fun StringToScale(value: String): ContentScale? = when (value) {
    "Crop" -> ContentScale.Crop
    "Fit" -> ContentScale.Fit
    "FillBounds" -> ContentScale.FillBounds
    "FillHeight" -> ContentScale.FillHeight
    "FillWidth" -> ContentScale.FillWidth
    "Inside" -> ContentScale.Inside
    "None" -> ContentScale.None
    else -> null // ou exception
}

fun scaleToString(value: ContentScale?): String = when (value) {
    ContentScale.Crop -> "Crop"
    ContentScale.Fit -> "Fit"
    ContentScale.FillBounds -> "FillBounds"
    ContentScale.FillHeight -> "FillHeight"
    ContentScale.FillWidth -> "FillWidth"
    ContentScale.Inside -> "Inside"
    ContentScale.None -> "None"
    else -> "Crop" // ou exception
}