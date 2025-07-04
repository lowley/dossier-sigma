package lorry.folder.items.dossiersigma.data.dataSaver

import android.content.Context
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.SigmaApplication
import java.io.File
import javax.inject.Inject


class FileCompositeManager @Inject constructor(
    private val targetPath: String,
) {

    fun save(element: IElementInComposite) {

        val appContext = SigmaApplication.getContext().applicationContext
        val injector = EntryPointAccessors.fromApplication(appContext, MyInjectors::class.java)
        val compositeIO = injector.provideFileCompositeIO()

        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch(Dispatchers.IO) {
            val target = File(targetPath)
            val existingComposite = if (target.exists()) {
                val existingComposite = compositeIO.getComposite(targetPath)
                existingComposite ?: CompositeData()
            } else {
                CompositeData()
            }

            val updatedComposite = element.update(existingComposite)
            compositeIO.saveComposite(targetPath, updatedComposite)
        }
    }

    suspend fun get(): CompositeData {
        val appContext = SigmaApplication.getContext().applicationContext
        val injector = EntryPointAccessors.fromApplication(appContext, MyInjectors::class.java)
        val compositeIO = injector.provideFileCompositeIO()

        val target = File(targetPath)
        return withContext(Dispatchers.IO) {
            if (target.exists()) {
                compositeIO.getComposite(targetPath) ?: CompositeData()
            } else {
                CompositeData()
            }
        }
    }
}