package lorry.folder.items.dossiersigma.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import lorry.folder.items.dossiersigma.ui.MainActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSizeDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.viewModelScope
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.PermissionsManager
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.data.dataSaver.CompositeManager
import lorry.folder.items.dossiersigma.data.intent.DSI_IntentWrapper
import lorry.folder.items.dossiersigma.domain.services.MoveFileService
import lorry.folder.items.dossiersigma.domain.usecases.files.ChangePathUseCase
import lorry.folder.items.dossiersigma.domain.usecases.homePage.HomeViewModel
import lorry.folder.items.dossiersigma.ui.components.BottomTools
import lorry.folder.items.dossiersigma.ui.components.Breadcrumb
import lorry.folder.items.dossiersigma.ui.components.BrowserOverlay
import lorry.folder.items.dossiersigma.ui.components.CustomMoveFileExistingDestinationDialog
import lorry.folder.items.dossiersigma.ui.components.CustomTextDialog
import lorry.folder.items.dossiersigma.ui.components.CustomYesNoDialog
import lorry.folder.items.dossiersigma.ui.components.ItemComponent
import lorry.folder.items.dossiersigma.ui.components.TagInfos
import lorry.folder.items.dossiersigma.ui.components.TagInfosDialog
import lorry.folder.items.dossiersigma.ui.components.Tools.DEFAULT
import lorry.folder.items.dossiersigma.ui.theme.DossierSigmaTheme
import java.io.File
import javax.inject.Inject
import kotlin.random.Random
import lorry.folder.items.dossiersigma.data.dataSaver.Memo
import lorry.folder.items.dossiersigma.ui.settings.SettingsViewModel
import lorry.folder.items.dossiersigma.ui.settings.settingsPage

@Composable
fun MainActivity.settingsPage(
    vm: SettingsViewModel

) {
    val nasAddressFromDataStore by vm.settingsManager.nasAddressFlow.collectAsState("")
    var nasAddress by remember(nasAddressFromDataStore) {
        mutableStateOf(nasAddressFromDataStore)
    }

    val nasLoginFromDataStore by vm.settingsManager.nasLoginFlow.collectAsState("")
    var nasLogin by remember(nasLoginFromDataStore) {
        mutableStateOf(nasLoginFromDataStore)
    }

    val nasPasswordFromDataStore by vm.settingsManager.nasPasswordFlow.collectAsState("")
    var nasPassword by remember(nasPasswordFromDataStore) {
        mutableStateOf(nasPasswordFromDataStore)
    }

    val nasFolderFromDataStore by vm.settingsManager.nasFolderFlow.collectAsState("")
    var nasFolder by remember(nasFolderFromDataStore) {
        mutableStateOf(nasFolderFromDataStore)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        //données
        Row(){
            val rowHeight = 50.dp

            //titres
            Column(
                modifier = Modifier,

            ) {
                Text(
                    modifier = Modifier.height(rowHeight),
                    text = "Addresse")
                Text(
                    modifier = Modifier.height(rowHeight),
                    text = "Login")
                Text(
                    modifier = Modifier.height(rowHeight),
                    text = "Mot de passe")
                Text(
                    modifier = Modifier.height(rowHeight),
                    text = "Répertoire")
            }

            //champs de saisie
            Column(
                modifier = Modifier

            ) {
                TextField(
                    modifier = Modifier.height(rowHeight),
                    value = nasAddress,
                    onValueChange = {
                        nasAddress = it
                    }

                )

                TextField(
                    modifier = Modifier.height(rowHeight),
                    value = nasLogin,
                    onValueChange = {
                        nasLogin = it
                    }

                )

                TextField(
                    modifier = Modifier.height(rowHeight),
                    value = nasPassword,
                    onValueChange = {
                        nasPassword = it
                    }

                )

                TextField(
                    modifier = Modifier.height(rowHeight),
                    value = nasFolder,
                    onValueChange = {
                        nasFolder = it
                    }
                )
            }
        }

        Spacer(
            modifier = Modifier
                .weight(1f)
        )

        //boutons, en bas de la page
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {

            Spacer(
                modifier = Modifier
                    .weight(1f)
            )

            Button(
                modifier = Modifier
                    .padding(end = 20.dp, bottom = 10.dp),
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = Color.Red,
                    contentColor = Color.Black
                ),
                onClick = {
                    mainViewModel.setIsSettingsPageVisible(false)
                }
            ) {
                Text(text = "Annuler")
            }

            Button(
                modifier = Modifier
                    .padding(end = 20.dp, bottom = 10.dp),
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = Color.Red,
                    contentColor = Color.Black
                ),
                onClick = {
                    vm.viewModelScope.launch{
                        settingsViewModel.settingsManager.saveNasAddress(nasAddress)
                        settingsViewModel.settingsManager.saveNasLogin(nasLogin)
                        settingsViewModel.settingsManager.saveNasPassword(nasPassword)
                        settingsViewModel.settingsManager.saveNasFolder(nasFolder)
                    }

                    //sauver dans les opréférences

                    mainViewModel.setIsSettingsPageVisible(false)
                }
            ) {
                Text(text = "Enregistrer")
            }
        }
    }
}