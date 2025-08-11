package lorry.folder.items.dossiersigma.headless.serviceVariants.moveToNAS

interface IMoveToNASComponent {

    fun startService(
        filesToTransfer: List<Pair<String, String?>>,
        nasDirectory: String,
        changeBottomTools: (progress: Int, index: Int, total: Int) -> Unit,
    )
}