package lorry.folder.items.dossiersigma.headless.favoriteObservation.external

import android.graphics.Bitmap
import androidx.compose.ui.layout.ContentScale
import com.google.gson.Gson
import lorry.folder.items.dossiersigma.basics.domain.ColoredTag
import lorry.folder.items.dossiersigma.basics.domain.SigmaFile
import lorry.folder.items.dossiersigma.basics.domain.SigmaFolder

object SigmaFolderMapping {
    private val json = kotlinx.serialization.json.Json {
        encodeDefaults = true; ignoreUnknownKeys = true
    }

    fun toPersisted(
        f: SigmaFolder,
        saveBitmap: (Bitmap) -> String
    ): PersistedSigmaFolder {
        return PersistedSigmaFolder(
            path = f.fullPath,
            items = f.items.map { item ->

                val pic = when (val any = item.picture /* Any? */) {
                    is Int -> PictureStore(PictureStore.Kind.RES_ID, resId = any)
                    is Bitmap -> {
                        val path = saveBitmap(any) // -> retourne un filePath dans /files/bitmaps/
                        PictureStore(PictureStore.Kind.BITMAP_FILE, filePath = path)
                    }

                    null -> PictureStore(PictureStore.Kind.NONE)
                    else -> PictureStore(PictureStore.Kind.NONE) // autres cas ignorés/projetés
                }

                PersistedItem(
                    name = item.name,
                    modificationDate = item.modificationDate,
                    tagId = Gson().toJson(item.tag),
                    picture = pic,
                    isFolder = item.isFolder(),
                    fullPath = item.fullPath,
                    memo = item.memo,
                    scale = when(item.scale){
                        ContentScale.Fit -> "Fit"
                        ContentScale.Crop -> "Crop"
                        ContentScale.FillBounds -> "FillBounds"
                        ContentScale.FillHeight -> "FillHeight"
                        ContentScale.FillWidth -> "FillWidth"
                        ContentScale.Inside -> "Inside"
                        else -> null
                    }
                )
            },
            meta = mapOf("name" to f.name) /* si tu as des métadonnées simples */
//            meta = f.meta /* si tu as des métadonnées simples */
        )
    }

    fun fromPersisted(
        p: PersistedSigmaFolder,
        loadBitmap: (String) -> Bitmap?
    ): SigmaFolder {
        val items = p.items.map { pi ->
            val any: Any? = when (pi.picture?.kind) {
                PictureStore.Kind.RES_ID -> pi.picture.resId
                PictureStore.Kind.BITMAP_FILE -> pi.picture.filePath?.let(loadBitmap)
                else -> null
            }
            if (pi.isFolder)
                SigmaFolder(
//                    name = pi.name,
                    modificationDate = pi.modificationDate,
                    tag = pi.tagId?.let { Gson().fromJson(it, ColoredTag::class.java) },
                    picture = any,
                    fullPath = pi.fullPath,
                    scale = when (pi.scale){
                        "Fit" -> ContentScale.Fit
                        "Crop" -> ContentScale.Crop
                        "FillBounds" -> ContentScale.FillBounds
                        "FillHeight" -> ContentScale.FillHeight
                        "FillWidth" -> ContentScale.FillWidth
                        "Inside" -> ContentScale.Inside
                        else -> null
                    },
                    items = emptyList(),
                    memo = pi.memo
                )
            else
                SigmaFile(
//                    name = pi.name,
                    modificationDate = pi.modificationDate,
                    tag = pi.tagId?.let { Gson().fromJson(it, ColoredTag::class.java) },
                    picture = any,
                    parentPath = pi.fullPath.dropLastSegmentOfPath(),
                    name = pi.name,
                    scale = when (pi.scale){
                        "Fit" -> ContentScale.Fit
                        "Crop" -> ContentScale.Crop
                        "FillBounds" -> ContentScale.FillBounds
                        "FillHeight" -> ContentScale.FillHeight
                        "FillWidth" -> ContentScale.FillWidth
                        "Inside" -> ContentScale.Inside
                        else -> null
                    },
                    memo = pi.memo
                )
        }

        return SigmaFolder.ofItemsAndPersistedSigmaFolder(
            items,
            fullPath = p.path,
            )
    }

    fun encodeToString(f: SigmaFolder, saveBitmap: (android.graphics.Bitmap) -> String): String =
        Gson().toJson(toPersisted(f, saveBitmap), PersistedSigmaFolder::class.java)

    fun decodeFromString(s: String, loadBitmap: (String) -> android.graphics.Bitmap?): SigmaFolder {
        var result: SigmaFolder? = null
        try {
            val p = Gson().fromJson(s, PersistedSigmaFolder::class.java)
            result = fromPersisted(p, loadBitmap)
        }catch (e: Exception) {
            println("erreur scale, fichier:$s, ${e.message}")
        }

        return result ?: SigmaFolder.DUMMY
    }
}