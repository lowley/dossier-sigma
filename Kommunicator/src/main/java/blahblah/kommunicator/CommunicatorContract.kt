package blahblah.kommunicator

object CommunicatorContract {

    const val ACTION = "kommunicator.ACTION_COMMUNICATOR"
    const val PERMISISON = "kommunicator.SEND_MESSAGE"
    const val EXTRA_PAYLOAD = "payload"

    const val EMITTER__RECEPTION_ACKNOWLEDGMENT = "reception_acknowledgement"
    const val EMITTER__PROCESSING_FILE = "processing_file"
    const val EMITTER__PROCESSED_FILE = "processed_file"

}

sealed class Messageτ(val tubeContent: String) {

    object START_PROCEEDING : Messageτ("Start proceeding")

}


data class IncomingMessage(
    val text: String,
    val index: Int = 0,
    val total: Int = 0,
    val fileName: String = ""
)
