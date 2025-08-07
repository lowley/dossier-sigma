package lorry.folder.items.dossiersigma.headless.service.utilities

object CoreExecutionRegistry {
    private val contents = mutableMapOf<String, CoreContent>()

    fun register(id: String, content: CoreContent) {
        contents[id] = content
    }

    fun consume(id: String): CoreContent? = contents.remove(id)
}

object NotificationRegistry{

    private val notifications = mutableMapOf<Int, SigmaNotification>()

    fun register(id: Int, notification: SigmaNotification) {
        notifications[id] = notification
    }

    fun get(id: Int): SigmaNotification? = notifications[id]
}