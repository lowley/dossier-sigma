package lorry.folder.items.dossiersigma.ui.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

// On déclare la classe comme un Singleton pour n'avoir qu'une seule instance dans toute l'app
@Singleton
class SettingsManager @Inject constructor(@ApplicationContext private val context: Context) {

    // Crée une instance de DataStore liée à un fichier "settings.preferences_pb"
    // Le nom est arbitraire mais doit être unique.
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    // On définit les clés pour chaque valeur que l'on veut stocker.
    // C'est une bonne pratique de les déclarer comme des objets compagnons.
    companion object {
        val NAS_ADDRESS_KEY = stringPreferencesKey("nas_address")
        val NAS_LOGIN_KEY = stringPreferencesKey("nas_login")
        val NAS_PASSWORD_KEY = stringPreferencesKey("nas_password")
        val NAS_FOLDER_KEY = stringPreferencesKey("nas_folder")
    }

    suspend fun saveNasAddress(address: String) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { settings ->
                settings[NAS_ADDRESS_KEY] = address
            }
        }
    }

    val nasAddressFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            // On lit la valeur associée à notre clé.
            // Si elle n'existe pas, on retourne une valeur par défaut (chaîne vide).
            preferences[NAS_ADDRESS_KEY] ?: ""
        }

    suspend fun saveNasLogin(login: String) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { settings ->
                settings[NAS_LOGIN_KEY] = login
            }
        }
    }

    val nasLoginFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[NAS_LOGIN_KEY] ?: ""
        }

    suspend fun saveNasPassword(password: String) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { settings ->
                settings[NAS_PASSWORD_KEY] = password
            }
        }
    }

    val nasPasswordFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            // On lit la valeur associée à notre clé.
            // Si elle n'existe pas, on retourne une valeur par défaut (chaîne vide).
            preferences[NAS_PASSWORD_KEY] ?: ""
        }

    suspend fun saveNasFolder(folder: String) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { settings ->
                settings[NAS_FOLDER_KEY] = folder
            }
        }
    }

    val nasFolderFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            // On lit la valeur associée à notre clé.
            // Si elle n'existe pas, on retourne une valeur par défaut (chaîne vide).
            preferences[NAS_FOLDER_KEY] ?: ""
        }




}