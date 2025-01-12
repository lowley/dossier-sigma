package lorry.folder.items.dossiersigma.domain.usecases.clipboard

import android.content.Context
import android.content.Intent
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.domain.interfaces.IClipboardRepository
import lorry.folder.items.dossiersigma.ui.SigmaViewModel
import javax.inject.Inject

class AccessingToInternetSiteForPictureService @Inject constructor(
    val context: Context,
    val clipboardRepository: IClipboardRepository){
    
    fun openBrowser(item: Item, viewModel: SigmaViewModel) {
        val preparedKey = item.name.split('.').last().split(' ').joinToString("+")
        viewModel.setSelectedItem(item)
        viewModel.setBrowserPersonSearch(preparedKey)
        viewModel.showBrowser()
    
    //        val url = SigmaApplication.INTERNET_SITE_SEARCH+preparedKey
//
//        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.setPackage("com.android.chrome");
//        startActivity(context, intent, null);
    }
    
    fun startListeningToClipboard(id: String){
        val serviceIntent = Intent(context, ClipboardService::class.java).apply { 
            putExtra("item_id", id)
        }
        context.startForegroundService(serviceIntent)
    }
    
}