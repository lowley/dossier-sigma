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
        filesToTransfer: List<Pair<String, String?>>,
        nasDirectory: String,
        changeBottomTools: (progress: Int, index: Int, total: Int) -> Unit,
        manifestUri: String,
    ) {
        val filesToTransferDataDelegate = parameter<List<Pair<String, String?>>>()
        val filesToTransferData by filesToTransferDataDelegate

        val nasDirectoryDataDelegate = parameter<String>()
        val nasDirectoryData by nasDirectoryDataDelegate

        val manifestUriDelegate = parameter<String>()
        val manifestUriData by manifestUriDelegate

        val delegates = mapOf(
            "filesToTransferData" to filesToTransferDataDelegate,
            "nasDirectoryData" to nasDirectoryDataDelegate,
            "manifestUriData" to manifestUriDelegate
        )

        service.startService(
            params = delegates,
            values = mapOf(
                "filesToTransferData" to Gson().toJson(filesToTransfer),
                "nasDirectoryData" to nasDirectory,
                "manifestUriData" to manifestUri
            ),
            notificationInfos = listOf(),
            context = context
        ){

            //////////////////
            // core content //
            //////////////////
            val filesToTransferData = delegates["filesToTransferData"]?.value as? List<Pair<String, String?>>
            val nasDirectoryData = delegates["nasDirectoryData"]?.value as? String
            val manifestUriData = delegates["manifestUriData"]?.value as? String

            Log.d(TAG, "MoveToNASService: filesToTransferData: ${filesToTransferData?.size} éléments")
            Log.d(TAG, "MoveToNASService: premier: ${filesToTransferData?.get(0)?.first?.takeLast(20)}, " +
                    "dernier: ${if (filesToTransferData?.get(filesToTransferData.size - 1)?.second == null) "null" else filesToTransferData?.get(filesToTransferData.size - 1)?.second?.take(20)}")

            if (filesToTransferData == null || nasDirectoryData == null || manifestUriData == null)
                START_NOT_STICKY

            val destination = "/$nasDirectory"

            scope.launch {
                Log.d(TAG, "MoveToNASService: dans launch")
                filesToTransferData!!.forEachIndexed { index, source ->
                    println("MoveToNASService: copie de $source")
                    try {
                        nasUtilities.copy(
                            source.first,
                            destination,
                            index = index,
                            total = filesToTransferData.size,
                            changeBottomTools = changeBottomTools
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    println("vérification: source=$source, destination=$destination")
                    val verify = nasUtilities.verify(source.first, destination)
                    println("résultat de la vérification: $verify")

                    if (verify) {
                        println("vérification positive, traitements sur le point d'être effectués")
                        nasUtilities.delete(source.first)
                        println("fichier $source supprimé")

                        println("envoi du message à CopieurTho2")
                        sendMessageToThoApp(
                            context,
                            source.first,
                            manifestUri = manifestUriData!!)
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