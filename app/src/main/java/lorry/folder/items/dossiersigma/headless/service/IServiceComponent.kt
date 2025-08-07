package lorry.folder.items.dossiersigma.headless.service

import android.content.Context
import lorry.folder.items.dossiersigma.headless.service.utilities.CoreContent
import lorry.folder.items.dossiersigma.headless.service.utilities.ParameterDelegate
import lorry.folder.items.dossiersigma.headless.service.utilities.SigmaNotification

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
interface IServiceComponent {

    fun startService(
        params: Map<String, ParameterDelegate<*>>,
        values: Map<String, String>,
        notificationInfos: List<SigmaNotification>?,
        context: Context,
        coreContent: CoreContent,
    )

    fun stopSelf()
}

/*
INTERNE:
val values = mapOf(
    "source" to "42",
    "target" to "destination",
    "debug" to "true"
)


 */

