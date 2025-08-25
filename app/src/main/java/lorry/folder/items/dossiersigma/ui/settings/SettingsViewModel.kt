package lorry.folder.items.dossiersigma.ui.settings

import android.content.Context
import android.util.Log
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kavi.droid.color.palette.KvColorPalette
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import lorry.folder.items.dossiersigma.ServiceLocator
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
    val context: Context
) : ViewModel() {

    val settingsManager = ServiceLocator.settings(context)

    companion object{
        const val TAG = "SgsVM"
    }

    private val _nightAndDay = MutableStateFlow(NightAndDay.LIGHT)
    val nightAndDay: StateFlow<NightAndDay> = _nightAndDay

    fun setNightAndDay(nightAndDay: NightAndDay) {
        _nightAndDay.value = nightAndDay
    }

    val colorScheme: StateFlow<ColorScheme> = combine(
        settingsManager.baseColorFlow,
        nightAndDay
    ) { baseColor, nightAndDay ->
        generateKvColorScheme(baseColor, nightAndDay)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = generateKvColorScheme(Color.Green, NightAndDay.LIGHT)
    )

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


data class ColorPair(
    val foreground: Color,
    val background: Color
    )