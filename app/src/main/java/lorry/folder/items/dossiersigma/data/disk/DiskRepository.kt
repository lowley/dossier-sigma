package lorry.folder.items.dossiersigma.data.disk

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.util.Base64
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import com.google.gson.Gson
import com.pointlessapps.rt_editor.utils.RichTextValueSnapshot
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.data.base64.IBase64DataSource
import lorry.folder.items.dossiersigma.data.intent.DSI_IntentWrapper
import lorry.folder.items.dossiersigma.data.interfaces.IDiskDataSource
import lorry.folder.items.dossiersigma.domain.ColoredTag
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.domain.SigmaFile
import lorry.folder.items.dossiersigma.domain.SigmaFolder
import lorry.folder.items.dossiersigma.domain.interfaces.IDiskRepository
import lorry.folder.items.dossiersigma.ui.ITEMS_ORDERING_STRATEGY
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class DiskRepository @Inject constructor(
    val datasource: IDiskDataSource,
    val base64DataSource: IBase64DataSource,
    val intentWrapper: DSI_IntentWrapper
) : IDiskRepository {

    @OptIn(DelicateCoroutinesApi::class)
    suspend override fun getFolderItems(
        folderPath: String,
        sorting: ITEMS_ORDERING_STRATEGY
    ): List<Item> {
        return withContext(Dispatchers.IO) {
            val initialItems = withContext(Dispatchers.IO) {
                datasource.getFolderContent(folderPath)
                    .filter { itemDTO ->
                        !itemDTO.name.startsWith(".") &&
                                itemDTO.name != "System Volume Information" &&
                                itemDTO.name != "Android"
                    }
                    .map { itemDTO ->
                        async {
                            if (itemDTO.isFile) {
                                var file: Item = SigmaFile(
                                    path = itemDTO.path,
                                    name = itemDTO.name,
                                    picture = null,
                                    modificationDate = itemDTO.lastModified,
                                    tag = null,
                                    scale = ContentScale.Crop,
                                    memo = RichTextValueSnapshot(),
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

                                if (itemDTO.name.endsWith(".mp4")) {
                                    try {
                                        val picture =
                                            extractCoverBitmap("${itemDTO.path}/${itemDTO.name}")
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
                                    items = listOf<Item>(),
                                    modificationDate = itemDTO.lastModified,
                                    tag = null,
                                    scale = ContentScale.Crop,
                                    memo = RichTextValueSnapshot(),
                                    
                                )
                            }
                        }
                    }.awaitAll()
            }

            val sorted = when (sorting) {
                ITEMS_ORDERING_STRATEGY.NAME_ASC -> initialItems.sortedWith(
                    compareBy<Item> { it.isFile() }
                        .thenBy { it.name.toLowerCase(locale = Locale.current) })


                ITEMS_ORDERING_STRATEGY.DATE_DESC -> initialItems.sortedWith(
                    compareBy<Item> { it.isFile() }
                        .thenByDescending { it.modificationDate })
            }

            return@withContext sorted
        }
    }

    fun extractCoverBitmap(videoPath: String): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoPath)
            val cover = retriever.embeddedPicture
            retriever.release()
            cover?.let { data ->
                BitmapFactory.decodeByteArray(data, 0, data.size)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
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

    override suspend fun getSigmaFolder(
        folderPath: String,
        sorting: ITEMS_ORDERING_STRATEGY
    ): SigmaFolder {

        val folder = File(folderPath)
        val lastModified = withContext(Dispatchers.IO) { folder.lastModified() }

        return SigmaFolder(
            fullPath = folderPath,
            picture = null,
            items = getFolderItems(folderPath, sorting),
            modificationDate = folder.lastModified(),
            tag = null,
            scale = ContentScale.Crop,
            memo = RichTextValueSnapshot()
        )
    }

    override suspend fun saveFolderPictureToHtmlFile(item: Item, onlyCropped: Boolean) {
        if (item.picture == null || item.isFile())
            return

        if (!onlyCropped) {
            //picture contient un bitmap
            val destFullPath = "${item.fullPath}/.folderPicture.html"
            createShortcut(text(item), destFullPath)
        }

        val destCroppedFullPath = "${item.fullPath}/.folderPictureCropped.html"
        createShortcut(text(item), destCroppedFullPath)
    }

    override suspend fun createFolderHtmlFile(folderItem: Item) {
        if (folderItem.isFile())
            return

        //picture contient un bitmap
        val destFullPath = "${folderItem.fullPath}/.folderPicture.html"
        createShortcut(baseText(folderItem), destFullPath)
    }

    override suspend fun insertPictureToHtmlFile(
        item: Item,
        picture: String
    ) {
        val htmlFile = File(item.fullPath + "/.folderPicture.html")
        if (!withContext(Dispatchers.IO) { htmlFile.exists() }) 
            return

        insertPictureToHtmlFile(htmlFile, item)
    }

    private suspend fun insertPictureToHtmlFile(htmlFile: File, item: Item) {
        val htmlContent = withContext(Dispatchers.IO) { htmlFile.readText() }

        val base64Cover: String? = (item.picture as Bitmap?)?.let {
            val outputStream = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val imageBytes = outputStream.toByteArray()
            Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        }

        val imageSection = base64Cover?.let {
            """<img src="data:image/jpeg;base64,$it" alt="cover" style="max-width:100%;height:auto;"/><br>"""
        } ?: ""

        val newHtmlContent = replaceOrInsert(
            htmlContent,
            """<img\s+[^>]*src\s*=\s*"data:image/[^;]+;base64,[^"]+"""", imageSection
        )

        htmlFile.delete()
        withContext(Dispatchers.IO) {
            val fichier = File(item.fullPath + "/.folderPicture.html")
            fichier.writeText(newHtmlContent, Charsets.UTF_8)
        }
    }

    fun replaceOrInsert(html: String, patternToFind: String, replacement: String): String {
        val regex = Regex(patternToFind)
        return if (regex.containsMatchIn(html)) {
            regex.replace(html, replacement)
        } else {
            html.replace("</body>", "$replacement</body>")
        }
    }

    override suspend fun insertScaleToHtmlFile(
        item: Item,
        scale: ContentScale
    ) {
        val htmlFile = File(item.fullPath + "/.folderPicture.html")
        if (!withContext(Dispatchers.IO) { htmlFile.exists() })
            return

        val htmlContent = withContext(Dispatchers.IO) { htmlFile.readText() }

        val scaleToInsert = when(scale){
            ContentScale.Crop -> "Crop"
            ContentScale.Fit -> "Fit"
            ContentScale.None -> "None"
            ContentScale.Inside -> "Inside"
            ContentScale.FillWidth -> "FillWidth"
            ContentScale.FillHeight -> "FillHeight"
            ContentScale.FillBounds -> "FillBounds"
            else -> "Crop"
        }
        
        val imageSection = """<div class="contentScale">$scaleToInsert</div>""""

        val newHtmlContent = replaceOrInsert(htmlContent, """<div class="contentScale" >[^"]+</div>"""
            .trimMargin(), imageSection)

        htmlFile.delete()
        withContext(Dispatchers.IO) {
            val fichier = File(item.fullPath + "/.folderPicture.html")
            fichier.writeText(newHtmlContent, Charsets.UTF_8)
        }
    }

    override suspend fun extractScaleFromHtml(folderPath: String): ContentScale? {
        val htmlFile = File("$folderPath/.folderPicture.html")
        if (!withContext(Dispatchers.IO) { htmlFile.exists() }) return null

        val htmlContent = withContext(Dispatchers.IO) { htmlFile.readText() }

        // Regex pour trouver le contenu de src="data:image/...;base64,..."
        val regex = Regex("""<div class="contentScale">([^"]+)</div>"""")
        val match = regex.find(htmlContent) ?: return null

        val scale = match.groupValues[1]
        return contentScaleFromString(scale)
    }

    override suspend fun removeScaleFromHtml(htmlFileFullPath: String) {
        val htmlFile = File(htmlFileFullPath+"/.folderPicture.html")
        if (!withContext(Dispatchers.IO) { htmlFile.exists() }) 
            return

        val htmlContent = withContext(Dispatchers.IO) { htmlFile.readText() }

        // Regex pour trouver le contenu de src="data:image/...;base64,..."
        val regex = Regex("""<div class="contentScale">([^"]+)</div>"""")
        val correctedText = htmlContent.replace(regex, "")

        htmlFile.delete()
        withContext(Dispatchers.IO) {
            val fichier = File(htmlFileFullPath + "/.folderPicture.html")
            fichier.writeText(correctedText, Charsets.UTF_8)
        }
    }

    fun contentScaleFromString(value: String): ContentScale = when (value) {
        "Crop" -> ContentScale.Crop
        "Fit" -> ContentScale.Fit
        "FillBounds" -> ContentScale.FillBounds
        "FillHeight" -> ContentScale.FillHeight
        "FillWidth" -> ContentScale.FillWidth
        "Inside" -> ContentScale.Inside
        "None" -> ContentScale.None
        else -> ContentScale.Crop // ou exception
    }

    override suspend fun createShortcut(text: String, fullPathAndName: String) {
        withContext(Dispatchers.IO) {
            val fichier = File(fullPathAndName)
            fichier.writeText(text, Charsets.UTF_8)
        }
    }

    override suspend fun hasPictureFile(item: Item): Boolean {
        if (item.isFile())
            return false

        val folderPictureFile = File("${item.fullPath}/.folderPicture.html")
        val folderPictureCroppedFile = File("${item.fullPath}/.folderPictureCropped.html")
        
        val result = withContext(Dispatchers.IO) {
            folderPictureFile.exists() || folderPictureCroppedFile.exists()
        }

        return result
    }

    fun text(item: Item): String {
        //assert
        val base64Cover: String? = (item.picture as Bitmap?)?.let {
            val outputStream = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val imageBytes = outputStream.toByteArray()
            Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        }

        val imageSection = base64Cover?.let {
            """<img src="data:image/jpeg;base64,$it" alt="cover" style="max-width:100%;height:auto;"/><br>"""
        } ?: ""

        val text = """<!DOCTYPE html>
                                 <html lang="fr">
                                 <head>
                                     <meta charset="UTF-8">
                                     <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                     <title>Image container</title>
                                 </head>
                                 <body>
                                    $imageSection
                                 </body>
                                 </html>"""

        return text
    }

    fun baseText(item: Item): String {
        val text = """<!DOCTYPE html>
                                 <html lang="fr">
                                 <head>
                                     <meta charset="UTF-8">
                                     <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                     <title>Image container</title>
                                 </head>
                                 <body>
                                   
                                 </body>
                                 </html>"""

        return text
    }

    override fun askInputFolder() {
        intentWrapper.do_ACTION_OPEN_DOCUMENT_TREE()
    }

    override suspend fun countFilesAndFolders(folder: File): Pair<Int, Int> {
        if (!folder.isDirectory) return 0 to 0

        val files = folder.listFiles() ?: return 0 to 0
        var fileCount = 0
        var folderCount = 0

        for (f in files) {
            if (
                f.isFile &&
                !f.name.startsWith(".")
            ) fileCount++
            else if (f.isDirectory) folderCount++
        }

        return fileCount to folderCount
    }

    override suspend fun copyFile(source: File, destination: File) {
        withContext(Dispatchers.IO) {
            Files.copy(
                source.toPath(),
                destination.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )
        }
    }
    
    override suspend fun isFileOrFolderExists(parentPath: String, item: Item): 
    Boolean{
        
        val correspondingItem = datasource.getFolderContent(parentPath)
            .firstOrNull { it.isFile == item.isFile()
                    && it.name == item.fullPath.substringAfterLast("/")
            }
        
        return correspondingItem != null
    }

    override suspend fun extractFlagFromHtml(folderPath: String): ColoredTag? {
        val htmlFile = File("$folderPath/.folderPicture.html")

        val exists = try {
             withContext(Dispatchers.IO) { htmlFile.exists() }
        }
        catch(e: Exception){
            println(e.message)
            false
        }
        if (!exists) return null

        val htmlContent = withContext(Dispatchers.IO) { htmlFile.readText() }

        // Regex pour trouver le contenu de src="data:image/...;base64,..."
        val regex = Regex("""<div class="coloredTag">(.*?)</div>"""")
        val match = regex.find(htmlContent)
        
        if (match != null)
            println("match")
        
        if (match == null)
            return null

        val tag = match.groupValues[1]
        return Gson().fromJson(tag, ColoredTag::class.java)
    }

    override suspend fun insertTagToHtmlFile(
        item: Item,
        tag: ColoredTag,
    ) {
        if (item.isFile())
            return
        
        val htmlFile = File(item.fullPath + "/.folderPicture.html")
        if (!withContext(Dispatchers.IO) { htmlFile.exists() })
            return

        val htmlContent = withContext(Dispatchers.IO) { htmlFile.readText() }

        val tagToInsert = Gson().toJson(tag)
        val tagSection = """<div class="coloredTag">$tagToInsert</div>""""

        val newHtmlContent = replaceOrInsert(htmlContent, """<div class="coloredTag" >[^"]+</div>"""
            .trimMargin(), tagSection)

        htmlFile.delete()
        withContext(Dispatchers.IO) {
            val fichier = File(item.fullPath + "/.folderPicture.html")
            fichier.writeText(newHtmlContent, Charsets.UTF_8)
        }
    }

    override suspend fun removeTagFromHtml(folderPath: String) {
        val htmlFile = File(folderPath+"/.folderPicture.html")
        if (!withContext(Dispatchers.IO) { htmlFile.exists() })
            return

        val htmlContent = withContext(Dispatchers.IO) { htmlFile.readText() }

        // Regex pour trouver le contenu de src="data:image/...;base64,..."
        val regex = Regex("""<div class="coloredTag">(.*?)</div>"""")
        val correctedText = htmlContent.replace(regex, "")

        if (htmlContent == correctedText)
            return
        
        htmlFile.delete()
        withContext(Dispatchers.IO) {
            val fichier = File(folderPath + "/.folderPicture.html")
            fichier.writeText(correctedText, Charsets.UTF_8)
        }
    }

    override suspend fun getSize(file: File): Long {
        return withContext(Dispatchers.IO) {
            file.length()
        }
    }

//    override suspend fun insertMemoToFolder(folderPath: String) {
//        
//    }
//
//    override suspend fun extractMemoFromFolder(folderPath: String): RichTextValueSnapshot? {
//        return null
//    }
//
//    override suspend fun removeMemoFromFolder(folderPath: String) {
//        
//    }
}