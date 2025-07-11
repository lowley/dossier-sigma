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
import lorry.folder.items.dossiersigma.data.dataSaver.InitialPicture.Companion
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
    val gson: Gson = Gson()

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

    fun getTheMemo(): String? {
        return if (memo == null)
            null
        else
            gson.fromJson(memo, String::class.java)
    }

    override fun toString(): String {
        return "CompositeData(initialPicture=${initialPicture?.takeLast(10)}, " +
                "croppedPicture=${croppedPicture?.takeLast(10)}, " +
                "flag=$flag, scale=$scale, memo=${memo?.take(20)})"
    }
}

interface IElementInComposite {

    suspend fun update(composite: CompositeData): CompositeData
}

data class InitialPicture @Inject constructor(
    val initialPicture: Any?,
    val videoInfoEmbedder: IVideoInfoEmbedder,
) : IElementInComposite {
    override suspend fun update(composite: CompositeData): CompositeData {
        if (initialPicture == null)
            return composite.copy(initialPicture = null)

        val intImage = initialPicture as? Int
        if (intImage != null)
            return composite.copy(initialPicture = intImage.toString())

        val base64 = (initialPicture as? Bitmap)?.let {
            videoInfoEmbedder.bitmapToBase64(it)
        }
        if (base64 != null)
            return composite.copy(initialPicture = base64)

        return composite
    }

    companion object : IElementReader<Any> {

        var videoInfoEmbedder = VideoInfoEmbedder()

        override suspend fun fileGet(filePath: String, useOld: Boolean): Any? {
            val fileCompositeManager = FileCompositeManager(filePath, useOld)
            val composite = fileCompositeManager.getComposite()
            val initialData = composite.initialPicture ?: return null

            val initialInt = initialData.toIntOrNull()
            if (initialInt != null) {
                return initialInt
            }

            val initialBase64 = initialData as? String
            val base64 = initialBase64 ?: return null
            return videoInfoEmbedder.base64ToBitmap(base64)
        }

        override suspend fun folderGet(folderPath: String, useOld: Boolean): Any? {
            val filePath = "$folderPath/.folderPicture.html"
            return fileGet(filePath, useOld)
        }
    }
}

data class CroppedPicture @Inject constructor(
    val croppedPicture: Any?,
    val videoInfoEmbedder: IVideoInfoEmbedder,
) : IElementInComposite {
    override suspend fun update(composite: CompositeData): CompositeData {
        if (croppedPicture == null)
            return composite.copy(croppedPicture = null)

        val intImage = croppedPicture as? Int
        if (intImage != null)
            return composite.copy(croppedPicture = intImage.toString())

        val base64 = (croppedPicture as? Bitmap)?.let {
            videoInfoEmbedder.bitmapToBase64(it)
        }
        if (base64 != null)
            return composite.copy(croppedPicture = base64)

        return composite
    }

    companion object : IElementReader<Any> {

        var videoInfoEmbedder = VideoInfoEmbedder()

        override suspend fun fileGet(filePath: String, useOld: Boolean): Any? {
            val fileCompositeManager = FileCompositeManager(filePath, useOld)
            val composite = fileCompositeManager.getComposite()
            val initialData = composite.croppedPicture ?: return null

            val initialInt = initialData.toIntOrNull()
            if (initialInt != null) {
                return initialInt
            }

            val initialBase64 = initialData as? String
            val base64 = initialBase64 ?: return null
            return videoInfoEmbedder.base64ToBitmap(base64)
        }

        override suspend fun folderGet(folderPath: String, useOld: Boolean): Any? {
            val filePath = "$folderPath/.folderPicture.html"
            return fileGet(filePath, useOld)
        }
    }
}

data class Flag @Inject constructor(
    val flag: ColoredTag?
) : IElementInComposite {
    val gson: Gson = Gson()

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
        val gson: Gson = Gson()

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
    val memo: String?
) : IElementInComposite {
    val gson: Gson = Gson()

    override suspend fun update(composite: CompositeData): CompositeData {
//        val memoAsString = gson.toJson(memo)
        val memoAsString = if (memo.isNullOrEmpty()) null else memo
        return composite.copy(memo2 = memoAsString)
    }

    companion object : IElementReader<String> {

        val gson: Gson = Gson()

        override suspend fun fileGet(filePath: String, useOld: Boolean): String? {
            val fileCompositeManager = FileCompositeManager(filePath, useOld)
            val composite = fileCompositeManager.getComposite()
            if (composite.memo2 == null)
                return null

//            return gson.fromJson(composite.memo2, String::class.java)
            return composite.memo2
        }

        override suspend fun folderGet(folderPath: String, useOld: Boolean): String? {
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