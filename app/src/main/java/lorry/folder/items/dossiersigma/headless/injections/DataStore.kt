package lorry.folder.items.dossiersigma.headless.injections

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// Top-level, unique dans le projet
val Context.settingsDataStore by preferencesDataStore(
    name = "settings"
)

object SettingsStoreProvider {
    @Volatile private var instance: DataStore<Preferences>? = null

    fun get(appContext: Context): DataStore<Preferences> =
        instance ?: synchronized(this) {
            instance ?: appContext.settingsDataStore.also { instance = it }
        }
}