package lorry.folder.items.dossiersigma.domain

class Folder(var path: String, val items: List<Item>){
    
    val isEmpty: Boolean
        get() = items.isEmpty()
    
    fun copy(
        path: String = this.path,
        items: List<Item> = this.items
    ): Folder{
        return Folder(path = path, items = items.map { it.copy() })
    }
}