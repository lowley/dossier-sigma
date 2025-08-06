package lorry.folder.items.dossiersigma.headless.domain

data class ItemDTO(
    val path: String,
    val name: String,
    val isFile: Boolean,
    val lastModified: Long,
)