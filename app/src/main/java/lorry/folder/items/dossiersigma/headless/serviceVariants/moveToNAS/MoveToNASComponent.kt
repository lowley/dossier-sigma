package lorry.folder.items.dossiersigma.headless.serviceVariants.moveToNAS

import android.app.Service.START_NOT_STICKY
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.headless.service.IServiceComponent
import lorry.folder.items.dossiersigma.headless.service.utilities.parameter
import lorry.folder.items.dossiersigma.headless.services.MoveToNASService.Companion.TAG
import lorry.folder.items.dossiersigma.headless.services.sendMessageToThoApp
import lorry.folder.items.dossiersigma.ui.sigma.SigmaViewModel
import javax.inject.Inject

class MoveToNASComponent @Inject constructor(
    val context: Context,
    val service: IServiceComponent,
    val nasUtilities: NasUtilities,
): IMoveToNASComponent {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun startService(
        filesToTransfer: List<String>,
        nasDirectory: String,
        changeBottomTools: (progress: Int, index: Int, total: Int) -> Unit,
    ) {
        val filesToTransferDataDelegate = parameter<List<String>>()
        val filesToTransferData by filesToTransferDataDelegate

        val nasDirectoryDataDelegate = parameter<String>()
        val nasDirectoryData by nasDirectoryDataDelegate

        val delegates = mapOf(
            "filesToTransferData" to filesToTransferDataDelegate,
            "nasDirectoryData" to nasDirectoryDataDelegate
        )

        service.startService(
            params = delegates,
            values = mapOf(
                "filesToTransferData" to Gson().toJson(filesToTransfer),
                "nasDirectoryData" to nasDirectory
            ),
            notificationInfos = listOf(),
            context = context
        ){

            //////////////////
            // core content //
            //////////////////
            val filesToTransferData = delegates["filesToTransferData"]?.value as? List<String>
            val nasDirectoryData = delegates["nasDirectoryDataDelegate"]?.value as? String

            if (filesToTransferData == null || nasDirectoryData == null)
                START_NOT_STICKY

            val destination = "/$nasDirectory"

            scope.launch {
                Log.d(TAG, "MoveToNASService: dans launch")
                filesToTransferData!!.forEachIndexed { index, source ->
                    println("MoveToNASService: copie de $source")
                    try {
                        nasUtilities.copy(
                            source,
                            destination,
                            index = index,
                            total = filesToTransferData.size,
                            changeBottomTools = changeBottomTools
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    println("vérification: source=$source, destination=$destination")
                    val verify = nasUtilities.verify(source, destination)
                    println("résultat de la vérification: $verify")

                    if (verify) {
                        println("vérification positive, traitements sur le point d'être effectués")
                        nasUtilities.delete(source)
                        println("fichier $source supprimé")

                        println("envoi du message à CopieurTho2")
                        sendMessageToThoApp(context, source)
                        println("message envoyé")
                    }

                    SigmaViewModel.requestRefresh()
                }

                service.stopSelf()
            }

            START_NOT_STICKY
        }
    }
}