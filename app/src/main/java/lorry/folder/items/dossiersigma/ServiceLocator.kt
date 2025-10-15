package lorry.folder.items.dossiersigma

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import lorry.folder.items.dossiersigma.external.base64.Base64DataSource
import lorry.folder.items.dossiersigma.external.disk.DiskDataSource
import lorry.folder.items.dossiersigma.external.disk.DiskRepository
import lorry.folder.items.dossiersigma.external.intent.DS_IntentWrapper
import lorry.folder.items.dossiersigma.external.nas.DSI_FTP
import lorry.folder.items.dossiersigma.external.nas.DS_FTP
import lorry.folder.items.dossiersigma.external.userPreferences.DS_UserPreferences
import lorry.folder.items.dossiersigma.headless.moveToNasWorker.utilities.MoveEngine
import lorry.folder.items.dossiersigma.headless.serviceVariants.moveToNAS.NasUtilities
import lorry.folder.items.dossiersigma.headless.shortcuts.ShortcutUseCase

object ServiceLocator {

    // --- DS_FTP (singleton dépendant de Settings) ----------------------------
    @Volatile private var dsFtpSingleton: DSI_FTP? = null
    fun dsFtp(ctx: Context): DSI_FTP =
        dsFtpSingleton ?: synchronized(this) {
            dsFtpSingleton ?: DS_FTP(ctx).also { dsFtpSingleton = it }
        }

//    fun dataStore(ctx: Context): DataStore<Preferences> = PreferenceDataStoreFactory.create {
//        ctx.preferencesDataStoreFile("settings")
//    }


    // --- NasUtilities (singleton) -------------------------------------------
    @Volatile private var nasUtilitiesSingleton: NasUtilities? = null
    fun nasUtilities(ctx: Context): NasUtilities =
        nasUtilitiesSingleton ?: synchronized(this) {
            nasUtilitiesSingleton ?: NasUtilities(dsFtp(ctx)).also { nasUtilitiesSingleton = it }
        }
}
