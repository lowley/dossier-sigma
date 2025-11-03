package lorry.folder.items.dossiersigma.ui.dialogs

import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.external.base64.VideoInfoEmbedder
import lorry.folder.items.dossiersigma.basics.domain.SigmaPath
import lorry.folder.items.dossiersigma.basics.domain.str
import lorry.folder.items.dossiersigma.basics.domain.toSigmaPath
import lorry.folder.items.dossiersigma.headless.usecases.homePage.HomeUiState
import lorry.folder.items.dossiersigma.ui.browser.changeState
import lorry.folder.items.dossiersigma.ui.browser.utilities.BrowserTarget
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity
import lorry.folder.items.dossiersigma.ui.sigma.SigmaViewModel

@Composable
fun SigmaActivity.HomeItemDialog(
    message: String,
    homeItemInfos: StateFlow<HomeItemInfos?>,
    onDatasCompleted: (homeItemInfos: HomeItemInfos?) -> Unit,
    viewModel: SigmaViewModel,
) {
    var editText1 by remember { mutableStateOf(homeItemInfos.value?.oldTitle ?: "") }
    var editPath1 by remember { mutableStateOf(homeItemInfos.value?.path ?: "") }
    var editPicture1 by remember { mutableStateOf(homeItemInfos.value?.picture) }
    val homeInfos by homeItemInfos.collectAsState()
    val focusRequester = remember { FocusRequester() }

    Log.d(SigmaActivity.Companion.TAG, "HomeItemDialog: $homeInfos")
    Box(
        modifier = Modifier.Companion
            .width(600.dp)
            .background(
                color = contentColorFor(Color.Companion.White)
                    .copy(alpha = 0.6f)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    viewModel.setIsHomeItemDialogVisible(false)
                }
            ),
        contentAlignment = Alignment.Companion.Center
    ) {
        Column(
            modifier = Modifier.Companion
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                .background(Color.Companion.White)
                .padding(8.dp),
        ) {

            Text(
                modifier = Modifier.Companion,
                text = message,
                color = Color.Companion.Black
            )

            Spacer(modifier = Modifier.Companion.height(8.dp))

            TextField(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                value = if (homeInfos!!.newTitle == null) homeInfos!!.oldTitle!! else homeInfos!!.newTitle!!,
                onValueChange = { value: String ->
                    sigmaActivity.homeViewModel.setDialogHomeItemInfos(
                        sigmaActivity.homeViewModel.dialogHomeItemInfos.value?.copy(
                            newTitle = value
                        )
                    )
                },
                singleLine = true,
                label = { Text("Titre") }
            )

            Spacer(modifier = Modifier.Companion.height(8.dp))

            Row(
                modifier = Modifier.Companion
                    .fillMaxWidth()
            ) {
                TextField(
                    modifier = Modifier.Companion
                        .weight(1f)
                        .padding(end = 5.dp),
                    value = homeInfos!!.path!!.str,
                    onValueChange = { value: String ->
                        sigmaActivity.homeViewModel.setDialogHomeItemInfos(
                            sigmaActivity.homeViewModel.dialogHomeItemInfos.value?.copy(
                                path = value.toSigmaPath()
                            )
                        )
                    },
                    singleLine = true,
                    label = { Text("Chemin") }
                )

                Button(
                    onClick = {
                        sigmaActivity.onFolderChosen = { path ->
                            if (path != null) {
                                sigmaActivity.homeViewModel.setDialogHomeItemInfos(
                                    sigmaActivity.homeViewModel.dialogHomeItemInfos.value?.copy(
                                        path = path
                                    )
                                )
                            }
                        }

                        mainViewModel.setIsFilePickerVisible(true)
                    }) {
                    Text("Choisir")
                }
            }

            Spacer(modifier = Modifier.Companion.height(8.dp))

            AsyncImage(
                modifier = Modifier.Companion
                    .size(100.dp)
                    .padding(10.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .border(
                        1.dp,
                        Color.Companion.Black,
                        androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    )
                    .pointerInput(true) {
                        detectTapGestures(
                            onTap = {
                                /**
                                 * @see lorry.folder.items.dossiersigma.ui.browser.BrowserOverlay
                                 * le Browser est un composable dans MainActivity
                                 * voir BrowserOverlay et son appel par MainActivity
                                 * le callback est un de ses paramètres d'appel
                                 */
                                sigmaActivity.browser.changeState(
                                    isOpen = true,
                                    item = mainViewModel.selectedItem.value,
                                    target = BrowserTarget.GOOGLE,
                                    onImageClicked = { url ->
                                        mainViewModel.viewModelScope.launch {
                                            val bitmap =
                                                mainViewModel.changingPictureUseCase.urlToBitmap(url)
                                                    ?: return@launch
                                            withContext(Dispatchers.Main) {
                                                mainViewModel.setIsHomeItemDialogVisible(true)
                                                sigmaActivity.homeViewModel.setDialogHomeItemInfos(
                                                    sigmaActivity.homeViewModel.dialogHomeItemInfos.value?.copy(
                                                        picture = bitmap
                                                    )
                                                )
                                            }
                                        }
                                    }
                                )

//                                sigmaActivity.onGotBrowserImage = { url ->
//                                    mainViewModel.viewModelScope.launch {
//                                        val bitmap =
//                                            mainViewModel.changingPictureUseCase.urlToBitmap(url)
//                                                ?: return@launch
//                                        withContext(Dispatchers.Main) {
//                                            mainViewModel.setIsHomeItemDialogVisible(true)
//                                            sigmaActivity.homeViewModel.setDialogHomeItemInfos(
//                                                sigmaActivity.homeViewModel.dialogHomeItemInfos.value?.copy(
//                                                    picture = bitmap
//                                                )
//                                            )
//                                        }
//                                    }
//                                }

                                mainViewModel.setIsHomeItemDialogVisible(false)
//                                mainViewModel.browserManager.openBrowserWithText("")
                            }
                        )
                    },
                model = homeInfos!!.picture,
                contentDescription = "Miniature",
                contentScale = ContentScale.Companion.Fit,
            )

            Spacer(modifier = Modifier.Companion.height(8.dp))

            Row(
                modifier = Modifier.Companion
            ) {
                Spacer(
                    modifier = Modifier.Companion
                        .weight(1f)
                )

                Button(
                    modifier = Modifier.Companion,
                    onClick = {
                        viewModel.setIsHomeItemDialogVisible(false)
                    }
                ) {
                    Text("Annuler")
                }

                Button(
                    modifier = Modifier.Companion
                        .padding(start = 5.dp),
                    onClick = {
                        if (homeInfos!!.newTitle != null && homeInfos!!.path != null) {
                            val newHomeItem = HomeItemInfos(
                                oldTitle = homeItemInfos.value?.oldTitle,
                                newTitle = homeInfos!!.newTitle,
                                path = homeInfos!!.path,
                                picture = homeInfos!!.picture,
                                index = homeItemInfos.value?.index
                                    ?: (sigmaActivity.homeViewModel.uiState as? HomeUiState.Ready)
                                        ?.items?.size ?: 0

                            )

                            mainViewModel.viewModelScope.launch {
                                onDatasCompleted(newHomeItem)

                                val existingHomeItems = (sigmaActivity.homeViewModel.uiState
                                    .value as? HomeUiState.Ready)
                                    ?.items

                                if (existingHomeItems.isNullOrEmpty())
                                    return@launch

                                val newHomeItems = existingHomeItems.toMutableList()
                                    .map {
                                        if (it.title == homeInfos!!.newTitle) homeInfos!! else HomeItemInfos(
                                            oldTitle = it.title,
                                            newTitle = it.title,
                                            path = it.path,
                                            picture = it.picture,
                                            index = homeItemInfos.value?.index
                                                ?: (sigmaActivity.homeViewModel.uiState
                                                    .value as? HomeUiState.Ready)
                                                    ?.items?.size ?: 0
                                        )
                                    }.toSet()

                                sigmaActivity.settingsViewModel.settings.saveHomeItems(
                                    newHomeItems
                                )
                            }

                            viewModel.setIsHomeItemDialogVisible(false)
                        } else
                            Toast.makeText(
                                sigmaActivity,
                                "Veuillez renseigner au moins le titre et le chemin du raccourci",
                                Toast.LENGTH_LONG
                            ).show()
                    }
                ) {
                    Text("Valider")
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        // après composition → demande le focus
        focusRequester.requestFocus()
    }
}

data class HomeItemInfos(
    val oldTitle: String? = null,
    val newTitle: String? = null,
    val path: SigmaPath?,
    val picture: Bitmap?,
    val index: Int
) {
    suspend fun toHomeItemInfosDTO(): HomeItemInfosDTO {
        val videoEmbedder = VideoInfoEmbedder()
        return HomeItemInfosDTO(
            oldTitle = oldTitle,
            newTitle = newTitle,
            path = path,
            picture = if (picture != null) videoEmbedder.bitmapToBase64(picture)
            else null,
            index = index
        )
    }
}

data class HomeItemInfosDTO(
    val oldTitle: String? = null,
    val newTitle: String? = null,
    val path: SigmaPath?,
    val picture: String?,
    val index: Int
) {
    suspend fun toHomeItemInfos(): HomeItemInfos {
        val videoEmbedder = VideoInfoEmbedder()
        return HomeItemInfos(
            oldTitle = oldTitle,
            newTitle = newTitle,
            path = path,
            picture = if (picture != null) videoEmbedder.base64ToBitmap(picture)
            else null,
            index = index
        )
    }
}