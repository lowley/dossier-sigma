package lorry.folder.items.dossiersigma.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewModelScope
import com.elixer.palette.composables.Palette
import com.elixer.palette.constraints.HorizontalAlignment
import com.elixer.palette.constraints.VerticalAlignment
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.basics.domain.SigmaPath
import lorry.folder.items.dossiersigma.basics.domain.mapSigmaPaths
import lorry.folder.items.dossiersigma.basics.domain.str
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity
import lorry.folder.items.dossiersigma.ui.sigma.SigmaColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SigmaActivity.SettingsPage(
    vm: SettingsViewModel

) {
    /////////////////
    // nas address //
    /////////////////
    val nasAddressFromDataStore by vm.settings.nasAddressFlow.collectAsState("")
    var nasAddress = rememberSaveable(SigmaPath("")) {
        mutableStateOf(nasAddressFromDataStore)
    }

    var userEditedAddress = remember {
        mutableStateOf(false)
    }

    // Si la valeur DataStore change et que l'utilisateur n'a pas commencé à taper,
    // on met à jour le champ local pour rester en phase.
    LaunchedEffect(nasAddressFromDataStore) {
        if (!userEditedAddress.value) nasAddress.value = nasAddressFromDataStore
    }

    val hasNasAddressChanged = remember {
        derivedStateOf { userEditedAddress.value && nasAddress.value != nasAddressFromDataStore }
    }

    ///////////////
    // nas login //
    ///////////////
    val nasLoginFromDataStore by vm.settings.nasLoginFlow.collectAsState("")
    var nasLogin = rememberSaveable(nasLoginFromDataStore) {
        mutableStateOf(nasLoginFromDataStore)
    }

    var userEditedNasLogin = remember {
        mutableStateOf(false)
    }

    // Si la valeur DataStore change et que l'utilisateur n'a pas commencé à taper,
    // on met à jour le champ local pour rester en phase.
    LaunchedEffect(nasLoginFromDataStore) {
        if (!userEditedNasLogin.value) nasLogin.value = nasLoginFromDataStore
    }

    val hasNasLoginChanged = remember {
        derivedStateOf { userEditedNasLogin.value && nasLogin.value != nasLoginFromDataStore }
    }

    //////////////////
    // nas password //
    //////////////////
    val nasPasswordFromDataStore by vm.settings.nasPasswordFlow.collectAsState("")
    var nasPassword = remember(nasPasswordFromDataStore) {
        mutableStateOf(nasPasswordFromDataStore)
    }

    var userEditedNasPassword = remember {
        mutableStateOf(false)
    }

    // Si la valeur DataStore change et que l'utilisateur n'a pas commencé à taper,
    // on met à jour le champ local pour rester en phase.
    LaunchedEffect(nasPasswordFromDataStore) {
        if (!userEditedNasPassword.value) nasPassword.value = nasPasswordFromDataStore
    }

    val hasNasPasswordChanged = remember {
        derivedStateOf { userEditedNasPassword.value && nasPassword.value != nasPasswordFromDataStore }
    }

    ////////////////
    // nas folder //
    ////////////////
    val nasFolderFromDataStore by vm.settings.nasFolderFlow.collectAsState(SigmaPath(""))
    var nasFolder = remember(nasFolderFromDataStore) {
        mutableStateOf(nasFolderFromDataStore)
    }

    var userEditedNasFolder = remember {
        mutableStateOf(false)
    }

    // Si la valeur DataStore change et que l'utilisateur n'a pas commencé à taper,
    // on met à jour le champ local pour rester en phase.
    LaunchedEffect(nasFolderFromDataStore) {
        if (!userEditedNasFolder.value) nasFolder.value = nasFolderFromDataStore
    }

    val hasNasFolderChanged = remember {
        derivedStateOf { userEditedNasFolder.value && nasFolder.value != nasFolderFromDataStore }
    }

    ////////////////
    // base color //
    ////////////////
    val baseColorEffective = vm.baseColorEffective.collectAsState(Color(0xFF4F86F7))

//    var userEditedBaseColor = rememberSaveable  {
//        mutableStateOf(false)
//    }

    // Si la valeur DataStore change et que l'utilisateur n'a pas commencé à taper,
    // on met à jour le champ local pour rester en phase.
//    LaunchedEffect(baseColorFromDataStore.value) {
//        if (!userEditedBaseColor.value) {
//            baseColor.value = baseColorFromDataStore.value
//        }
//    }

    val hasBaseColorChanged = vm.baseColorChanged.collectAsState()

    /////////////////////////
    // theme light ou dark //
    /////////////////////////
    val themeEffective by vm.modeEffective.collectAsState(NightAndDay.LIGHT)
//    var theme = remember(themeFromDataStore) {
//        mutableStateOf(themeFromDataStore)
//    }
//
//    var userEditedTheme = remember {
//        mutableStateOf(false)
//    }

    // Si la valeur DataStore change et que l'utilisateur n'a pas commencé à taper,
    // on met à jour le champ local pour rester en phase.
//    LaunchedEffect(themeFromDataStore) {
//        if (!userEditedTheme.value) {
//            theme.value = themeFromDataStore
////            settingsViewModel.setNightAndDay(theme.value)
//        }
//    }
//
    val hasThemeChanged = vm.modeChanged.collectAsState()

    //////////
    // tout //
    //////////
    val hasSomethingChanged =
         hasNasAddressChanged.value ||
                 hasNasLoginChanged.value ||
                 hasNasPasswordChanged.value ||
                 hasNasFolderChanged.value ||
                 hasBaseColorChanged.value ||
                 hasThemeChanged.value

//    LaunchedEffect(hasBaseColorChanged.value, hasSomethingChanged) {
//        Log.d("SETTINGS", "hasBase=${hasBaseColorChanged.value}  save=${hasSomethingChanged}  base=${baseColor.value}  ds=${baseColorFromDataStore}")
//    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        var showColorPicker by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //données
            val cellHeight = 50.dp
            val titleWidth = 170.dp
            val inputWidth = 300.dp

            //////////////////
            // aire adresse //
            //////////////////
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(bottom = 10.dp)
            ) {
                TitleZone(
                    modifier = Modifier,
                    text = "Adresse :",
                    width = titleWidth,
                    height = cellHeight
                )

                inputZone(
                    modifier = Modifier,
                    label = "ex: 192.168.1.26",
                    state = nasAddress,
                    width = inputWidth,
                    height = cellHeight,
                    userEditeField = userEditedAddress

                )
            }

            ////////////////
            // aire login //
            ////////////////
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(bottom = 10.dp)
            ) {
                TitleZone(
                    modifier = Modifier,
                    text = "Utilisateur :",
                    width = titleWidth,
                    height = cellHeight
                )

                inputZone(
                    modifier = Modifier,
                    label = "ex: tintin76",
                    state = nasLogin,
                    width = inputWidth,
                    height = cellHeight,
                    userEditeField = userEditedNasLogin
                )
            }

            ///////////////////////
            // aire mot de passe //
            ///////////////////////
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(bottom = 10.dp)
            ) {
                TitleZone(
                    modifier = Modifier,
                    text = "Mot de passe :",
                    width = titleWidth,
                    height = cellHeight
                )

                inputZone(
                    modifier = Modifier,
                    label = "ex: 123456",
                    state = nasPassword,
                    width = inputWidth,
                    height = cellHeight,
                    userEditeField = userEditedNasPassword
                )
            }

            /////////////////////
            // aire répertoire //
            /////////////////////
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(bottom = 10.dp)
            ) {
                TitleZone(
                    modifier = Modifier,
                    text = "Répertoire NAS :",
                    width = titleWidth,
                    height = cellHeight
                )

                inputZone(
                    modifier = Modifier,
                    label = "ex: fichiers",
                    state = nasFolder.mapSigmaPaths{ path -> path.str },
                    width = inputWidth,
                    height = cellHeight,
                    userEditeField = userEditedNasFolder
                )
            }

            Spacer(
                modifier = Modifier
                    .height(10.dp)
            )

            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .align(Alignment.CenterHorizontally),
                verticalAlignment = CenterVertically,

                ) {
                Text(
                    text = "Changer la couleur du fond",
                    modifier = Modifier
                        .padding(end = 10.dp),
                    color = SigmaColors.current.onPrimary
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
                        .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                        .background(baseColorEffective.value)
                )
            }

            Row(
                modifier = Modifier
                    .padding(end = 20.dp, bottom = 10.dp),
            ) {
                Switch(
                    modifier = Modifier,
                    checked = themeEffective.isDark(),
                    onCheckedChange = {
                        vm.previewMode(if (it) NightAndDay.DARK else NightAndDay.LIGHT)
                    }
                )

                Text(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 10.dp),
                    text = if (themeEffective.isDark()) "Mode sombre" else "Mode clair",
                    color = SigmaColors.current.onPrimary
                )
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
                        vm.previewMode(null)
                        vm.previewBaseColor(null)
                        mainViewModel.setIsSettingsPageVisible(false)
                    }
                ) {
                    Text(text = "Annuler")
                }

                val saveEnabled = hasSomethingChanged
                val disabledContainer = MaterialTheme.colorScheme.surfaceVariant
                val container = if (saveEnabled) Color.Red else disabledContainer

                Button(
                    modifier = Modifier
                        .padding(end = 20.dp, bottom = 10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = container,
                        contentColor   = if (saveEnabled) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    enabled = hasSomethingChanged,
                    onClick = {
                        vm.viewModelScope.launch {
                            if (hasSomethingChanged) {

                                settingsViewModel.settings.saveNasAddress(nasAddress.value)
                                settingsViewModel.settings.saveNasLogin(nasLogin.value)
                                settingsViewModel.settings.saveNasPassword(nasPassword.value)
                                settingsViewModel.settings.saveNasFolder(nasFolder.value)

                                val base = baseColorEffective.value
                                var mode = if (base.isLightBase()) NightAndDay.LIGHT
                                else
                                    NightAndDay.DARK

                                if (hasThemeChanged.value)
                                    mode = themeEffective

                                settingsViewModel.saveMode(mode)
                                settingsViewModel.saveBaseColor(baseColorEffective.value)
                            }
                        }

                        mainViewModel.setIsSettingsPageVisible(false)
                        homeViewModel.setHomePageVisible(true)
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
                Box(
                    modifier = Modifier
                        .offset(x = (-52).dp, y = (52).dp)
                        .size(104.dp)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(52.dp)
                        )
                        .align(Alignment.BottomStart)
                        .shadow(8.dp, shape = RoundedCornerShape(52.dp))
                )

                Palette(
                    defaultColor = baseColorEffective.value,
//                    defaultColor = Color(0xFF363E4C),
                    buttonSize = 100.dp,
                    swatches = if (themeEffective.isDark()) Palettes.darkPalettes else Palettes.lightPalettes,
                    innerRadius = 400f,
                    strokeWidth = 120f,
                    spacerRotation = 5f,
                    spacerOutward = 2f,
                    verticalAlignment = VerticalAlignment.Bottom,
                    horizontalAlignment = HorizontalAlignment.Start,
                    onColorSelected = {
                        vm.previewBaseColor(it)
                        showColorPicker = false
                    }
                )
            }

        }
    }
}

