package lorry.folder.items.dossiersigma.exposure.disk

interface ITempFileDataSource {
    suspend fun saveUrlToTempFile(fileUrl: String): String?
}