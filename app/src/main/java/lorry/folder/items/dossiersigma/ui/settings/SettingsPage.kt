package lorry.folder.items.dossiersigma.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewModelScope
import com.elixer.palette.Presets
import com.elixer.palette.composables.Palette
import com.elixer.palette.constraints.HorizontalAlignment
import com.elixer.palette.constraints.VerticalAlignment
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.ui.bottomArea.HomeItemInfos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SigmaActivity.SettingsPage(
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

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        var showColorPicker by remember { mutableStateOf(false) }
        var currentBackgroundColor by remember { mutableStateOf(Color.Black) }

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            //données
            Row() {
                val rowHeight = 50.dp

                //titres
                Column(
                    modifier = Modifier,

                    ) {
                    Text(
                        modifier = Modifier.height(rowHeight),
                        text = "Addresse"
                    )
                    Text(
                        modifier = Modifier.height(rowHeight),
                        text = "Login"
                    )
                    Text(
                        modifier = Modifier.height(rowHeight),
                        text = "Mot de passe"
                    )
                    Text(
                        modifier = Modifier.height(rowHeight),
                        text = "Répertoire"
                    )
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
                    .height(10.dp)
            )

            Row(
                verticalAlignment = CenterVertically
            ) {
                Text(
                    text = "Changer la couleur du fond",
                    modifier = Modifier
                        .padding(end = 10.dp)
                )

                Box(
                    modifier = Modifier
                        .size(35.dp)
                        .pointerInput(true) {
                            detectTapGestures(
                                onTap = {
                                    showColorPicker = !showColorPicker
                                }
                            )
                        }
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                        .background(currentBackgroundColor)
                )
            }

            Spacer(
                modifier = Modifier
                    .height(10.dp)
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
                        vm.viewModelScope.launch {
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

        if (showColorPicker) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(35f)
                    .padding(start = 60.dp, bottom = 60.dp)
            ) {
                Palette(
                    defaultColor = currentBackgroundColor,
//                    defaultColor = Color(0xFF363E4C),
                    buttonSize = 100.dp,
//                    swatches = Presets.material(),
                    swatches = Palettes.darkPalette,
                    innerRadius = 400f,
                    strokeWidth = 120f,
                    spacerRotation = 5f,
                    spacerOutward = 2f,
                    verticalAlignment = VerticalAlignment.Bottom,
                    horizontalAlignment = HorizontalAlignment.Start,
                    onColorSelected = {
                        currentBackgroundColor = it
                        showColorPicker = false
                    }
                )
            }

        }
    }
}

object Palettes {
    val darkPalette: List<List<Color>> = listOf(
        // Noirs & gris
        listOf(
            Color(0xFF131313),
            Color(0xFF252525),
            Color(0xFF333333),
            Color(0xFF3E3E3E),
            Color(0xFF4A4A4A)
        ),
        listOf(
            Color(0xFF0F0F0F),
            Color(0xFF16161D),
            Color(0xFF1B1B1B),
            Color(0xFF232B2B),
            Color(0xFF2C3539),
            Color(0xFF414A4C)
        ),
        // Bruns profonds / terre
        listOf(
            Color(0xFF372200),
            Color(0xFF574200),
            Color(0xFF776219),
            Color(0xFF978139),
            Color(0xFFB7A158)
        ),
        listOf(
            Color(0xFF48312B),
            Color(0xFF603A28),
            Color(0xFF522D17),
            Color(0xFF402315),
            Color(0xFF0B0201)
        ),
        // Bleu sombre / nuit
        listOf(
            Color(0xFF002147),
            Color(0xFF003366),
            Color(0xFF001E1E),
            Color(0xFF00312F),
            Color(0xFF005958)
        ),
        listOf(
            Color(0xFF002B36),
            Color(0xFF073642),
            Color(0xFF0F1F24),
            Color(0xFF152D32),
            Color(0xFF2F3B22)
        ),
        // Vert foncé / forêt
        listOf(
            Color(0xFF003F20),
            Color(0xFF035F40),
            Color(0xFF237F60),
            Color(0xFF439E80),
            Color(0xFF63BE9F)
        ),
        listOf(
            Color(0xFF556B2F),
            Color(0xFF6B8E23),
            Color(0xFF466037),
            Color(0xFF2E8B57)
        ),
        // Rouge sombre / bordeaux
        listOf(
            Color(0xFFB22222),
            Color(0xFF8B1A1A),
            Color(0xFFCD5C5C),
            Color(0xFFEE6363),
            Color(0xFFFF6A6A)
        ),
        listOf(
            Color(0xFF46000D),
            Color(0xFF5E0009),
            Color(0xFF720137),
            Color(0xFF590054),
            Color(0xFF42002E)
        ),
        // Purple / violet profond
        listOf(
            Color(0xFF9932CC),
            Color(0xFFD33682),
            Color(0xFFBD93F9),
            Color(0xFF44475A),
            Color(0xFF6272A4)
        ),
        listOf(
            Color(0xFF322F42),
            Color(0xFF4B3A70),
            Color(0xFF212531),
            Color(0xFFC5C3C4),
            Color(0xFFB7A2C9)
        ),
        // Neutres sombres
        listOf(
            Color(0xFF353A34),
            Color(0xFF515051),
            Color(0xFF555D57),
            Color(0xFF616260),
            Color(0xFF717262)
        ),
        listOf(
            Color(0xFF232B2B),
            Color(0xFF2C3539),
            Color(0xFF414A4C),
            Color(0xFF1A2421)
        ),
        // Brown-dark neutrals
        listOf(
            Color(0xFF8B4513),
            Color(0xFFCD661D),
            Color(0xFFEE7621),
            Color(0xFFFF7F24)
        ),
        listOf(
            Color(0xFF6B8E23),
            Color(0xFF556B2F),
            Color(0xFF8B6914),
            Color(0xFFCD950C)
        ),
        // Bleu-vert profond
        listOf(
            Color(0xFF016764),
            Color(0xFF005958),
            Color(0xFF014848),
            Color(0xFF00312F)
        ),
        listOf(
            Color(0xFF1B2F3C),
            Color(0xFF26333B),
            Color(0xFF384B49),
            Color(0xFF3F3F3F)
        ),
        // Palette sombre variée
        listOf(
            Color(0xFF1D1E27),
            Color(0xFF14151C),
            Color(0xFF000000),
            Color(0xFF180E13),
            Color(0xFF27141F)
        ),
        listOf(
            Color(0xFF2F4F4F),
            Color(0xFF4682B4),
            Color(0xFF36454F),
            Color(0xFF2F4F4F)
        ),
        listOf(
            Color(0xFF3B3C36),
            Color(0xFF1B1B1B),
            Color(0xFF100C08),
            Color(0xFF0D031B),
            Color(0xFF242124)
        ),
        listOf(
            Color(0xFF0F0F0F),
            Color(0xFF16161D),
            Color(0xFF36454F),
            Color(0xFF2C3539)
        ),
        listOf(
            Color(0xFF20116D),
            Color(0xFF40318D),
            Color(0xFF6051AC),
            Color(0xFF8071CC),
            Color(0xFF9F90EC)
        ),
        listOf(
            Color(0xFF612302),
            Color(0xFF3D0C02),
            Color(0xFF32174D),
            Color(0xFF3D2B1F)
        ),
        listOf(
            Color(0xFF002147),
            Color(0xFF003366),
            Color(0xFF20116D),
            Color(0xFF40318D),
            Color(0xFF6051AC)
        )
    )
}