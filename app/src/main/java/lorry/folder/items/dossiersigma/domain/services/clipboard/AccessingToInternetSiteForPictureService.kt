package lorry.folder.items.dossiersigma.domain.services.clipboard

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import lorry.folder.items.dossiersigma.SigmaApplication
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.domain.interfaces.IClipboardRepository
import lorry.folder.items.dossiersigma.ui.components.ItemComponent
import javax.inject.Inject

class AccessingToInternetSiteForPictureService @Inject constructor(
    val context: Context){
    
    fun openBrowser(item: Item) {
        val preparedKey = item.name.split('.').last().split(' ').joinToString("+")
        val url = SigmaApplication.INTERNET_SITE_SEARCH+preparedKey

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("com.android.chrome");
        startActivity(context, intent, null);
        
        
    }
}