@Composable
context(RowScope)
fun TitleZone(
    modifier: Modifier,
    text: String,
    width: Dp,
    height: Dp
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
    ) {
        Text(
            modifier = modifier
                .align(Alignment.CenterStart),
            text = text,
            color = SigmaColors.current.onPrimary
        )
    }
}

@Composable
context(RowScope)
fun inputZone(
    modifier: Modifier,
    label: String,
    state: MutableState<String>,
    width: Dp,
    height: Dp,
    userEditeField: MutableState<Boolean>
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
    ) {
        TextField(
            modifier = Modifier.height(height),
            value = state.value,
            label = { Text(label) },
            onValueChange = {
                state.value = it
                userEditeField.value = true
            },
        )
    }
}

object Palettes {
    /**
     * Chaque sous-liste = 1 famille chromatique.
     * Ordre interne : du plus sombre au plus clair pour favoriser les thèmes dark.
     * Tous les Color(...) sont en ARGB (0xFFrrggbb).
     */
    val darkPalettes: List<List<Color>> = listOf(
        // 0) Noirs & gris neutres
        listOf(
            Color(0xFF080809),
            Color(0xFF0F0F10),
            Color(0xFF171718),
            Color(0xFF232425),
            Color(0xFF2F3032),
            Color(0xFF3B3C3E)
        ),

        // 1) Gris chauds
        listOf(
            Color(0xFF151210),
            Color(0xFF1D1917),
            Color(0xFF27221F),
            Color(0xFF322B26),
            Color(0xFF3E352F),
            Color(0xFF4A413A)
        ),

        // 2) Bleus "nuit"
        listOf(
            Color(0xFF00121B),
            Color(0xFF03202B),
            Color(0xFF07343F),
            Color(0xFF0B4A55),
            Color(0xFF11656F),
            Color(0xFF1A7F8A)
        ),

        // 3) Bleus profonds
        listOf(
            Color(0xFF041426),
            Color(0xFF0A2138),
            Color(0xFF13314E),
            Color(0xFF1C3F64),
            Color(0xFF274F7A),
            Color(0xFF335F91)
        ),

        // 4) Bleus froids vifs
        listOf(
            Color(0xFF061A31),
            Color(0xFF0D2A47),
            Color(0xFF14395E),
            Color(0xFF1C4A76),
            Color(0xFF255E8F),
            Color(0xFF3072A8)
        ),

        // 5) Bleus grisés (blue-gray)
        listOf(
            Color(0xFF0A1116),
            Color(0xFF121921),
            Color(0xFF1B2430),
            Color(0xFF243242),
            Color(0xFF2E4054),
            Color(0xFF394D66)
        ),

        // 6) Cyan profonds
        listOf(
            Color(0xFF021F22),
            Color(0xFF063038),
            Color(0xFF0A4750),
            Color(0xFF0E5E69),
            Color(0xFF147E86),
            Color(0xFF18A0AA)
        ),

        // 7) Teal / bleu-vert
        listOf(
            Color(0xFF071E1A),
            Color(0xFF0B2C24),
            Color(0xFF0F3A30),
            Color(0xFF155340),
            Color(0xFF1A6C53),
            Color(0xFF21866A)
        ),

        // 8) Turquoises doux
        listOf(
            Color(0xFF061715),
            Color(0xFF0C2B29),
            Color(0xFF123E3C),
            Color(0xFF185352),
            Color(0xFF206A69),
            Color(0xFF2B827F)
        ),

        // 9) Verts forêt
        listOf(
            Color(0xFF06170C),
            Color(0xFF0A2A14),
            Color(0xFF0F3C1E),
            Color(0xFF184F2A),
            Color(0xFF236236),
            Color(0xFF2F7442)
        ),

        // 10) Verts olive
        listOf(
            Color(0xFF101306),
            Color(0xFF161B0A),
            Color(0xFF212612),
            Color(0xFF2B351B),
            Color(0xFF364422),
            Color(0xFF43532A)
        ),

        // 11) Jaunes ambrés (dark-friendly)
        listOf(
            Color(0xFF241A03),
            Color(0xFF2F2206),
            Color(0xFF3D2D0B),
            Color(0xFF4B3810),
            Color(0xFF5B4416),
            Color(0xFF6C511E)
        ),

        // 12) Or vieilli / laiton
        listOf(
            Color(0xFF201B05),
            Color(0xFF2A2309),
            Color(0xFF352C0F),
            Color(0xFF423512),
            Color(0xFF52421A),
            Color(0xFF615024)
        ),

        // 13) Oranges bruns
        listOf(
            Color(0xFF2A1207),
            Color(0xFF361A0A),
            Color(0xFF452112),
            Color(0xFF5A2C18),
            Color(0xFF6E391F),
            Color(0xFF834725)
        ),

        // 14) Oranges cuivrés
        listOf(
            Color(0xFF2A1108),
            Color(0xFF36180C),
            Color(0xFF4A2313),
            Color(0xFF5E2F1A),
            Color(0xFF7A3E24),
            Color(0xFF97502F)
        ),

        // 15) Rouges bordeaux
        listOf(
            Color(0xFF24060B),
            Color(0xFF330A11),
            Color(0xFF431218),
            Color(0xFF571922),
            Color(0xFF6B212B),
            Color(0xFF7F2833)
        ),

        // 16) Rouges profonds
        listOf(
            Color(0xFF240808),
            Color(0xFF341010),
            Color(0xFF451818),
            Color(0xFF5A2121),
            Color(0xFF6F2929),
            Color(0xFF853131)
        ),

        // 17) Magentas sourds
        listOf(
            Color(0xFF220615),
            Color(0xFF2D0E20),
            Color(0xFF3D172B),
            Color(0xFF4F2138),
            Color(0xFF602B45),
            Color(0xFF733553)
        ),

        // 18) Fuchsias froids
        listOf(
            Color(0xFF160615),
            Color(0xFF241022),
            Color(0xFF33142F),
            Color(0xFF44163D),
            Color(0xFF56174C),
            Color(0xFF69205E)
        ),

        // 19) Violets profonds
        listOf(
            Color(0xFF10061A),
            Color(0xFF1A0F2B),
            Color(0xFF27183E),
            Color(0xFF341F53),
            Color(0xFF41266A),
            Color(0xFF4F2E82)
        ),

        // 20) Violets bleutés
        listOf(
            Color(0xFF0C0B1C),
            Color(0xFF16132B),
            Color(0xFF221E40),
            Color(0xFF2E2856),
            Color(0xFF3A3170),
            Color(0xFF484089)
        ),

        // 21) Bruns terre
        listOf(
            Color(0xFF160E0A),
            Color(0xFF21130E),
            Color(0xFF2C1C14),
            Color(0xFF38271C),
            Color(0xFF463321),
            Color(0xFF533F28)
        ),

        // 22) Bruns chocolat
        listOf(
            Color(0xFF120906),
            Color(0xFF1C120D),
            Color(0xFF291A12),
            Color(0xFF361F17),
            Color(0xFF432720),
            Color(0xFF51312A)
        ),

        // 23) Neutres bleutés (slate)
        listOf(
            Color(0xFF0B0F14),
            Color(0xFF12171D),
            Color(0xFF1B222A),
            Color(0xFF24313A),
            Color(0xFF2D3E4A),
            Color(0xFF374B59)
        )
    )


