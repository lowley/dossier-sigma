package lorry.folder.items.dossiersigma.data.dataSaver

import java.io.File

class FileCompositeManager(
    private val targetPath: String,
    private val compositeIO: FileCompositeIO
) {

    fun save(element: IElementInComposite) {
        
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