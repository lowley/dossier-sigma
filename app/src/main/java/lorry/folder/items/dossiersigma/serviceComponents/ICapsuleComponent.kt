package lorry.folder.items.dossiersigma.serviceComponents

import lorry.folder.items.dossiersigma.data.dataSaver.CompositeData
import lorry.folder.items.dossiersigma.data.dataSaver.IElementInComposite
import lorry.folder.items.dossiersigma.data.dataSaver.IElementReader

interface ICapsuleComponent {
    suspend fun save(element: IElementInComposite)
    suspend fun getComposite(): CompositeData?

    /**
     * lecture à chaque fois de l'info dans le fichier/dossier
     */
    suspend fun <T> getElement(reader: IElementReader<T>): T?



}