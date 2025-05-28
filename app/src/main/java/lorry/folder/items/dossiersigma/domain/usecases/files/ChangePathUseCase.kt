package lorry.folder.items.dossiersigma.domain.usecases.files

import lorry.folder.items.dossiersigma.domain.interfaces.IDiskRepository
import java.net.URLDecoder
import javax.inject.Inject

class ChangePathUseCase @Inject constructor(
    val diskRepo : IDiskRepository
) {

    //callback
    fun setNewFolder(newValue: String) {
//        val valueToSave = "/storage/7376-B000/" + URLDecoder.decode(
//            newValue.substringAfter(
//                "7376-B000", "le dossier doît être sur la carte SD"
//            )
//        ).drop(1)
        
        //GoToFolder
        
        
        
    }

    fun askInputFolder() {
        diskRepo.askInputFolder()
        //le callback contient le reste du traitement
    }
    
    
    
}