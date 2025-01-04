package lorry.folder.items.dossiersigma.domain

class Folder(val id: Int){
    var content: MutableList<Item> = mutableListOf()
    var path: String = String()

}