package lorry.folder.items.dossiersigma.headless.service

import android.content.Context
import lorry.folder.items.dossiersigma.headless.service.utilities.ParameterDelegate
import lorry.folder.items.dossiersigma.headless.service.utilities.SigmaNotification

interface IServiceComponent {

    fun startService(
        params: Map<String, ParameterDelegate<*>>,
        values: Map<String, String>,
        notificationInfos: List<SigmaNotification>?,
        context: Context,
        coreContent: suspend () -> Unit,
    )
}

/*
INTERNE:
val values = mapOf(
    "source" to "42",
    "target" to "destination",
    "debug" to "true"
)


 */

