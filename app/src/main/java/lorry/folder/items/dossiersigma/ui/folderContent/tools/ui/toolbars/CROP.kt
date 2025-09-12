package lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.toolbars

import android.net.Uri
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewModelScope
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.external.base64.VideoInfoEmbedder
import lorry.folder.items.dossiersigma.external.capsule.CapsuleComponent
import lorry.folder.items.dossiersigma.external.capsule.utilities.CroppedPicture
import lorry.folder.items.dossiersigma.external.capsule.utilities.InitialPicture
import lorry.folder.items.dossiersigma.external.capsule.utilities.Scale
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.BottomToolContent
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.Tool
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.Tools
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.changeCrop
import lorry.folder.items.dossiersigma.ui.items.utils.imageAsAnyToTempUri
import lorry.folder.items.dossiersigma.ui.sigma.SigmaViewModel
import java.io.File

object CROP : Tools() {
    override fun content(viewModel: SigmaViewModel?) = BottomToolContent(
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

        if (item.isFile() &&
            item.fullPath.endsWith(".mp4") ||
            item.fullPath.endsWith(".avi") ||
            item.fullPath.endsWith(".mpg") ||
            item.fullPath.endsWith(".html") ||
            item.fullPath.endsWith(".iso") ||
            item.fullPath.endsWith(".mkv")
        ) {
            viewModel.viewModelScope.launch {
                val capsuleMgr = CapsuleComponent()
                capsuleMgr.save(
                    Scale(scale),
                    item.fullPath
                )
            }
        }

        if (item.isFolder()) {
            viewModel.viewModelScope.launch {
                val file = File(item.fullPath + "/.folderPicture.html")
                if (!file.exists())
                    viewModel.diskRepository.createFolderHtmlFile(item)

                val capsuleMgr = CapsuleComponent()
                capsuleMgr.save(
                    Scale(scale),
                    item.fullPath
                )
//            viewModel.refreshCurrentFolder()
            }
        }

//    viewModel.notifyPictureUpdated()
//    viewModel.setSelectedItem(null)
//    BottomTools.setCurrentContent(DEFAULT)
    }
}
