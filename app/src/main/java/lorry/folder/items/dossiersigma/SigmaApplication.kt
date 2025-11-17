package lorry.folder.items.dossiersigma

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.StrictMode
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import io.github.lowley.engineRoom.submarine.DiveLogging
import javax.inject.Inject
import lorry.folder.items.dossiersigma.external.capsule.utilities.AppContextProvider

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

//        startKoin {
//            // nécessaire pour koin-android
//            androidContext(this@SigmaApplication)
//        }

        DiveLogging.startService()


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