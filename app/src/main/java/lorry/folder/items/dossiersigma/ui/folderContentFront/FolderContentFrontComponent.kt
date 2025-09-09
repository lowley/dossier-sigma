package lorry.folder.items.dossiersigma.ui.folderContentFront

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import javax.inject.Inject
import lorry.folder.items.dossiersigma.ui.memo.MemoViewModel
import kotlin.getValue

class FolderContentFrontComponent @Inject constructor(
    private val owner: ViewModelStoreOwner
) : IFolderContentFrontComponent{

    val frontViewModel: FolderContentFrontViewModel by lazy {
        ViewModelProvider(owner)[FolderContentFrontViewModel::class.java]
    }






}