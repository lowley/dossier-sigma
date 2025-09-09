package lorry.folder.items.dossiersigma.headless.favoriteObservation.external

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.SigmaApplication
import lorry.folder.items.dossiersigma.headless.domain.SigmaFolder
import lorry.folder.items.dossiersigma.headless.folderContent.FolderCacheEntry
import lorry.folder.items.dossiersigma.headless.folderContent.FolderFreshness
import lorry.folder.items.dossiersigma.UI.sigma.SortingCriterion
import java.io.File
import java.time.Instant

@Database(entities = [FolderCacheEntry::class], version = 2, exportSchema = true)
@TypeConverters(SigmaFolderConverters::class, EnumConverters::class, Converters::class, FreshnessConverters::class)
abstract class FolderCacheEntryDB : RoomDatabase() {
    abstract fun folderCacheEntryRepository(): IFolderCacheEntryRepository

    companion object {
        @Volatile
        private var INSTANCE: FolderCacheEntryDB? = null

        fun get(context: Context): FolderCacheEntryDB =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FolderCacheEntryDB::class.java,
                    "favoriteObservation.db"
                )
//                    .addMigrations(MIGRATION_1_2)
                    // .addMigrations(MIGRATION_1_2, ...)   // à utiliser quand le schéma évolue
                    // .fallbackToDestructiveMigration()    // option « reset » si pas de migration
                    //.fallbackToDestructiveMigration(true)
                    .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                    .build().also { INSTANCE = it }
            }
    }

    suspend fun saveFolderCacheEntry(entry: FolderCacheEntry, ctx: Context) {
        val db = get(ctx)
        db.folderCacheEntryRepository().upsert(entry)
    }

    ////////////////////
    // lecture par UI //
    ////////////////////
//    private val dao = AppDb.get(ctx).noteDao()
//
//    // Flow qui réagit dès qu’une note est ajoutée/supprimée/modifiée
//    val notes = dao.getAll()
//        .stateIn(viewModelScope,
//            kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
//            emptyList())

    fun add(entry: FolderCacheEntry, scope: CoroutineScope, ctx: Context) {
        scope.launch {
            val db = get(ctx)
            db.folderCacheEntryRepository().upsert(entry)
        }
    }

    fun remove(entry: FolderCacheEntry, scope: CoroutineScope, ctx: Context) {
        scope.launch {
            val db = get(ctx)
            db.folderCacheEntryRepository().delete(entry)
        }
    }

    suspend fun getByPath(path: String, scope: CoroutineScope, ctx: Context): FolderCacheEntry? {
        val db = FolderCacheEntryDB.get(ctx)
        return db.folderCacheEntryRepository().getByPath(path)
    }
}

class Converters {
    @TypeConverter fun fromInstant(i: Instant): Long = i.toEpochMilli()
    @TypeConverter fun toInstant(ms: Long): Instant = Instant.ofEpochMilli(ms)
}

class EnumConverters {
    @TypeConverter fun toSorting(name: String): SortingCriterion =
        SortingCriterion.valueOf(name)
    @TypeConverter fun fromSorting(s: SortingCriterion): String = s.name
}

class FreshnessConverters {
    @TypeConverter fun toFreshness(text: String): FolderFreshness =
        Gson().fromJson(text, FolderFreshness::class.java)
    @TypeConverter fun fromFreshness(freshness: FolderFreshness): String =
        Gson().toJson(freshness)
}

class SigmaFolderConverters() {

    private val app: Application = SigmaApplication.instance

    @TypeConverter
    fun sigmaFolderToString(value: SigmaFolder): String {
//        if (value == null) return null
        return SigmaFolderMapping.encodeToString(value) { bmp ->
            // Sauvegarde disque (ex. /files/room_bitmaps/)
            val dir = File(app.filesDir, "room_bitmaps").apply { mkdirs() }
            val name = "bmp_${bmp.hashCode()}_${System.nanoTime()}.webp"
            val out = File(dir, name).outputStream()
            out.use {
                bmp.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, it)
            }
            File(dir, name).absolutePath
        }
    }

    @TypeConverter
    fun stringToSigmaFolder(s: String): SigmaFolder {
//        if (s == null) return null
        return SigmaFolderMapping.decodeFromString(s) { path ->
            runCatching { BitmapFactory.decodeFile(path) }.getOrNull()
        }
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1) Colonne path en TEXT, non nulle, valeur par défaut *texte* '0'
        db.execSQL(
            "ALTER TABLE folder_cache_entry " +
                    "ADD COLUMN path TEXT NOT NULL DEFAULT '0'"
        )

        // 2) Index unique sur path (nom et unicité doivent matcher l'entité)
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_folder_cache_entry_path " +
                    "ON folder_cache_entry(path)"
        )
    }
}