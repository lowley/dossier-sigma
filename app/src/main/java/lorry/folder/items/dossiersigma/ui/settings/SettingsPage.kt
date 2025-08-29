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
    val nasAddressFromDataStore by vm.settingsManager.nasAddressFlow.collectAsState("")
    var nasAddress = rememberSaveable("") {
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
    val nasLoginFromDataStore by vm.settingsManager.nasLoginFlow.collectAsState("")
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
    val nasPasswordFromDataStore by vm.settingsManager.nasPasswordFlow.collectAsState("")
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
    val nasFolderFromDataStore by vm.settingsManager.nasFolderFlow.collectAsState("")
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
                    state = nasFolder,
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

                                settingsViewModel.settingsManager.saveNasAddress(nasAddress.value)
                                settingsViewModel.settingsManager.saveNasLogin(nasLogin.value)
                                settingsViewModel.settingsManager.saveNasPassword(nasPassword.value)
                                settingsViewModel.settingsManager.saveNasFolder(nasFolder.value)

//                                settingsViewModel.settingsManager.saveTheme(if (theme.value.isDark()) NightAndDay.DARK else NightAndDay.LIGHT)
//                                settingsViewModel.setNightAndDay(if (theme.value.isDark()) NightAndDay.DARK else NightAndDay.LIGHT)

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
                    swatches = Palettes.mixedPalettes,
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
            Color(0xFF0F0F10),
            Color(0xFF1A1B1D),
            Color(0xFF27292C),
            Color(0xFF36393D),
            Color(0xFF4A4E53)
        ),
        // 1) Gris chauds
        listOf(
            Color(0xFF1A1715),
            Color(0xFF2A2623),
            Color(0xFF3B3632),
            Color(0xFF4C4641),
            Color(0xFF5E5751)
        ),

        // 2) Bleus "nuit"
        listOf(
            Color(0xFF031225),
            Color(0xFF07203A),
            Color(0xFF0C2E50),
            Color(0xFF123B66),
            Color(0xFF1B4A7D)
        ),
        // 3) Bleus profonds
        listOf(
            Color(0xFF081633),
            Color(0xFF0E224A),
            Color(0xFF173060),
            Color(0xFF213E76),
            Color(0xFF2B4C8C)
        ),
        // 4) Bleus froids vifs
        listOf(
            Color(0xFF0A213F),
            Color(0xFF0E2F58),
            Color(0xFF143D72),
            Color(0xFF1D4C8C),
            Color(0xFF2A5AA6)
        ),
        // 5) Bleus grisés (blue-gray)
        listOf(
            Color(0xFF0F1A24),
            Color(0xFF172635),
            Color(0xFF203445),
            Color(0xFF2A4256),
            Color(0xFF355066)
        ),

        // 6) Cyan profonds
        listOf(
            Color(0xFF042127),
            Color(0xFF07363F),
            Color(0xFF0A4A58),
            Color(0xFF0F5F72),
            Color(0xFF16748B)
        ),
        // 7) Teal / bleu-vert
        listOf(
            Color(0xFF05221D),
            Color(0xFF08342B),
            Color(0xFF0C4639),
            Color(0xFF115847),
            Color(0xFF176B56)
        ),
        // 8) Turquoises doux
        listOf(
            Color(0xFF06201F),
            Color(0xFF0B3432),
            Color(0xFF134846),
            Color(0xFF1C5C5A),
            Color(0xFF26716F)
        ),

        // 9) Verts forêt
        listOf(
            Color(0xFF0A1D0F),
            Color(0xFF0F2C18),
            Color(0xFF153B22),
            Color(0xFF1D4B2D),
            Color(0xFF265C38)
        ),
        // 10) Verts olive
        listOf(
            Color(0xFF1A1F0C),
            Color(0xFF232A10),
            Color(0xFF2E3616),
            Color(0xFF39431D),
            Color(0xFF465127)
        ),

        // 11) Jaunes ambrés (dark-friendly)
        listOf(
            Color(0xFF231A02),
            Color(0xFF342607),
            Color(0xFF48330D),
            Color(0xFF5E4215),
            Color(0xFF75531F)
        ),
        // 12) Or vieilli / laiton
        listOf(
            Color(0xFF221C05),
            Color(0xFF32290A),
            Color(0xFF423612),
            Color(0xFF53441B),
            Color(0xFF655226)
        ),

        // 13) Oranges bruns
        listOf(
            Color(0xFF2A1407),
            Color(0xFF3D1E0B),
            Color(0xFF532910),
            Color(0xFF6B3516),
            Color(0xFF85411D)
        ),
        // 14) Oranges cuivrés
        listOf(
            Color(0xFF2B1209),
            Color(0xFF3F1A0E),
            Color(0xFF562315),
            Color(0xFF6E2E1D),
            Color(0xFF883926)
        ),

        // 15) Rouges bordeaux
        listOf(
            Color(0xFF26060B),
            Color(0xFF3A0B13),
            Color(0xFF4F111B),
            Color(0xFF661826),
            Color(0xFF7F2031)
        ),
        // 16) Rouges profonds
        listOf(
            Color(0xFF2A0707),
            Color(0xFF3F0D0D),
            Color(0xFF561515),
            Color(0xFF6F1E1E),
            Color(0xFF892828)
        ),

        // 17) Magentas sourds
        listOf(
            Color(0xFF230818),
            Color(0xFF330D23),
            Color(0xFF45132F),
            Color(0xFF591A3D),
            Color(0xFF6E224C)
        ),
        // 18) Fuchsias froids
        listOf(
            Color(0xFF1B0A1D),
            Color(0xFF290F2B),
            Color(0xFF38143A),
            Color(0xFF491A4B),
            Color(0xFF5C215E)
        ),

        // 19) Violets profonds
        listOf(
            Color(0xFF140A25),
            Color(0xFF1F1036),
            Color(0xFF2B1748),
            Color(0xFF391F5C),
            Color(0xFF472870)
        ),
        // 20) Violets bleutés
        listOf(
            Color(0xFF0F0C27),
            Color(0xFF19143A),
            Color(0xFF231D4E),
            Color(0xFF2F2763),
            Color(0xFF3B3280)
        ),

        // 21) Bruns terre
        listOf(
            Color(0xFF1A100A),
            Color(0xFF26170F),
            Color(0xFF331E14),
            Color(0xFF42261A),
            Color(0xFF523020)
        ),
        // 22) Bruns chocolat
        listOf(
            Color(0xFF160C07),
            Color(0xFF22130C),
            Color(0xFF2F1B11),
            Color(0xFF3D2417),
            Color(0xFF4C2E1E)
        ),

        // 23) Neutres bleutés (slate)
        listOf(
            Color(0xFF0D1116),
            Color(0xFF171C24),
            Color(0xFF222935),
            Color(0xFF2E3747),
            Color(0xFF3B475B)
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
            Color(0xFFF7F9FB),
            Color(0xFFE9EEF3),
            Color(0xFFD8DFE7),
            Color(0xFFC4CDD8),
            Color(0xFFAFBBC9)
        ),
        // 1) Neutres chauds (gris beiges)
        listOf(
            Color(0xFFFAF8F6),
            Color(0xFFF0ECE8),
            Color(0xFFE5DFD8),
            Color(0xFFD8D1C8),
            Color(0xFFCBC3B8)
        ),

        // 2) Bleus clairs “ciel”
        listOf(
            Color(0xFFF2F7FE),
            Color(0xFFE3EEFD),
            Color(0xFFCFE2FB),
            Color(0xFFB9D4F7),
            Color(0xFFA1C5F3)
        ),
        // 3) Bleus moyens “UI”
        listOf(
            Color(0xFFF3F7FC),
            Color(0xFFE4EDFA),
            Color(0xFFD0E0F7),
            Color(0xFFBAD1F2),
            Color(0xFFA3C1ED)
        ),
        // 4) Bleus gris (blue-gray)
        listOf(
            Color(0xFFF5F8FB),
            Color(0xFFE8EEF4),
            Color(0xFFD8E0E9),
            Color(0xFFC6D0DD),
            Color(0xFFB2BFCE)
        ),
        // 5) Bleus vifs (accents)
        listOf(
            Color(0xFFEDF5FF),
            Color(0xFFD9EAFF),
            Color(0xFFC1DBFF),
            Color(0xFFA7CBFF),
            Color(0xFF8DBAFF)
        ),

        // 6) Cyans clairs
        listOf(
            Color(0xFFECFAFB),
            Color(0xFFD6F2F5),
            Color(0xFFBEE7EB),
            Color(0xFFA6DBE1),
            Color(0xFF8DCED6)
        ),
        // 7) Teal (bleu-vert doux)
        listOf(
            Color(0xFFEDF9F7),
            Color(0xFFD8F1EC),
            Color(0xFFC2E6DF),
            Color(0xFFA9D9D0),
            Color(0xFF92CCC2)
        ),
        // 8) Turquoises pastels
        listOf(
            Color(0xFFEDFAF9),
            Color(0xFFD9F2EF),
            Color(0xFFC3E8E4),
            Color(0xFFAEDDD9),
            Color(0xFF98D2CE)
        ),

        // 9) Verts frais
        listOf(
            Color(0xFFF1FBF3),
            Color(0xFFDEF6E4),
            Color(0xFFC9EED4),
            Color(0xFFB3E5C4),
            Color(0xFF9BDBB3)
        ),
        // 10) Olive clair (lisible en clair)
        listOf(
            Color(0xFFFAFBF2),
            Color(0xFFF0F3E0),
            Color(0xFFE5EAD0),
            Color(0xFFD9E0C1),
            Color(0xFFCBD5B1)
        ),

        // 11) Jaunes ambrés doux
        listOf(
            Color(0xFFFFFCF1),
            Color(0xFFFEF6D9),
            Color(0xFFFBEEC0),
            Color(0xFFF6E3A8),
            Color(0xFFF0D88F)
        ),
        // 12) Laiton / or vieilli clairs
        listOf(
            Color(0xFFFBF8EE),
            Color(0xFFF4EED8),
            Color(0xFFEDE4C3),
            Color(0xFFE3D8AE),
            Color(0xFFD8CB99)
        ),

        // 13) Oranges doux
        listOf(
            Color(0xFFFFF6F1),
            Color(0xFFFFE9DB),
            Color(0xFFFFDCC7),
            Color(0xFFFFCFB4),
            Color(0xFFFEC3A3)
        ),
        // 14) Cuivres clairs
        listOf(
            Color(0xFFFEF6F3),
            Color(0xFFFBE7DE),
            Color(0xFFF6D7CB),
            Color(0xFFEFC6B7),
            Color(0xFFE7B4A3)
        ),

        // 15) Rouges rosés (UI non agressifs)
        listOf(
            Color(0xFFFFF4F5),
            Color(0xFFFFE4E7),
            Color(0xFFFFD2D7),
            Color(0xFFFFBFC7),
            Color(0xFFFFACB7)
        ),
        // 16) Rouges corail
        listOf(
            Color(0xFFFEF4F3),
            Color(0xFFFDE4E1),
            Color(0xFFFBD2CD),
            Color(0xFFF7BFBA),
            Color(0xFFF2ABA6)
        ),

        // 17) Magentas pastels
        listOf(
            Color(0xFFFEF5FA),
            Color(0xFFFBE6F2),
            Color(0xFFF7D5E9),
            Color(0xFFF1C3DF),
            Color(0xFFEAB1D5)
        ),
        // 18) Fuchsias doux
        listOf(
            Color(0xFFFEF6FE),
            Color(0xFFF9E6FB),
            Color(0xFFF3D5F6),
            Color(0xFFEBC3F0),
            Color(0xFFE2B1E9)
        ),

        // 19) Violets lavande
        listOf(
            Color(0xFFFAF7FF),
            Color(0xFFF0E8FE),
            Color(0xFFE4D8FB),
            Color(0xFFD6C7F6),
            Color(0xFFC7B6F0)
        ),
        // 20) Violets bleutés (pervenche)
        listOf(
            Color(0xFFF6F7FF),
            Color(0xFFE8EAFF),
            Color(0xFFD9DCFE),
            Color(0xFFC9CDFB),
            Color(0xFFB8BFF6)
        ),

        // 21) Bruns sable
        listOf(
            Color(0xFFFBF8F5),
            Color(0xFFF2E9E2),
            Color(0xFFE7DBD1),
            Color(0xFFDCCDBF),
            Color(0xFFD0C0AF)
        ),
        // 22) Bruns clairs (cappuccino)
        listOf(
            Color(0xFFFCF7F4),
            Color(0xFFF3E9E3),
            Color(0xFFE8DBD3),
            Color(0xFFDDCEC4),
            Color(0xFFD2C1B6)
        ),

        // 23) Neutres “paper” (gris très légers)
        listOf(
            Color(0xFFFFFFFF),
            Color(0xFFFAFBFC),
            Color(0xFFF5F6F8),
            Color(0xFFEFF1F4),
            Color(0xFFE8EBEF)
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
