package lorry.folder.items.dossiersigma.domain

data class Item(
    val id: Int, 
    val text: String, 
    val type: ItemType, 
    val content: Object) {
}

enum class ItemType {
    FOLDER,
    FILE
}
