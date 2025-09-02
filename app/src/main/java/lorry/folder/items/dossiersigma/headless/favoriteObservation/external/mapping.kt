package lorry.folder.items.dossiersigma.headless.favoriteObservation.external

import android.graphics.Bitmap
import com.google.gson.Gson
import lorry.folder.items.dossiersigma.headless.domain.ColoredTag
import lorry.folder.items.dossiersigma.headless.domain.SigmaFile
import lorry.folder.items.dossiersigma.headless.domain.SigmaFolder

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
                    fullPath = item.fullPath
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
            // Reconstruis ton Item “métier” (sans changer sa classe)
            if (pi.isFolder)
                SigmaFolder(
//                    name = pi.name,
                    modificationDate = pi.modificationDate,
                    tag = pi.tagId?.let { Gson().fromJson(it, ColoredTag::class.java) },
                    picture = any,
                    fullPath = pi.fullPath,
                    scale = null,
                    items = emptyList()
                )
            else
                SigmaFile(
//                    name = pi.name,
                    modificationDate = pi.modificationDate,
                    tag = pi.tagId?.let { Gson().fromJson(it, ColoredTag::class.java) },
                    picture = any,
                    fullPath = pi.fullPath,
                    scale = null
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
        val p = Gson().fromJson(s, PersistedSigmaFolder::class.java)
        return fromPersisted(p, loadBitmap)
    }
}