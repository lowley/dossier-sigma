package lorry.folder.items.dossiersigma.domain.usecases.clipboard

import android.content.Context
import android.content.Intent
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.domain.interfaces.IClipboardRepository
import lorry.folder.items.dossiersigma.ui.SigmaViewModel
import javax.inject.Inject

class AccessingToInternetSiteForPictureUseCase @Inject constructor(
    val context: Context,
    val clipboardRepository: IClipboardRepository){
    
    fun openBrowser(item: Item, viewModel: SigmaViewModel) {
        val preparedKey = item.name.split('.').last().split(' ').joinToString("+")
        viewModel.setSelectedItem(item)
        viewModel.setBrowserPersonSearch(preparedKey)
        viewModel.showBrowser()
    }
}