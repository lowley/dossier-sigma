package lorry.folder.items.dossiersigma.domain

class Folder(var path: String, val items: List<Item>){
    
    val isEmpty: Boolean
        get() = items.isEmpty()
    
    
}