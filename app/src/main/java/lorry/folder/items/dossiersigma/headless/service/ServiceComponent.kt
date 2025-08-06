package lorry.folder.items.dossiersigma.headless.service

import android.app.Service
import lorry.folder.items.dossiersigma.headless.service.utilities.ParameterDelegate
import lorry.folder.items.dossiersigma.headless.service.utilities.SigmaNotification

/** USAGE
 *
 * `val sourceDelegate = parameter<Int>()`
 * `val targetDelegate = parameter<String>()`
 * `val debugDelegate = parameter<Boolean>()`
 *
 * `val sourceMap = mapOf(
 *     "source" to sourceDelegate,
 *     "target" to targetDelegate,
 *     "debug" to debugDelegate
 * )`
 */
class ServiceComponent(
    override val delegates: MutableMap<String, ParameterDelegate<*>>,
    override val notificationInfos: List<SigmaNotification>?,
    override val coreContent: suspend () -> Unit,
) : IServiceComponent {

    var service: Service? = null




}

/**
 * USAGE:
 * val values = mapOf(
 *     "source" to "42",
 *     "target" to "destination",
 *     "debug" to "true"
 * )
 *
 * injectParameters(sourceMap, values)
 */

fun injectParameters(
    paramMap: Map<String, ParameterDelegate<*>>,
    values: Map<String, String>
) {
    paramMap.forEach { (name, delegate) ->
        values[name]?.let { rawValue ->
            delegate.assignFromString(rawValue)
        }
    }
}
