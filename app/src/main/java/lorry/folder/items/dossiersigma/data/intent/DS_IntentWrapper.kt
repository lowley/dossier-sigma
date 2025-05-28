package lorry.folder.items.dossiersigma.data.intent

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class DS_IntentWrapper @Inject constructor() : DSI_IntentWrapper {

    private var launcher: ActivityResultLauncher<Intent>? = null

    override fun setLauncher(launcher: Object): DSI_IntentWrapper {
        this.launcher = launcher as ActivityResultLauncher<Intent>

        this.launcher = launcher
        return this
    }

    override fun do_ACTION_OPEN_DOCUMENT_TREE(){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)

        try {
            launcher?.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}