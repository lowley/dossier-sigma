package lorry.folder.items.dossiersigma.data.base64

import java.io.File

interface IMp4Base64Embedder {
    suspend fun appendBase64ToMp4(mp4File: File, base64Image: String)
    suspend fun extractBase64FromMp4(mp4File: File, lookbackBytes: Int = 65536): String?
    suspend fun removeEmbeddedBase64(mp4File: File): Boolean
}