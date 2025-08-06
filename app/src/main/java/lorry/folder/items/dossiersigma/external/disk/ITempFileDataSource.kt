package lorry.folder.items.dossiersigma.external.disk

interface ITempFileDataSource {
    suspend fun saveUrlToTempFile(fileUrl: String): String?
}