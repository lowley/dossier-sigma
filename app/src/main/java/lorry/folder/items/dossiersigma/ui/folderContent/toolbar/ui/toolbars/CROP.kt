package lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.toolbars

import android.net.Uri
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewModelScope
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.external.base64.VideoInfoEmbedder
import lorry.folder.items.dossiersigma.external.capsule.CapsuleComponent
import lorry.folder.items.dossiersigma.external.capsule.utilities.CroppedPicture
import lorry.folder.items.dossiersigma.external.capsule.utilities.InitialPicture
import lorry.folder.items.dossiersigma.external.capsule.utilities.Scale
import lorry.folder.items.dossiersigma.headless.domain.str
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.ToolbarContent
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.Tool
import lorry.folder.items.dossiersigma.ui.folderContent.items.utils.imageAsAnyToTempUri
import lorry.folder.items.dossiersigma.ui.sigma.SigmaViewModel
import java.io.File

object CROP : Tools() {
    override fun content() = ToolbarContent(
        toolInit = listOf(
            Tool(
                text = { "Aucun" },
                icon = R.drawable.crop,
                onClick = { viewModel, mainActivity ->
                    changeCrop(viewModel, ContentScale.Companion.None)
                }
            ),

            Tool(
                text = { "Rogner" },
                icon = R.drawable.crop,
                onClick = { viewModel, mainActivity ->
                    changeCrop(viewModel, ContentScale.Companion.Crop)
                }
            ),

            Tool(
                text = { "Remplir ⇅" },
                icon = R.drawable.crop,
                onClick = { viewModel, mainActivity ->
                    changeCrop(viewModel, ContentScale.Companion.FillHeight)
                }
            ),

            Tool(
                text = { "Remplir ⇿" },
                icon = R.drawable.crop,
                onClick = { viewModel, mainActivity ->
                    changeCrop(viewModel, ContentScale.Companion.FillWidth)
                }
            ),

            Tool(
                text = { "Etirer" },
                icon = R.drawable.crop,
                onClick = { viewModel, mainActivity ->
                    changeCrop(viewModel, ContentScale.Companion.Fit)
                }
            ),

            Tool(
                text = { "Dedans" },
                icon = R.drawable.crop,
                onClick = { viewModel, mainActivity ->
                    changeCrop(viewModel, ContentScale.Companion.Inside)
                }
            ),

            Tool(
                text = { "Manuel" },
                icon = R.drawable.image,
                isColoredIcon = true,
                onClick = { viewModel, mainActivity ->
                    run {
                        val item = viewModel.selectedItem.value
                        var sourceBitmap: Any? = null

                        if (item == null)
                            return@run

                        val capsuleMgr = CapsuleComponent()
                        sourceBitmap = capsuleMgr.getElement(
                            InitialPicture.Companion,
                            item.fullPath
                        )
                        val test = capsuleMgr.getElement(
                            CroppedPicture.Companion,
                            item.fullPath
                        )

                        if (sourceBitmap == null && test != null) {
                            capsuleMgr.save(
                                InitialPicture(test, VideoInfoEmbedder()),
                                item.fullPath
                            )
                            sourceBitmap = test
                        }

                        if (sourceBitmap == null)
                            return@run

                        val sourceUri = imageAsAnyToTempUri(mainActivity, sourceBitmap)
                        val destinationUri =
                            Uri.fromFile(
                                File.createTempFile(
                                    "cropped_", ".jpg",
                                    mainActivity.cacheDir
                                )
                            )

                        //le callback est dans MainActivity : onActivityResult (override)
                        UCrop.of(sourceUri, destinationUri)
                            .withAspectRatio(1f, 1f)
                            .withMaxResultSize(175, 175)
                            .start(mainActivity)
                    }
                }
            ),
        ),
        "CROP"
    )

    fun changeCrop(viewModel: SigmaViewModel, scale: ContentScale) {
        val item = viewModel.selectedItem.value ?: return
        viewModel.setSelectedItem(item.copy(scale = scale))

        if (item.isFile()
//            &&
//            item.fullPath.endsWith(".mp4") ||
//            item.fullPath.endsWith(".avi") ||
//            item.fullPath.endsWith(".mpg") ||
//            item.fullPath.endsWith(".html") ||
//            item.fullPath.endsWith(".iso") ||
//            item.fullPath.endsWith(".mkv")
        ) {
            viewModel.viewModelScope.launch {
                val capsuleMgr = CapsuleComponent()
                capsuleMgr.save(
                    Scale(scale),
                    item.fullPath
                )

                withContext(Dispatchers.Main) {
                    viewModel.folderContentComponent.reloadCurrentFolder()
                    viewModel.setSelectedItem(null)
                    toolBarManager.toolbarComponent.toolsViewModel.rawFeed.setCurrentContent(DEFAULT)
                }
            }
        }

        if (item.isFolder()) {
            viewModel.viewModelScope.launch {
                val file = File(item.fullPath.str + "/.folderPicture.html")
//                if (!file.exists())
//                    viewModel.diskRepository.createFolderHtmlFile(item)

                val capsuleMgr = CapsuleComponent()
                capsuleMgr.save(
                    Scale(scale),
                    item.fullPath
                )

                withContext(Dispatchers.Main){
                    viewModel.folderContentComponent.reloadCurrentFolder()
                    viewModel.setSelectedItem(null)
                    toolBarManager.toolbarComponent.toolsViewModel.rawFeed.setCurrentContent(DEFAULT)
                }
            }
        }

//    viewModel.notifyPictureUpdated()
//    viewModel.setSelectedItem(null)
    }
}
