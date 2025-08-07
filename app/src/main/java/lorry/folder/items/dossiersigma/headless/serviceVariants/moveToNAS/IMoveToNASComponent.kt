package lorry.folder.items.dossiersigma.headless.serviceVariants.moveToNAS

interface IMoveToNASComponent {

    fun startService(
        filesToTransfer: List<String>,
        nasDirectory: String
    )


}