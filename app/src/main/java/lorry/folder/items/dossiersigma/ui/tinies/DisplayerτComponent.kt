package lorry.folder.items.dossiersigma.ui.tinies

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import blahblah.kommunicator.CommunicatorContract
import blahblah.kommunicator.CommunicatorContract.EMITTER__PROCESSED_FILE
import blahblah.kommunicator.CommunicatorContract.EMITTER__PROCESSING_FILE
import blahblah.kommunicator.CommunicatorContract.EMITTER__RECEPTION_ACKNOWLEDGMENT
import blahblah.kommunicator.IncomingMessage
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.withTimeout
import java.text.MessageFormat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DisplayerτComponent @Inject constructor(
    private val appContext: Context
) {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    var testFlow: Flow<IncomingMessage>? = null
    var somethingToDisplay = false

    val realFlow: SharedFlow<IncomingMessage> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val msg = extractMessage(intent) ?: return
                trySend(msg)
            }

            private fun extractMessage(intent: Intent): IncomingMessage? {
                val payload =
                    intent.getStringExtra(CommunicatorContract.EXTRA_PAYLOAD) ?: return null
                val msg =
                    runCatching { Gson().fromJson(payload, IncomingMessage::class.java) }
                        .getOrElse { return null }

                return msg
            }
        }

        // Android 13+ : préciser l’export policy
        val flags = Context.RECEIVER_EXPORTED
        val filter = IntentFilter(CommunicatorContract.ACTION)

        appContext.registerReceiver(
            receiver,
            filter,
            CommunicatorContract.PERMISISON,
            null,
            flags
        )

        //le flux détecte la fin du message et ferme le receiver
        awaitClose { appContext.unregisterReceiver(receiver) }

    }.shareIn(scope = scope, started = SharingStarted.Lazily, replay = 0)

    val statesFlow: Flow<IncomingMessage>
        get() = testFlow ?: realFlow

    companion object {
        const val MESSAGE_TAG = "message"
        const val WAITING_MESSAGE = "En attente ..."
        const val PROCESS_STARTED_MESSAGE = "Traitement en cours ..."
        const val NO_COMMUNICATION_MESSAGE = "Communication non établie"
        const val PROCESSING_FILE_MESSAGE = "Traitement du fichier {0}/{1}"
        const val PROCESSED_FILE_MESSAGE = "Traitement terminé {0}/{1}"
        const val ERROR_FILE_MESSAGE = "{0} en erreur"
    }

    @Composable
    fun MessageDisplayerτ() {

        val state = statesFlow.collectAsState(initial = null)
        var previousState: IncomingMessage? by remember { mutableStateOf(null) }
        var currentState: IncomingMessage? by remember { mutableStateOf(null) }

        LaunchedEffect(state.value) {
            previousState = currentState
            currentState = state.value
        }

        var message by remember { mutableStateOf(WAITING_MESSAGE) }

        LaunchedEffect(currentState) {
            if (previousState == null &&
                currentState?.text == EMITTER__RECEPTION_ACKNOWLEDGMENT
            ) {
                somethingToDisplay = true
                message = PROCESS_STARTED_MESSAGE
                Log.d("tests", "MessageDisplayerτ - attribution à message: $message")
            }

            if (previousState?.text == EMITTER__RECEPTION_ACKNOWLEDGMENT) {
                if (currentState?.text == EMITTER__PROCESSING_FILE) {
                    if (
                        currentState?.index == null ||
                        currentState?.total == null || currentState?.total == 0
                    )
                        message = MessageFormat.format(
                            ERROR_FILE_MESSAGE, currentState?.fileName
                        )
                    else
                        message = MessageFormat.format(
                            PROCESSING_FILE_MESSAGE,
                            currentState?.index,
                            currentState?.total
                        )
                }
            }

            if (previousState?.text == EMITTER__PROCESSING_FILE) {
                if (currentState?.text == EMITTER__PROCESSED_FILE) {
                    message = MessageFormat.format(
                        PROCESSED_FILE_MESSAGE,
                        currentState?.index,
                        currentState?.total
                    )
                }
//                else {
//                    // si on sort par timeout → erreur
//                    message = MessageFormat.format(
//                        ERROR_FILE_MESSAGE, currentState?.fileName
//                            ?.substringAfterLast("/")
//                    )
//                    Log.d("tests", "MessageDisplayerτ - attribution à message: $message")
//                }

                delay(5_000)
                message = WAITING_MESSAGE
                somethingToDisplay = false
            }
        }

        Log.d("tests", "MessageDisplayerτ - message affiché: $message")

        Text(
            text = message,
            modifier = Modifier.testTag(MESSAGE_TAG)
        )
    }
}

fun androidx.compose.runtime.State<IncomingMessage?>.hasText(text: String): Boolean =
    this.value?.text == text

fun androidx.compose.runtime.State<IncomingMessage?>.startsWithText(text: String): Boolean =
    this.value?.text?.startsWith(text) == true

fun androidx.compose.runtime.State<IncomingMessage?>.toEMITTER__PROCESSING_FILE(): Pair<Int, Int>? =
    this.value?.takeIf { it.text == EMITTER__PROCESSED_FILE }
        ?.let { (it.index ?: 0) to (it.total ?: 0) }


