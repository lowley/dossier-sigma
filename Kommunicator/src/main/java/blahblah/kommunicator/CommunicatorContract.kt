package blahblah.kommunicator

object CommunicatorContract {

    const val ACTION = "kommunicator.ACTION_COMMUNICATOR"
    const val PERMISISON = "kommunicator.SEND_MESSAGE"
    const val EXTRA_PAYLOAD = "payload"

    const val EMITTER__RECEPTION_ACKNOWLEDGMENT = "reception_acknowledgement"

}

sealed class Messageτ(val tubeContent: String){

    object START_PROCEEDING: Messageτ("Start proceeding")

}


data class IncomingMessage(val text: String)
