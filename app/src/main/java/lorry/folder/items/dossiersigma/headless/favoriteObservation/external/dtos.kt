package lorry.folder.items.dossiersigma.headless.favoriteObservation.external

import lorry.folder.items.dossiersigma.basics.domain.SigmaPath

// Exemple d’un miroir minimal, à adapter à tes champs réels :
@kotlinx.serialization.Serializable
data class PersistedSigmaFolder(
    val path: SigmaPath,
    val items: List<PersistedItem>,
    val meta: Map<String, String>? = null
)

@kotlinx.serialization.Serializable
data class PersistedItem(
    val name: String,
    val modificationDate: Long,
    val tagId: String?,
    val picture: PictureStore?,
    val isFolder: Boolean,
    val fullPath: SigmaPath,
    val memo: String? = null,
    val scale: String?
)

// On évite de stocker un Bitmap en DB : on le met en cache fichier et on stocke le chemin.
@kotlinx.serialization.Serializable
data class PictureStore(
    val kind: Kind,
    val resId: Int? = null,           // si c’est un Int (resource id)
    val filePath: String? = null      // si c’est un Bitmap : chemin du .webp/.png dans ton cache
) {
    @kotlinx.serialization.Serializable
    enum class Kind { NONE, RES_ID, BITMAP_FILE }
}
