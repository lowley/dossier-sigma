package lorry.folder.items.dossiersigma.external.capsule.utilities

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.layout.ContentScale
import com.google.gson.Gson
import kotlinx.serialization.Serializable
import lorry.folder.items.dossiersigma.external.base64.IVideoInfoEmbedder
import lorry.folder.items.dossiersigma.external.base64.VideoInfoEmbedder
import lorry.folder.items.dossiersigma.headless.domain.ColoredTag
import lorry.folder.items.dossiersigma.headless.domain.SigmaPath
import lorry.folder.items.dossiersigma.headless.domain.str
import javax.inject.Inject

@Serializable
data class CapsuleData(
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
            try {
                StringToScale(gson.fromJson(scale, String::class.java))
            }catch(ex: Exception){
                Log.d("CompositeData", "getScale: $ex")
                ContentScale.Fit
            }
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

interface IElementInCapsule {

    suspend fun update(composite: CapsuleData): CapsuleData
}

data class InitialPicture @Inject constructor(
    val initialPicture: Any?,
    val videoInfoEmbedder: IVideoInfoEmbedder,
) : IElementInCapsule {
    override suspend fun update(composite: CapsuleData): CapsuleData {
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

        override suspend fun fileGet(filePath: SigmaPath, useOld: Boolean): Any? {
            val fileCompositeManager = FileCapsuleManager(filePath.str, useOld)
            val composite = fileCompositeManager.getCapsule()
            val initialData = composite.initialPicture ?: return null

            val initialInt = initialData.toIntOrNull()
            if (initialInt != null) {
                return initialInt
            }

            val initialBase64 = initialData
            val base64 = initialBase64
            return videoInfoEmbedder.base64ToBitmap(base64)
        }

        override suspend fun folderGet(folderPath: SigmaPath, useOld: Boolean): Any? {
            val filePath = folderPath.appendToPath(".folderPicture.html")
            return fileGet(filePath, useOld)
        }
    }
}

data class CroppedPicture @Inject constructor(
    val croppedPicture: Any?,
    val videoInfoEmbedder: IVideoInfoEmbedder,
) : IElementInCapsule {
    override suspend fun update(composite: CapsuleData): CapsuleData {
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

        override suspend fun fileGet(filePath: SigmaPath, useOld: Boolean): Any? {
            val fileCompositeManager = FileCapsuleManager(filePath.str, useOld)
            val composite = fileCompositeManager.getCapsule()
            val initialData = composite.croppedPicture ?: return null

            val initialInt = initialData.toIntOrNull()
            if (initialInt != null) {
                return initialInt
            }

            val initialBase64 = initialData as? String
            val base64 = initialBase64 ?: return null
            return videoInfoEmbedder.base64ToBitmap(base64)
        }

        override suspend fun folderGet(folderPath: SigmaPath, useOld: Boolean): Any? {
            val filePath = folderPath.appendToPath(".folderPicture.html")
            return fileGet(filePath, useOld)
        }
    }
}

data class Flag @Inject constructor(
    val flag: ColoredTag?
) : IElementInCapsule {
    val gson: Gson = Gson()

    override suspend fun update(composite: CapsuleData): CapsuleData {
        val flagAsString = gson.toJson(flag)
        return composite.copy(flag = flagAsString)
    }

    companion object : IElementReader<ColoredTag> {

        var videoInfoEmbedder = VideoInfoEmbedder()

        override suspend fun fileGet(filePath: SigmaPath, useOld: Boolean): ColoredTag? {
            val fileCompositeManager = FileCapsuleManager(filePath.str, useOld)
            val composite = fileCompositeManager.getCapsule()
            if (composite.flag == null)
                return null

            return Gson().fromJson(composite.flag, ColoredTag::class.java)
        }

        override suspend fun folderGet(folderPath: SigmaPath, useOld: Boolean): ColoredTag? {
            val filePath = folderPath.appendToPath(".folderPicture.html")
            return fileGet(filePath, useOld)
        }
    }
}

data class Scale @Inject constructor(
    val scale: ContentScale?
) : IElementInCapsule {
    override suspend fun update(composite: CapsuleData): CapsuleData {
        val scaleAsString = scaleToString(scale)
        return composite.copy(scale = scaleAsString)
    }

    companion object : IElementReader<ContentScale> {

        var videoInfoEmbedder = VideoInfoEmbedder()
        val gson: Gson = Gson()

        override suspend fun fileGet(filePath: SigmaPath, useOld: Boolean): ContentScale? {
            val fileCompositeManager = FileCapsuleManager(filePath.str, useOld)
            val composite = fileCompositeManager.getCapsule()
            if (composite.scale == null)
                return null

            val scaleAsString = gson.fromJson(composite.scale, String::class.java)

            val scale = StringToScale(scaleAsString)
            return scale
        }

        override suspend fun folderGet(folderPath: SigmaPath, useOld: Boolean): ContentScale? {
            val filePath = folderPath.appendToPath(".folderPicture.html")
            return fileGet(filePath, useOld)
        }
    }
}

data class Memo @Inject constructor(
    val memo: String?
) : IElementInCapsule {
    val gson: Gson = Gson()

    override suspend fun update(capsule: CapsuleData): CapsuleData {
//        val memoAsString = gson.toJson(memo)
        val memoAsString = if (memo.isNullOrEmpty()) null else memo
        return capsule.copy(memo2 = memoAsString)
    }

    companion object : IElementReader<String> {

        val gson: Gson = Gson()

        override suspend fun fileGet(filePath: SigmaPath, useOld: Boolean): String? {
            val fileCapsuleManager = FileCapsuleManager(filePath.str, useOld)
            val capsule = fileCapsuleManager.getCapsule()
            if (capsule.memo2 == null)
                return null

//            return gson.fromJson(composite.memo2, String::class.java)
            return capsule.memo2
        }

        override suspend fun folderGet(folderPath: SigmaPath, useOld: Boolean): String? {
            val filePath = folderPath.appendToPath(".folderPicture.html")
            return fileGet(filePath, useOld)
        }
    }
}

interface IElementReader<T> {
    suspend fun fileGet(filePath: SigmaPath, useOld: Boolean = false): T?
    suspend fun folderGet(folderPath: SigmaPath, useOld: Boolean = false): T?
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