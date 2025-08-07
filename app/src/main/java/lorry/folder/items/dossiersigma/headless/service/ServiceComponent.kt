package lorry.folder.items.dossiersigma.headless.service

import android.app.Service
import android.content.Context
import android.content.Intent
import lorry.folder.items.dossiersigma.headless.service.utilities.CoreContent
import lorry.folder.items.dossiersigma.headless.service.utilities.CoreExecutionRegistry
import lorry.folder.items.dossiersigma.headless.service.utilities.NotificationRegistry
import lorry.folder.items.dossiersigma.headless.service.utilities.NucleusService
import lorry.folder.items.dossiersigma.headless.service.utilities.ParameterDelegate
import lorry.folder.items.dossiersigma.headless.service.utilities.SigmaNotification
import java.util.UUID
import javax.inject.Inject

/** USAGE
 *
 * ```
 * val source by parameter<Int>()
 * val debug by parameter<Boolean>()
 *
 * val delegates = mapOf("source" to source, "debug" to debug)
 * val component = ServiceComponent()
 *
 * component.startService(
 *     delegates = delegates,
 *     notificationInfos = listOf(
 *          SigmaNotification(1, ...)
 *     )
 *     values = mapOf("source" to "42", "debug" to "true"),
 *     context = context
 * ){
 *      val s by source
 *      val d by debug
 *      if (d) println("Debug: source=$s")
 *      showNotificationById(1, "En cours…")
 * }
 * ```
 */
class ServiceComponent @Inject constructor() : IServiceComponent {

    var service: Service? = null

    override fun startService(
        delegates: Map<String, ParameterDelegate<*>>,
        values: Map<String, String>,
        notificationInfos: List<SigmaNotification>?,
        context: Context,
        coreContent: CoreContent,
    ) {
        val id = UUID.randomUUID().toString()

        delegates.forEach { (name, delegate) ->
            values[name]?.let { delegate.assignFromString(it) }
        }

        CoreExecutionRegistry.register(id, coreContent)
        notificationInfos?.forEach {
            NotificationRegistry.register(it.notificationId, it)
        }

        val intent = Intent(context, NucleusService::class.java).apply {
            putExtra("execution_id", id)
        }

        context.startService(intent)
    }

    override fun stopSelf() {
        service?.stopSelf()
    }
}


