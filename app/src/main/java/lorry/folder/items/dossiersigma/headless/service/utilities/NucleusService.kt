package lorry.folder.items.dossiersigma.headless.service.utilities

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NucleusService : Service() {

    var parameters: Map<String, ParameterDelegate<*>> = emptyMap()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val id = intent?.getStringExtra("execution_id") ?: return START_NOT_STICKY
        val job = CoreExecutionRegistry.consume(id)

        if (job != null) {
            CoroutineScope(Dispatchers.Default).launch {
                job()
                stopSelf(startId)
            }
        }

        return START_STICKY
    }


    fun injectParameters(params: Map<String, ParameterDelegate<*>>, values: Map<String, String>) {
        params.forEach { (name, delegate) ->
            val value = values[name] ?: return@forEach
            delegate.assignFromString(value)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}