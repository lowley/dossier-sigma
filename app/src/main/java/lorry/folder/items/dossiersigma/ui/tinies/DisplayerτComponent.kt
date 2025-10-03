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
import blahblah.kommunicator.CommunicatorContract.EMITTER__RECEPTION_ACKNOWLEDGMENT
import blahblah.kommunicator.IncomingMessage
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DisplayerτComponent @Inject constructor(
    private val appContext: Context,
    testFlow: Flow<IncomingMessage>? = null
) {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
                        .getOrElse { return null}

                return msg
            }
        }

        // Android 13+ : préciser l’export policy
        val flags = Context.RECEIVER_EXPORTED
        val filter = IntentFilter(CommunicatorContract.ACTION)

        appContext.registerReceiver(receiver, filter, flags)

        //le flux détecte la fin du message et ferme le receiver
        awaitClose { appContext.unregisterReceiver(receiver) }

    }.shareIn(scope = scope, started = SharingStarted.Lazily, replay = 0)

    val statesFlow = testFlow ?: realFlow

    companion object {
        const val MESSAGE_TAG = "message"
        const val WAITING_MESSAGE = "En attente ..."
        const val PROCESS_STARTED_MESSAGE = "Traitement en cours ..."
        const val NO_COMMUNICATION_MESSAGE = "Communication non établie"
    }

    @Composable
    fun MessageDisplayerτ() {

        val state = statesFlow.collectAsState(initial = null)
        var message by remember { mutableStateOf(WAITING_MESSAGE) }

//        LaunchedEffect(states.value) {
//            val currentValue = states.value
//
//
//        }

        if (state.value == null) {
            LaunchedEffect(Unit) {
                delay(2_000)
                message = NO_COMMUNICATION_MESSAGE
                Log.d("tests", "MessageDisplayerτ - attribution à message: $message")
            }

            // 2) Détection de sortie anticipée de A : on passe à C
            DisposableEffect(Unit) {
                onDispose {
                    if (message != NO_COMMUNICATION_MESSAGE && state.value?.text ==
                        EMITTER__RECEPTION_ACKNOWLEDGMENT
                    ) {
                        message = PROCESS_STARTED_MESSAGE
                        Log.d("tests", "MessageDisplayerτ - attribution à message: $message")
                    }
                }
            }
        }

        Log.d("tests", "MessageDisplayerτ - message affiché: $message")

        Text(
            text = message,
            modifier = Modifier.testTag(MESSAGE_TAG)
        )
    }
}