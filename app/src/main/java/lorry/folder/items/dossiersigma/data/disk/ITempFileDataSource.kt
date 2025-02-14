package lorry.folder.items.dossiersigma.data.disk

interface ITempFileDataSource {
    suspend fun saveUrlToTempFile(fileUrl: String): String?
}