package lorry.folder.items.dossiersigma.external.userPreferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(name = "preferences_tho")

@Singleton
open class DS_UserPreferences constructor(private val context: Context) : DSI_UserPreferences {

    private val dataStore = context.applicationContext.dataStore

    companion object {
        private val INPUT_FOLDER_KEY = stringPreferencesKey("input_folder")
        private val STORAGE_FOLDER_KEY = stringPreferencesKey("storage_folder")
        private val DESTINATION_FOLDERS_KEY = stringSetPreferencesKey("destination_folders")
    }

    override val storage_folder: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[STORAGE_FOLDER_KEY] ?: ""
        }

    override val destination_folders: Flow<Set<String>> = dataStore.data
        .map { preferences ->
            preferences[DESTINATION_FOLDERS_KEY] ?: emptySet()
        }

    override suspend fun get_storage_folder(): String {
        return withContext(Dispatchers.IO) {
            var storageFolder = ""
            storage_folder.collect { item ->
                storageFolder = item
            }
            return@withContext storageFolder
        }
    }

    override suspend fun get_destination_folders(): Set<String> {
        return withContext(Dispatchers.IO) {
            var destinationFolders = emptySet<String>()
            destination_folders.collect { item ->
                destinationFolders = item
            }
            return@withContext destinationFolders
        }
    }

    override val input_folder: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[INPUT_FOLDER_KEY] ?: ""
        }

    override suspend fun save_input_folder(value: String) {
        context.dataStore.edit { preferences ->
            preferences[INPUT_FOLDER_KEY] = value
        }
    }

    override suspend fun save_storage_folder(value: String) {
        context.dataStore.edit { preferences ->
            preferences[STORAGE_FOLDER_KEY] = value
        }
    }

    override suspend fun save_destination_folders(values: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[DESTINATION_FOLDERS_KEY] = values
        }
    }

    override suspend fun add_destination_folder(firstLevel: String) {
        withContext(Dispatchers.IO) {
            var destinationFolders = mutableSetOf("")
            destination_folders.collect { item ->
                destinationFolders = item.toMutableSet()
            }
            destinationFolders.add(firstLevel)
            save_destination_folders(destinationFolders)
        }
    }
}