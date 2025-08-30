package lorry.folder.items.dossiersigma.external.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

// Top-level, unique dans le projet
val Context.settingsDataStore by androidx.datastore.preferences.preferencesDataStore(
    name = "settings"
)

object SettingsStoreProvider {
    @Volatile private var instance: DataStore<Preferences>? = null

    fun get(appContext: Context): DataStore<Preferences> =
        instance ?: synchronized(this) {
            instance ?: appContext.settingsDataStore.also { instance = it }
        }
}