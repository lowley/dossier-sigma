package lorry.folder.items.dossiersigma.serviceComponents.utilities

interface ICapsuleIO {
    suspend fun getCapsule(filePath: String): CapsuleData?
    suspend fun replaceCapsule(filePath: String, capsule: CapsuleData?
    ): Boolean
}