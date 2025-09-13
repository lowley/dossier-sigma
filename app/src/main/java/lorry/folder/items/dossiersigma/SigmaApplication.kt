package lorry.folder.items.dossiersigma

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.work.Configuration
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import lorry.folder.items.dossiersigma.external.capsule.utilities.AppContextProvider

@HiltAndroidApp
class SigmaApplication() : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        instance = this

        val channel = NotificationChannel(
            "clipboard_channel",
            "Clipboard Monitoring",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = ContextCompat.getSystemService(this, NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }

    @Inject
    lateinit var workerFactory: androidx.hilt.work.HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    companion object {
        lateinit var instance: SigmaApplication
            private set

        val APPLICATION_NAME = "SigmaApplication"

        fun getContext(): Context{
            return EntryPointAccessors.fromApplication(
                instance, AppContextProvider::class.java
            ).getContext()
        }
    }
}