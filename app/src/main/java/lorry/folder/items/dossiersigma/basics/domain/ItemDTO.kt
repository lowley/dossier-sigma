package lorry.folder.items.dossiersigma.basics.domain

data class ItemDTO(
    val path: String,
    val name: String,
    val isFile: Boolean,
    val lastModified: Long,
)