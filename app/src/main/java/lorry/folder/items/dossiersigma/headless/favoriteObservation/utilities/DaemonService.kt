package lorry.folder.items.dossiersigma.headless.favoriteObservation.utilities

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.ui.settings.SettingsManager
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity
import javax.inject.Inject

private const val CHANNEL_ID = "daemon"

@AndroidEntryPoint
class DaemonService : LifecycleService() {

    @Inject
    lateinit var settingsManager: SettingsManager

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    private var index: Int = 0

    override fun onCreate() {
        super.onCreate()
        applicationContext.ensureDaemonChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        scope.launch(Dispatchers.IO) {
            settingsManager.saveIsFileObserverEnabled(true)
        }

        // 1) Démarrer au premier plan rapidement
        startForeground(30215, buildOngoingNotification())


        // 2) Démarrer ta boucle “daemon”
        scope.launch {
            runDaemonLoop()
        }

        // Conseil : START_STICKY pour relance automatique après kill système
        return START_STICKY
    }

    private suspend fun runDaemonLoop() {
        // Exemple : boucle d’écoute
        while (isActive) {
            //* … ton travail de fond (IO, sync, watch, etc.)
            // Mettre à jour la notif si utile :
            // updateNotification("Progression: 42%")
            updateNotification("index: $index")
            index++

            delay(2_000)
        }
    }

    private fun buildOngoingNotification(): Notification {
        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, SigmaActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.settings)
            .setContentTitle("Service actif")
            .setContentText("Surveillance en cours…")
            .setContentIntent(openApp)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.settings)
            .setContentTitle("Service actif")
            .setContentText(text)
            .setOngoing(true)
            .build()
        nm.notify(30215, notif)
    }

    override fun onDestroy() {
        super.onDestroy()

        scope.launch(Dispatchers.IO) {
            settingsManager.saveIsFileObserverEnabled(false)
            job.cancel() // Arrête proprement les coroutines
            stopForeground(STOP_FOREGROUND_DETACH) // détache la notif (ou STOP_FOREGROUND_REMOVE pour la retirer)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }
}

fun Context.ensureDaemonChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val mgr = getSystemService(NotificationManager::class.java)
        val ch = NotificationChannel(
            CHANNEL_ID, "Service en cours", NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Activité de fond en cours" }
        mgr.createNotificationChannel(ch)
    }
}

fun Context.startDaemon() {
    val i = Intent(this, DaemonService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(i)
    else startService(i)
}

fun Context.stopDaemon() {
    stopService(Intent(this, DaemonService::class.java))
}

fun Context.isDaemonNotifVisible(id: Int = 30215, channelId: String = "daemon"): Boolean {
    val nm = getSystemService(NotificationManager::class.java)
    // API 23+ : notifications de TON app uniquement (pas besoin de permission spéciale)
    return nm.activeNotifications.any { it.id == id && it.notification.channelId == channelId }
}