    /**
     * Chaque sous-liste = 1 famille.
     * Ordre interne : du très clair vers le moyen/soutenu (adapté thème clair).
     * Color(...) en ARGB: 0xFFrrggbb
     */
    val lightPalettes: List<List<Color>> = listOf(
        // 0) Neutres froids (gris bleutés)
        listOf(
            Color(0xFFFDFEFF),
            Color(0xFFF7F9FB),
            Color(0xFFEEF4F8),
            Color(0xFFE5EBF0),
            Color(0xFFD7DEE6),
            Color(0xFFC9D1DB)
        ),

        // 1) Neutres chauds (gris beiges)
        listOf(
            Color(0xFFFCFBF9),
            Color(0xFFFAF6F2),
            Color(0xFFF2ECE6),
            Color(0xFFE8E0D8),
            Color(0xFFDCCFC6),
            Color(0xFFCFBFB5)
        ),

        // 2) Bleus clairs “ciel”
        listOf(
            Color(0xFFFBFEFF),
            Color(0xFFF2F9FF),
            Color(0xFFE6F0FF),
            Color(0xFFD8E6FF),
            Color(0xFFC6DBFF),
            Color(0xFFB2CFFF)
        ),

        // 3) Bleus moyens “UI”
        listOf(
            Color(0xFFFBFCFF),
            Color(0xFFF1F7FF),
            Color(0xFFE4EEFF),
            Color(0xFFD3E2FF),
            Color(0xFFBDD6FF),
            Color(0xFFA7C8FF)
        ),

        // 4) Bleus gris (blue-gray)
        listOf(
            Color(0xFFFBFCFD),
            Color(0xFFF3F7F9),
            Color(0xFFE9EEF2),
            Color(0xFFDDE6EC),
            Color(0xFFCFE0E8),
            Color(0xFFC0D7E0)
        ),

        // 5) Bleus vifs (accents)
        listOf(
            Color(0xFFFBFDFF),
            Color(0xFFF0F7FF),
            Color(0xFFE0EEFF),
            Color(0xFFCCE2FF),
            Color(0xFFB6D4FF),
            Color(0xFF9EC6FF)
        ),

        // 6) Cyans clairs
        listOf(
            Color(0xFFFBFFFE),
            Color(0xFFECFDFC),
            Color(0xFFDFF8F8),
            Color(0xFFCFF0EE),
            Color(0xFFBFE7E4),
            Color(0xFFAEDDDC)
        ),

        // 7) Teal (bleu-vert doux)
        listOf(
            Color(0xFFFBFEFD),
            Color(0xFFF0FBF8),
            Color(0xFFE4F6F1),
            Color(0xFFD6EFE7),
            Color(0xFFC6E7DD),
            Color(0xFFB6DFD3)
        ),

        // 8) Turquoises pastels
        listOf(
            Color(0xFFFCFEFD),
            Color(0xFFF2FBFA),
            Color(0xFFE6F6F4),
            Color(0xFFD8F0EE),
            Color(0xFFC8E9E7),
            Color(0xFFB6E1DE)
        ),

        // 9) Verts frais
        listOf(
            Color(0xFFFBFFFC),
            Color(0xFFF2FBF5),
            Color(0xFFE6F6EA),
            Color(0xFFD7F0DE),
            Color(0xFFC6E9D1),
            Color(0xFFB4E1C4)
        ),

        // 10) Olive clair (lisible en clair)
        listOf(
            Color(0xFFFBFDF6),
            Color(0xFFFAFCEA),
            Color(0xFFF1F6DF),
            Color(0xFFE6EBCF),
            Color(0xFFDADFBF),
            Color(0xFFCFD4AF)
        ),

        // 11) Jaunes ambrés doux
        listOf(
            Color(0xFFFFFEFB),
            Color(0xFFFFFBF0),
            Color(0xFFFFF5DF),
            Color(0xFFFFEBC6),
            Color(0xFFF7E2A7),
            Color(0xFFEED98A)
        ),

        // 12) Laiton / or vieilli clairs
        listOf(
            Color(0xFFFFFDF8),
            Color(0xFFFBF5E8),
            Color(0xFFF6ECD6),
            Color(0xFFF0E1C3),
            Color(0xFFE6D5AD),
            Color(0xFFDCC89A)
        ),

        // 13) Oranges doux
        listOf(
            Color(0xFFFFFCFB),
            Color(0xFFFFF5F0),
            Color(0xFFFFEAD9),
            Color(0xFFFFDAC0),
            Color(0xFFFFCBA6),
            Color(0xFFFFBD8D)
        ),

        // 14) Cuivres clairs
        listOf(
            Color(0xFFFFFBFA),
            Color(0xFFFDF3ED),
            Color(0xFFF9E6DC),
            Color(0xFFF2D6C6),
            Color(0xFFE9C5B0),
            Color(0xFFDEB199)
        ),

        // 15) Rouges rosés (UI non agressifs)
        listOf(
            Color(0xFFFFFBFC),
            Color(0xFFFFF3F5),
            Color(0xFFFFE7EA),
            Color(0xFFFFD9DE),
            Color(0xFFFFCCD1),
            Color(0xFFFFBDC4)
        ),

        // 16) Rouges corail
        listOf(
            Color(0xFFFFFBFA),
            Color(0xFFFDF3F2),
            Color(0xFFFCE6E4),
            Color(0xFFF9D6D3),
            Color(0xFFF6C5C0),
            Color(0xFFF2B3AD)
        ),

        // 17) Magentas pastels
        listOf(
            Color(0xFFFFFBFE),
            Color(0xFFFEF5FB),
            Color(0xFFFBE9F6),
            Color(0xFFF6D9F0),
            Color(0xFFF0C8EA),
            Color(0xFFE9B6E1)
        ),

        // 18) Fuchsias doux
        listOf(
            Color(0xFFFFFBFF),
            Color(0xFFFDF2FB),
            Color(0xFFF9E5F6),
            Color(0xFFF4D6F0),
            Color(0xFFEDC6EA),
            Color(0xFFE4B5E1)
        ),

        // 19) Violets lavande
        listOf(
            Color(0xFFFFFBFF),
            Color(0xFFF8F4FF),
            Color(0xFFF0E8FF),
            Color(0xFFE6DBFF),
            Color(0xFFDCCFFB),
            Color(0xFFD2C4F6)
        ),

        // 20) Violets bleutés (pervenche)
        listOf(
            Color(0xFFFEFDFF),
            Color(0xFFF6F8FF),
            Color(0xFFEEF2FF),
            Color(0xFFE5E9FF),
            Color(0xFFDBDEFF),
            Color(0xFFD1D4FF)
        ),

        // 21) Bruns sable
        listOf(
            Color(0xFFFFFBFA),
            Color(0xFFFBF3EE),
            Color(0xFFF3E7DD),
            Color(0xFFE9D9C9),
            Color(0xFFDECBBA),
            Color(0xFFD2BDAA)
        ),

        // 22) Bruns clairs (cappuccino)
        listOf(
            Color(0xFFFFFBFA),
            Color(0xFFF7F0EE),
            Color(0xFFF0E2DD),
            Color(0xFFE6D2C6),
            Color(0xFFDCC0B1),
            Color(0xFFD0AF9E)
        ),

        // 23) Neutres “paper” (gris très légers)
        listOf(
            Color(0xFFFFFFFF),
            Color(0xFFFEFEFF),
            Color(0xFFF8FAFC),
            Color(0xFFF1F4F7),
            Color(0xFFEAEFF3),
            Color(0xFFE3E9EE)
        )
    )

