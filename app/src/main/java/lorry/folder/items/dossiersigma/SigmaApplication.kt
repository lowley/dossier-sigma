package lorry.folder.items.dossiersigma

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.StrictMode
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.DelegatingWorkerFactory
import androidx.work.WorkManager
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import lorry.folder.items.dossiersigma.ServiceLocator.dsFtp
import lorry.folder.items.dossiersigma.ServiceLocator.nasUtilities
import lorry.folder.items.dossiersigma.external.base64.Base64DataSource
import javax.inject.Inject
import lorry.folder.items.dossiersigma.external.capsule.utilities.AppContextProvider
import lorry.folder.items.dossiersigma.external.disk.DiskDataSource
import lorry.folder.items.dossiersigma.external.disk.DiskRepository
import lorry.folder.items.dossiersigma.external.intent.DS_IntentWrapper
import lorry.folder.items.dossiersigma.external.userPreferences.DS_UserPreferences
import lorry.folder.items.dossiersigma.headless.moveToNasWorker.utilities.MoveEngine
import lorry.folder.items.dossiersigma.headless.shortcuts.ShortcutUseCase

@HiltAndroidApp
class SigmaApplication() : Application(), Configuration.Provider {

    @Inject lateinit var hiltWorkerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        instance = this

        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build()
        )

        val channel = NotificationChannel(
            "clipboard_channel",
            "Clipboard Monitoring",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager =
            ContextCompat.getSystemService(this, NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }

    override val workManagerConfiguration: Configuration
        get() {
            android.util.Log.d("SigmaApp", "Providing WM config with AppWorkerFactory")

            return Configuration.Builder()
                .setWorkerFactory(hiltWorkerFactory) // ou directement hiltWorkerFactory si tu n’as plus besoin de la tienne
                .build()
        }

    companion object {
        lateinit var instance: SigmaApplication
            private set

        val APPLICATION_NAME = "SigmaApplication"

        fun getContext(): Context {
            return EntryPointAccessors.fromApplication(
                instance, AppContextProvider::class.java
            ).getContext()
        }
    }
}