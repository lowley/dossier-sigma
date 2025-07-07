package lorry.folder.items.dossiersigma.data.dataSaver

interface ICompositeIO {
    suspend fun getComposite(filePath: String): CompositeData?
    suspend fun replaceComposite(filePath: String, composite: CompositeData?
    ): Boolean

}