package lorry.folder.items.dossiersigma.domain.interfaces

import androidx.compose.ui.layout.ContentScale
import com.pointlessapps.rt_editor.utils.RichTextValueSnapshot
import lorry.folder.items.dossiersigma.domain.ColoredTag
import lorry.folder.items.dossiersigma.domain.SigmaFolder
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.ui.ITEMS_ORDERING_STRATEGY
import java.io.File

interface IDiskRepository {
    
    suspend fun getFolderItems(folderPath: String, sorting: ITEMS_ORDERING_STRATEGY) : List<Item>
    suspend fun saveUrlToTempFile(fileUrl: String) : String?
    suspend fun getSigmaFolder(folderPath: String, sorting: ITEMS_ORDERING_STRATEGY):
            SigmaFolder

    suspend fun createFolderHtmlFile(item: Item)
    suspend fun insertPictureToHtmlFile(item: Item, picture: String)
    suspend fun insertScaleToHtmlFile(item: Item, scale: ContentScale)
    suspend fun extractScaleFromHtml(htmlFile: String): ContentScale? 
    suspend fun saveFolderPictureToHtmlFile(item: Item, onlyCropped: Boolean)
    
    suspend fun createShortcut(text: String, fullPathAndName: String)

    suspend fun hasPictureFile(folder: Item): Boolean
    fun askInputFolder()

    suspend fun countFilesAndFolders(folder: File): Pair<Int, Int>
    suspend fun copyFile(source: File, destination: File)
    suspend fun removeScaleFromHtml(htmlFileFullPath: String)

    suspend fun isFileOrFolderExists(parentPath: String, item: Item): Boolean
    
    suspend fun extractFlagFromHtml(htmlFile: String): ColoredTag?
    suspend fun insertTagToHtmlFile(
        item: Item,
        tag: ColoredTag,
    )
    suspend fun removeTagFromHtml(htmlFileFullPath: String)
    suspend fun getSize(file: File): Long
    
//    suspend fun insertMemoToFolder(folderPath: String)
//    suspend fun extractMemoFromFolder(folderPath: String): RichTextValueSnapshot?
//    suspend fun removeMemoFromFolder(folderPath: String)
}