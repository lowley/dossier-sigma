package lorry.folder.items.dossiersigma.serviceComponents.utilities

interface ICapsuleIO {
    suspend fun getComposite(filePath: String): CapsuleData?
    suspend fun replaceComposite(filePath: String, composite: CapsuleData?
    ): Boolean

}