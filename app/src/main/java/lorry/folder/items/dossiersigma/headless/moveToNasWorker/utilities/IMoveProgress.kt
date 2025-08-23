package lorry.folder.items.dossiersigma.headless.moveToNasWorker.utilities

interface IMoveProgress {

    suspend fun onStart(total: Int) {}
    suspend fun onItemProgress(index: Int, percent: Int) {}
    suspend fun onItemDone(index: Int) {}
}