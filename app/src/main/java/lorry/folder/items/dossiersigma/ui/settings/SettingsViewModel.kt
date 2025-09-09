package lorry.folder.items.dossiersigma.ui.settings

import android.content.Context
import android.util.Log
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kavi.droid.color.palette.KvColorPalette
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
/**
 * PALETTE DE COULEURS
 *
 * utilisation:
 * init: setBaseColor + setNightAndDay, indépendants
 *
 * resultat: getPrimaryPair(), getSecondaryPair(), getTertiaryPair()
 */
class SettingsViewModel @Inject constructor(
    val context: Context,
    val settings: SettingsManager
) : ViewModel() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    // Pour éviter d’initialiser 2× si la page est recomposée :
    private var initialized = false
    companion object{
        const val TAG = "SgsVM"
    }

//    init{
//        serviceScope.launch {
//            if (initialized)
//                return@launch
//
//            val initialBaseColor = settingsManager.baseColorFlow.first()
////            setBaseColor(initialBaseColor)
//
//            val initialTheme = settingsManager.themeFlow.first()
////            setNightAndDay(initialTheme)
//            Log.d(TAG, "initialTheme=$initialTheme, initialBaseColor=$initialBaseColor")
//
//            initialized = true
//        }
//    }

    // 1) Flows persistants (DataStore)
    private val baseColorPersisted: StateFlow<Color> =
        settings.baseColorFlow.stateIn(viewModelScope, SharingStarted.Eagerly, Color(0xFF4F86F7))

    private val modePersisted: StateFlow<NightAndDay> =
        settings.themeFlow.stateIn(viewModelScope, SharingStarted.Eagerly, NightAndDay.LIGHT)

    // 2) Overrides locaux pour l’aperçu (null = pas d’override)
    private val baseColorOverride = MutableStateFlow<Color?>(null)
    private val modeOverride      = MutableStateFlow<NightAndDay?>(null)

    // 3) Valeurs “effectives” (persisté || override)
    val baseColorEffective: StateFlow<Color> =
        combine(baseColorPersisted, baseColorOverride) { p, o -> o ?: p }
            .stateIn(viewModelScope, SharingStarted.Eagerly, baseColorPersisted.value)

    val modeEffective: StateFlow<NightAndDay> =
        combine(modePersisted, modeOverride) { p, o -> o ?: p }
            .stateIn(viewModelScope, SharingStarted.Eagerly, modePersisted.value)

    val baseColorChanged: StateFlow<Boolean> =
        combine(baseColorPersisted, baseColorOverride) { p, o -> o != null && o != p }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val modeChanged: StateFlow<Boolean> =
        combine(modePersisted, modeOverride) { p, o -> o != null && o != p }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)


    val colorScheme: StateFlow<ColorScheme> = combine(
        baseColorEffective,
        modeEffective
    ) { baseColor, nightAndDay ->
        generateKvColorScheme(baseColor, nightAndDay)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = generateKvColorScheme(Color.Green, NightAndDay.LIGHT)
    )

    // --- API Settings ---
    fun previewBaseColor(c: Color?) = baseColorOverride.tryEmit(c)
    fun previewMode(m: NightAndDay?) = modeOverride.tryEmit(m)

    suspend fun saveBaseColor(c: Color) { settings.saveBaseColor(c); baseColorOverride.value = null }
    suspend fun saveMode(m: NightAndDay) { settings.saveTheme(m); modeOverride.value = null }


    private fun generateKvColorScheme(baseColor: Color, nightAndDay: NightAndDay): ColorScheme {
        KvColorPalette.initialize(
            baseColor = baseColor,
        )
        val result = if (nightAndDay == NightAndDay.LIGHT)
            KvColorPalette.colorSchemeThemePalette.lightColorScheme
        else
            KvColorPalette.colorSchemeThemePalette.darkColorScheme

        Log.d(TAG, "generateKvColorScheme: onPrimary=${result.onPrimary.toHex()}")
        return result
    }

    fun getPrimaryPair(): ColorPair = ColorPair(
        foreground = colorScheme.value.primary,
        background = colorScheme.value.onPrimary
    )

    fun getSecondaryPair(): ColorPair = ColorPair(
        foreground = colorScheme.value.secondary,
        background = colorScheme.value.onSecondary
    )

    fun getTertiaryPair(): ColorPair = ColorPair(
        foreground = colorScheme.value.tertiary,
        background = colorScheme.value.onTertiary
    )

    fun getBackgroundColor(): Color = colorScheme.value.background
}


enum class NightAndDay {
    LIGHT,
    DARK
}

fun NightAndDay.isLight(): Boolean = this == NightAndDay.LIGHT
fun NightAndDay.isDark(): Boolean = this == NightAndDay.DARK

fun Boolean.toNightAndDayIsDark(): NightAndDay = if (this) NightAndDay.DARK else NightAndDay.LIGHT


data class ColorPair(
    val foreground: Color,
    val background: Color
    )

fun Color.isLightBase(): Boolean {
    val r = this.red
    val g = this.green
    val b = this.blue
    // luminance relative sRGB (approx.)
    val lum = 0.2126f * r + 0.7152f * g + 0.0722f * b
    return lum > 0.7f // seuil à ajuster selon ton rendu
}