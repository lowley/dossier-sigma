package lorry.folder.items.dossiersigma.headless.favoriteObservation.external

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import lorry.folder.items.dossiersigma.headless.folderContentBack.utils.FolderCacheEntry

@Dao
interface IFolderCacheEntryRepository {
    @Query("SELECT * FROM folder_cache_entry ORDER BY id DESC")
    fun getAll(): Flow<List<FolderCacheEntry>>   // réémet à chaque changement

    @Query("SELECT * FROM folder_cache_entry WHERE path = :path")
    suspend fun getByPath(path: String): FolderCacheEntry?

    @Query("SELECT * FROM folder_cache_entry WHERE path = :path LIMIT 1")
    fun getFlowByPath(path: String): Flow<FolderCacheEntry?>

    @Query("SELECT * FROM folder_cache_entry WHERE path IN (:paths)")
    suspend fun getAllByPaths(paths: List<String>): List<FolderCacheEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<FolderCacheEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: FolderCacheEntry): Long

    @Delete
    suspend fun delete(entry: FolderCacheEntry)
}