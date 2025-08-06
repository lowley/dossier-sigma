package lorry.folder.items.dossiersigma.headless.service.utilities

object CoreExecutionRegistry {
    private val contents = mutableMapOf<String, suspend () -> Unit>()

    fun register(id: String, content: suspend () -> Unit) {
        contents[id] = content
    }

    fun consume(id: String): (suspend () -> Unit)? = contents.remove(id)
}