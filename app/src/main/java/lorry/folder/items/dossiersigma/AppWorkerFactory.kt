package lorry.folder.items.dossiersigma

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import lorry.folder.items.dossiersigma.headless.moveToNasWorker.MoveToNASWorker
import lorry.folder.items.dossiersigma.headless.moveToNasWorker.utilities.MoveEngine

class AppWorkerFactory(
    private val moveEngine: MoveEngine
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        params: WorkerParameters
    ): ListenableWorker? = when (workerClassName) {
        MoveToNASWorker::class.java.name ->
            MoveToNASWorker(appContext, params, moveEngine)
        else -> null
    }
}