    val mixedPalettes: List<List<Color>> = listOf(
        // 0) Neutres froids (gris bleutés)
        listOf(
            Color(0xFFF7F9FB), // très clair
            Color(0xFFE8EEF4),
            Color(0xFFD4DCE6),
            Color(0xFFBAC6D4),
            Color(0xFF95A4B5),
            Color(0xFF4A4E53)  // foncé
        ),
        // 1) Neutres chauds (gris beiges)
        listOf(
            Color(0xFFFAF8F6),
            Color(0xFFF0ECE8),
            Color(0xFFE4DDD6),
            Color(0xFFD2C6BA),
            Color(0xFFB39F8F),
            Color(0xFF5E5751)
        ),

        // 2) Bleus “ciel → nuit”
        listOf(
            Color(0xFFF2F7FE),
            Color(0xFFE3EEFD),
            Color(0xFFCFE2FB),
            Color(0xFFAFCFF6),
            Color(0xFF6FA6EA),
            Color(0xFF0C2E50)
        ),
        // 3) Bleus UI (légèrement plus saturés)
        listOf(
            Color(0xFFF3F7FC),
            Color(0xFFE4EDFA),
            Color(0xFFCFE0F7),
            Color(0xFFAFCBF1),
            Color(0xFF5B8FE0),
            Color(0xFF173060)
        ),
        // 4) Bleus gris (blue-gray)
        listOf(
            Color(0xFFF5F8FB),
            Color(0xFFE8EEF4),
            Color(0xFFD8E0E9),
            Color(0xFFBECADA),
            Color(0xFF7A93AD),
            Color(0xFF0F1A24)
        ),

        // 5) Cyans (eau)
        listOf(
            Color(0xFFECFAFB),
            Color(0xFFD6F2F5),
            Color(0xFFBFE8EC),
            Color(0xFFA2DAE0),
            Color(0xFF4FB2C1),
            Color(0xFF0A4A58)
        ),
        // 6) Teal (bleu-vert)
        listOf(
            Color(0xFFEDF9F7),
            Color(0xFFD8F1EC),
            Color(0xFFC2E6DF),
            Color(0xFFA8D8CF),
            Color(0xFF4FA99A),
            Color(0xFF0C4639)
        ),
        // 7) Turquoises
        listOf(
            Color(0xFFEDFAF9),
            Color(0xFFD9F2EF),
            Color(0xFFC3E8E4),
            Color(0xFFA9DCD7),
            Color(0xFF4BB9B1),
            Color(0xFF134846)
        ),

        // 8) Verts frais
        listOf(
            Color(0xFFF1FBF3),
            Color(0xFFDEF6E4),
            Color(0xFFC8EED3),
            Color(0xFFAEE2C0),
            Color(0xFF5ABF8C),
            Color(0xFF153B22)
        ),
        // 9) Olives
        listOf(
            Color(0xFFFAFBF2),
            Color(0xFFF0F3E0),
            Color(0xFFE5EAD0),
            Color(0xFFCFD9B4),
            Color(0xFF8FA36A),
            Color(0xFF2E3616)
        ),

        // 10) Jaunes / ambrés
        listOf(
            Color(0xFFFFFCF1),
            Color(0xFFFEF6D9),
            Color(0xFFFBEEC0),
            Color(0xFFF4E39F),
            Color(0xFFE0B94A),
            Color(0xFF48330D)
        ),
        // 11) Or vieilli / laiton
        listOf(
            Color(0xFFFBF8EE),
            Color(0xFFF4EED8),
            Color(0xFFEDE4C3),
            Color(0xFFDCCB9E),
            Color(0xFFB99745),
            Color(0xFF423612)
        ),

        // 12) Oranges doux → cuivrés
        listOf(
            Color(0xFFFFF6F1),
            Color(0xFFFFE9DB),
            Color(0xFFFFD9C2),
            Color(0xFFFFC7A8),
            Color(0xFFEC874F),
            Color(0xFF532910)
        ),
        // 13) Cuivres (chauds)
        listOf(
            Color(0xFFFEF6F3),
            Color(0xFFFBE7DE),
            Color(0xFFF6D7CB),
            Color(0xFFEFC4B4),
            Color(0xFFD47E5F),
            Color(0xFF562315)
        ),

        // 14) Rouges rosés
        listOf(
            Color(0xFFFFF4F5),
            Color(0xFFFFE4E7),
            Color(0xFFFFD2D7),
            Color(0xFFFEBEC7),
            Color(0xFFEB6B84),
            Color(0xFF4F111B)
        ),
        // 15) Rouges corail → bordeaux
        listOf(
            Color(0xFFFEF4F3),
            Color(0xFFFDE4E1),
            Color(0xFFFBD2CD),
            Color(0xFFF7BDB6),
            Color(0xFFE36F61),
            Color(0xFF661826)
        ),

        // 16) Magentas
        listOf(
            Color(0xFFFEF5FA),
            Color(0xFFFBE6F2),
            Color(0xFFF7D5E9),
            Color(0xFFF0C0DD),
            Color(0xFFD26CA9),
            Color(0xFF45132F)
        ),
        // 17) Fuchsias
        listOf(
            Color(0xFFFEF6FE),
            Color(0xFFF9E6FB),
            Color(0xFFF3D5F6),
            Color(0xFFE9C0EE),
            Color(0xFFC66BD4),
            Color(0xFF38143A)
        ),

        // 18) Violets lavande → profonds
        listOf(
            Color(0xFFFAF7FF),
            Color(0xFFF0E8FE),
            Color(0xFFE4D8FB),
            Color(0xFFD2C4F6),
            Color(0xFF9B83E6),
            Color(0xFF2B1748)
        ),
        // 19) Violets bleutés (pervenche)
        listOf(
            Color(0xFFF6F7FF),
            Color(0xFFE8EAFF),
            Color(0xFFD9DCFE),
            Color(0xFFC6CAFB),
            Color(0xFF7A8AEF),
            Color(0xFF231D4E)
        ),

        // 20) Bruns sable → terre
        listOf(
            Color(0xFFFBF8F5),
            Color(0xFFF2E9E2),
            Color(0xFFE7DBD1),
            Color(0xFFD6C7B8),
            Color(0xFFB28E6E),
            Color(0xFF331E14)
        ),
        // 21) Bruns cappuccino → chocolat
        listOf(
            Color(0xFFFCF7F4),
            Color(0xFFF3E9E3),
            Color(0xFFE8DBD3),
            Color(0xFFD7C7BC),
            Color(0xFFA9816A),
            Color(0xFF2F1B11)
        ),

        // 22) Slate (neutres bleutés)
        listOf(
            Color(0xFFF5F6F8),
            Color(0xFFE8EBEF),
            Color(0xFFD6DEE7),
            Color(0xFFBECADA),
            Color(0xFF7A93AD),
            Color(0xFF222935)
        )
    )
}
