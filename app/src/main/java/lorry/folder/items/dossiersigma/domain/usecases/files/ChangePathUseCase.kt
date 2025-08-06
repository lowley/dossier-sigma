package lorry.folder.items.dossiersigma.domain.usecases.files

import lorry.folder.items.dossiersigma.exposure.disk.IDiskRepository
import javax.inject.Inject

class ChangePathUseCase @Inject constructor(
    val diskRepo : IDiskRepository
) {

    //callback
    fun setNewFolder(newValue: String) {

        
    }

    fun askInputFolder() {
        diskRepo.askInputFolder()
        //le callback contient le reste du traitement
    }
    
    
    
}