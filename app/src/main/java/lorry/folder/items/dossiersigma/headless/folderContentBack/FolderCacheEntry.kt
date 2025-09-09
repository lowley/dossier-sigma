package lorry.folder.items.dossiersigma.headless.folderContentBack

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import lorry.folder.items.dossiersigma.headless.domain.SigmaFolder
import lorry.folder.items.dossiersigma.ui.sigma.SortingCriterion

@Entity(
    tableName = "folder_cache_entry",
    indices = [Index(value = ["path"], unique = true)]
)
data class FolderCacheEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val folder: SigmaFolder,
    @ColumnInfo(name = "path", defaultValue = "0") val path: String,
    val sort: SortingCriterion,
    val freshness: FolderFreshness,
    val cachedAt: Long = System.currentTimeMillis()


) {
    override fun toString(): String {
        return "FolderCacheEntry(path=${path.substringAfterLast("/")}, freshness=$freshness)"
    }
}
