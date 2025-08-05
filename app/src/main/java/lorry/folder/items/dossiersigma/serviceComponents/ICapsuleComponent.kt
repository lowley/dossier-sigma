package lorry.folder.items.dossiersigma.serviceComponents

import lorry.folder.items.dossiersigma.serviceComponents.utilities.CapsuleData
import lorry.folder.items.dossiersigma.serviceComponents.utilities.IElementInComposite
import lorry.folder.items.dossiersigma.serviceComponents.utilities.IElementReader

interface ICapsuleComponent {
    suspend fun save(element: IElementInComposite)
    suspend fun getComposite(): CapsuleData?

    /**
     * lecture à chaque fois de l'info dans le fichier/dossier
     */
    suspend fun <T> getElement(reader: IElementReader<T>): T?



}