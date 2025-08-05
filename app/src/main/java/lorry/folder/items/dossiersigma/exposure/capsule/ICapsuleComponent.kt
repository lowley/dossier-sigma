package lorry.folder.items.dossiersigma.exposure.capsule

import lorry.folder.items.dossiersigma.exposure.capsule.utilities.CapsuleData
import lorry.folder.items.dossiersigma.exposure.capsule.utilities.IElementInCapsule
import lorry.folder.items.dossiersigma.exposure.capsule.utilities.IElementReader

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