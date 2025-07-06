package lorry.folder.items.dossiersigma.data.dataSaver

import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.SigmaApplication
import java.io.File
import javax.inject.Inject


class FileCompositeManager @Inject constructor(
    private val targetPath: String,
) {

    suspend fun save(element: IElementInComposite) {

        val appContext = SigmaApplication.getContext().applicationContext
        val injector = EntryPointAccessors.fromApplication(appContext, MyInjectors::class.java)
        val compositeIO = injector.provideFileCompositeIO()

        val target = File(targetPath)
        val existingComposite = if (target.exists()) {
            compositeIO.getComposite(targetPath) ?: CompositeData()
        } else {
            CompositeData()
        }

        val updatedComposite = element.update(existingComposite)
        compositeIO.saveComposite(targetPath, updatedComposite)
    }

    suspend fun getComposite(): CompositeData {
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

    suspend fun <T> getElement(reader: IElementReader<T>): T? {
        return withContext(Dispatchers.IO) {
            reader.fileGet(targetPath)
        }
    }
}