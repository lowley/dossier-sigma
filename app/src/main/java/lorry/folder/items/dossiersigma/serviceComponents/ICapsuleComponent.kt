package lorry.folder.items.dossiersigma.serviceComponents

import lorry.folder.items.dossiersigma.serviceComponents.utilities.CapsuleData
import lorry.folder.items.dossiersigma.serviceComponents.utilities.IElementInCapsule
import lorry.folder.items.dossiersigma.serviceComponents.utilities.IElementReader

interface ICapsuleComponent {
    suspend fun save(
        element: IElementInCapsule,
        targetPath: String,
        useOld: Boolean = false)

    suspend fun getCapsule(
        targetPath: String,
        useOld: Boolean = false
    ): CapsuleData?

    /**
     * lecture à chaque fois de l'info dans le fichier/dossier
     */
    suspend fun <T> getElement(
        reader: IElementReader<T>,
        targetPath: String): T?



}