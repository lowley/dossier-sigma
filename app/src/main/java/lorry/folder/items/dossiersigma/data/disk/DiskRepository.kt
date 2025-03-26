package lorry.folder.items.dossiersigma.data.disk

import android.graphics.Bitmap
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.data.base64.Base64DataSource
import lorry.folder.items.dossiersigma.data.base64.IBase64DataSource
import lorry.folder.items.dossiersigma.data.bento.BentoRepository
import lorry.folder.items.dossiersigma.data.interfaces.IDiskDataSource
import lorry.folder.items.dossiersigma.domain.SigmaFolder
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.domain.SigmaFile
import lorry.folder.items.dossiersigma.domain.interfaces.IDiskRepository
import lorry.folder.items.dossiersigma.domain.interfaces.IFfmpegRepository
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

class DiskRepository @Inject constructor(
    val datasource: IDiskDataSource,
    val base64DataSource: IBase64DataSource
) : IDiskRepository {

    override suspend fun getInitialFolder(): SigmaFolder {
        return SigmaFolder(
            fullPath = "C:/Users/olivier/Desktop",
            items = List<Item>(80, init = { SigmaFile("/", "fichier ${it}", null) }),
            picture = null
        )
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend override fun getFolderItems(folderPath: String): List<Item> {
        return withContext(Dispatchers.IO) {
            val initialItems = withContext(Dispatchers.IO) {
                datasource.getFolderContent(folderPath)
                    .filter { itemDTO -> 
                        !itemDTO.name.startsWith(".") &&
                        itemDTO.name != "System Volume Information" &&
                        itemDTO.name != "Android"}
                    .map { itemDTO ->
                    async {
                        if (itemDTO.isFile) {
                            var file: Item = SigmaFile(
                                path = itemDTO.path,
                                name = itemDTO.name,
                                picture = null
                            )

                            if (itemDTO.name.endsWith(".html")) {
                                try {
                                    val picture =
                                        base64DataSource.extractImageFromHtml("${itemDTO.path}/${itemDTO.name}")
                                    if (picture != null)
                                        file = file.copy(picture = picture)
                                } catch (e: Exception) {
                                    println("Erreur lors de la lecture de cover : ${e.message}")
                                }
                            }
                            
                            file
                        } else {
                            SigmaFolder(
                                path = itemDTO.path,
                                name = itemDTO.name,
                                picture = null,
                                items = listOf()
                            )
                        }
                    }
                }.awaitAll()
            }

            return@withContext initialItems.sortedBy { item -> item.isFile() }
        }
    }

    override suspend fun saveUrlToTempFile(fileUrl: String): String? {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(fileUrl)
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val inputStream: InputStream? = response.body?.byteStream()
                if (inputStream != null) {
                    val tempFile = File.createTempFile("image", ".jpg")
                    FileOutputStream(tempFile).use { outputStream ->
                        inputStream.use { input ->
                            input.copyTo(outputStream)
                        }
                    }
                    tempFile.absolutePath // Retourne le chemin du fichier temporaire
                } else {
                    null
                }
            } else {
                println("Erreur lors du téléchargement de l'image : ${response.message}")
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}