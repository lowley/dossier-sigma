package lorry.folder.items.dossiersigma.domain.interfaces

interface IFFMpegRepository {
    
    suspend fun addPictureToMP4Metadata(pictureUrl: String, filePath: String) : Boolean
    
    
}