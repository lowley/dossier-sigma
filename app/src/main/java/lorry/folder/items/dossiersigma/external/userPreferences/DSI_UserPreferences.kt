package lorry.folder.items.dossiersigma.external.userPreferences

import kotlinx.coroutines.flow.Flow

interface DSI_UserPreferences {

    val input_folder: Flow<String>
    val storage_folder: Flow<String>
    val destination_folders: Flow<Set<String>>

    suspend fun get_storage_folder(): String
    suspend fun get_destination_folders(): Set<String>


    suspend fun save_input_folder(value: String)
    suspend fun save_storage_folder(value: String)
    suspend fun save_destination_folders(values: Set<String>)
    suspend fun add_destination_folder(value: String)
}