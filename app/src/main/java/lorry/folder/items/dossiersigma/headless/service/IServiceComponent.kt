package lorry.folder.items.dossiersigma.headless.service

import lorry.folder.items.dossiersigma.headless.service.utilities.ParameterDelegate
import lorry.folder.items.dossiersigma.headless.service.utilities.SigmaNotification

interface IServiceComponent {

    val delegates: MutableMap<String, ParameterDelegate<*>>
    val notificationInfos: List<SigmaNotification>?
    val coreContent: suspend () -> Unit

}

/*
INTERNE:
val values = mapOf(
    "source" to "42",
    "target" to "destination",
    "debug" to "true"
)


 */

