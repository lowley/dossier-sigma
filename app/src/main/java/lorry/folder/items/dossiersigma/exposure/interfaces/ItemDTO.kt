package lorry.folder.items.dossiersigma.exposure.interfaces

data class ItemDTO(
    val path: String,
    val name: String,
    val isFile: Boolean,
    val lastModified: Long,
)