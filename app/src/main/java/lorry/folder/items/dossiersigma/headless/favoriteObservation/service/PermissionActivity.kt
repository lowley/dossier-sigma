package lorry.folder.items.dossiersigma.headless.favoriteObservation.service

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class PermissionActivity : AppCompatActivity() {
    private val reqCode = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Optionnel : small UI or just a spinner

        do {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    reqCode
                )
            } else {
                finish()
            }
        } while (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        results: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        // Ici tu peux notifier le Service via un Broadcast / LiveData / bound service / WorkManager
        val granted = results.isNotEmpty() && results[0] == PackageManager.PERMISSION_GRANTED
        val intent = Intent("com.example.NOTIF_PERMISSION_RESULT")
        intent.putExtra("granted", granted)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        finish()
    }
}
