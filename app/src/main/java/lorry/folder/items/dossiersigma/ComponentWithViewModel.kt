package lorry.folder.items.dossiersigma

open class ComponentWithViewModel<VM : Any> {
    private var _vm: VM? = null

    fun attach(vm: VM) { _vm = vm }          // à appeler une fois
    val viewModel: VM
        get() = _vm ?: error("ViewModel ${this::class.java.name} non initialisé")
}