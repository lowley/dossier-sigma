package lorry.folder.items.dossiersigma.domain.usecases.clipboard

import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import lorry.folder.items.dossiersigma.GlobalStateManager
import lorry.folder.items.dossiersigma.domain.interfaces.IClipboardRepository
import lorry.folder.items.dossiersigma.ui.MainActivity
import javax.inject.Inject
import lorry.folder.items.dossiersigma.R

@AndroidEntryPoint
class ClipboardService : Service() {
    @Inject
    lateinit var clipboardRepository: IClipboardRepository

    @Inject
    lateinit var globalStateManager: GlobalStateManager

    var itemId: String? = null

    override fun onCreate() {
        super.onCreate()
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        clipboardManager.addPrimaryClipChangedListener {
            val clipData = clipboardManager.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val item = clipData.getItemAt(0)
                handleClipboardData(item)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        itemId = intent?.getStringExtra("item_id")

        val notification = NotificationCompat.Builder(this, "clipboard_channel")
            .setContentTitle("Clipboard Monitoring")
            .setContentText("Service actif pour surveiller le presse-papier")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Remplacez par une icône valide
            .build()

        // Démarrer le service en avant-plan
        startForeground(1, notification)

        return START_STICKY // Permet de redémarrer le service si le système l'arrête
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun handleClipboardData(data: ClipData.Item) {
        if (globalStateManager.selectedItem.value != null)
            globalStateManager.setSelectedItem(
                globalStateManager.selectedItem.value!!.copy(picture = data.uri)
            )

//        if (clipboardRepository.hasImageInClipboard(this)) {
//            reopenApp()
//        }
    }

    private fun reopenApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
    }
}
