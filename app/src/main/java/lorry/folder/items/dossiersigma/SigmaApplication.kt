package lorry.folder.items.dossiersigma

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.ContextCompat
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import lorry.folder.items.dossiersigma.data.dataSaver.AppContextProvider
import lorry.folder.items.dossiersigma.data.dataSaver.FileCompositeManager

@HiltAndroidApp
class SigmaApplication : Application() {

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

    companion object {
        lateinit var instance: SigmaApplication
            private set

        val APPLICATION_NAME = "SigmaApplication"
        val INTERNET_PERSON_SITE_SEARCH = "https://www.iafd.com/results.asp?searchtype=comprehensive&searchstring="
        val INTERNET_MOVIE_SITE_SEARCH = "https://www.hotmovies.com/adult-movies/search?sort=score&q="

        fun getContext(): Context{
            return EntryPointAccessors.fromApplication(
                SigmaApplication.instance, AppContextProvider::class.java
            ).getContext()
        }
    }
}