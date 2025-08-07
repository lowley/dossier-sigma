package lorry.folder.items.dossiersigma.headless.service.utilities

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class NucleusService : Service(), INotificationScope {

    var parameters: Map<String, ParameterDelegate<*>> = emptyMap()

    companion object{
        val channelId: String = UUID.randomUUID().toString()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val id = intent?.getStringExtra("execution_id") ?: return START_NOT_STICKY
        val coreContent = CoreExecutionRegistry.consume(id)

        var result: Int? = null
        if (coreContent != null) {
            CoroutineScope(Dispatchers.Default).launch {
                result = (this@NucleusService as INotificationScope).coreContent()
                stopSelf(startId)
            }
        }

        return result ?: START_STICKY
    }


    fun injectParameters(params: Map<String, ParameterDelegate<*>>, values: Map<String, String>) {
        params.forEach { (name, delegate) ->
            val value = values[name] ?: return@forEach
            delegate.assignFromString(value)
        }
    }

    override fun showNotificationById(notificationId: Int) {

        val notification = NotificationRegistry.get(notificationId) ?: return


        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(notification.title)
            .setContentText("${notification.timestamp.toHHMMSS()} - ${notification.text}")
            .setSmallIcon(notification.smallIconRes)
            .setOngoing(notification.isOngoing)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        notification.progress?.let { (max, current) ->
            builder.setProgress(max, current, false)
        }

//        data.onClick?.let {
//            builder.setContentIntent(it)
//        }

//        data.actions.forEach {
//            builder.addAction(it.iconRes, it.title, it.intent)
//        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notification.notificationId, builder.build())
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}

fun  androidx.datastore.preferences.protobuf.Timestamp.toHHMMSS(): String {
    return formatDuration(this.seconds)
}

fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return "%02d:%02d:%02d".format(hours, minutes, secs)
}

