package lorry.folder.items.dossiersigma.headless.service

import android.app.Service
import android.content.Context
import android.content.Intent
import lorry.folder.items.dossiersigma.headless.service.utilities.CoreExecutionRegistry
import lorry.folder.items.dossiersigma.headless.service.utilities.NucleusService
import lorry.folder.items.dossiersigma.headless.service.utilities.ParameterDelegate
import lorry.folder.items.dossiersigma.headless.service.utilities.SigmaNotification
import java.util.UUID

/** USAGE
 *
 * val source by parameter<Int>()
 * val debug by parameter<Boolean>()
 *
 * val delegates = mapOf("source" to source, "debug" to debug)
 *
 * val component = ServiceComponent()
 * component.startService(
 *     delegates = delegates,
 *     values = mapOf("source" to "42", "debug" to "true"),
 *     context = context
 * ){
 *      val s by source
 *      val d by debug
 *      if (d) println("Debug: source=$s")
 *  }
 */
class ServiceComponent() : IServiceComponent {

    var service: Service? = null

    override fun startService(
        delegates: Map<String, ParameterDelegate<*>>,
        values: Map<String, String>,
        notificationInfos: List<SigmaNotification>?,
        context: Context,
        coreContent: suspend () -> Unit,
    ) {
        val id = UUID.randomUUID().toString()

        delegates.forEach { (name, delegate) ->
            values[name]?.let { delegate.assignFromString(it) }
        }

        coreContent?.let {
            CoreExecutionRegistry.register(id, it)
        }

        val intent = Intent(context, NucleusService::class.java).apply {
            putExtra("execution_id", id)
        }

        context.startService(intent)
    }
}


