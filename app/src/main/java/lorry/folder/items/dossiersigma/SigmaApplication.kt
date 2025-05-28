package lorry.folder.items.dossiersigma

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SigmaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            "clipboard_channel",
            "Clipboard Monitoring",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = ContextCompat.getSystemService(this, NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }

    companion object{
        val APPLICATION_NAME = "SigmaApplication"
        val INTERNET_PERSON_SITE_SEARCH = "https://www.iafd.com/results.asp?searchtype=comprehensive&searchstring="
        val INTERNET_MOVIE_SITE_SEARCH = "https://www.hotmovies.com/adult-movies/search?sort=score&q="
        var SDRoot = "/storage/7376-B000/"
    }
}