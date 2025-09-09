package lorry.folder.items.dossiersigma.UI.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Data class to hold the generated color theme using Jetpack Compose's Color class.
 * Includes primary, secondary, tertiary, background, and surface roles.
 *
 * @property primary The primary color.
 * @property primaryVariant A darker or lighter variant of the primary color.
 * @property secondary An accent color, often analogous or complementary to the primary.
 * @property tertiary An additional accent color, providing more flexibility.
 * @property background The main background color.
 * @property surface The color for surfaces like cards, sheets.
 * @property onPrimary Color for text/icons on primary.
 * @property onSecondary Color for text/icons on secondary.
 * @property onTertiary Color for text/icons on tertiary.
 * @property onBackground Color for text/icons on background.
 * @property onSurface Color for text/icons on surface.
 */
data class ColorTheme(
    val primary: Color,
    val primaryVariant: Color,
    val secondary: Color,
    val tertiary: Color,
    val background: Color,
    val surface: Color,
    val onPrimary: Color,
    val onSecondary: Color,
    val onTertiary: Color,
    val onBackground: Color,
    val onSurface: Color
)

/**
 * Object responsible for generating a ColorTheme from a single base Compose Color.
 */
object ColorThemeGenerator {

    // Use standard Compose Colors
    private val COLOR_WHITE: Color = Color.Companion.White
    private val COLOR_BLACK: Color = Color.Companion.Black

    /**
     * Generates a ColorTheme based on the provided baseColor.
     *
     * @param baseColor The input Compose Color to generate the theme from.
     * @return A ColorTheme object containing the generated palette.
     */
    fun generateTheme(baseColor: Color): ColorTheme {
        val primary = baseColor
        val baseHsl = rgbToHsl(primary)

        val primaryVariant = adjustHsl(primary, lightnessFactor = 0.8f)

        val secondaryHue = (baseHsl[0] + 30f / 360f) % 1.0f
        val secondary = hslToRgb(
            hsl = floatArrayOf(
                secondaryHue,
                baseHsl[1].coerceIn(0.3f, 0.8f),
                baseHsl[2].coerceIn(0.4f, 0.8f)
            ),
            alpha = baseColor.alpha
        )

        val tertiaryHue = (baseHsl[0] + 60f / 360f) % 1.0f
        val tertiary = hslToRgb(
            hsl = floatArrayOf(
                tertiaryHue,
                baseHsl[1].coerceIn(0.3f, 0.7f),
                baseHsl[2].coerceIn(0.4f, 0.7f)
            ),
            alpha = baseColor.alpha
        )

        val background = desaturateAndLighten(primary, targetSaturation = 0.05f, targetLightness = 0.98f)

        val surface = desaturateAndLighten(primary, targetSaturation = 0.1f, targetLightness = 0.94f)

        val onPrimary = getContrastingColor(primary)
        val onSecondary = getContrastingColor(secondary)
        val onTertiary = getContrastingColor(tertiary)
        val onBackground = getContrastingColor(background)
        val onSurface = getContrastingColor(surface)

        return ColorTheme(
            primary = primary,
            primaryVariant = primaryVariant,
            secondary = secondary,
            tertiary = tertiary,
            background = background,
            surface = surface,
            onPrimary = onPrimary,
            onSecondary = onSecondary,
            onTertiary = onTertiary,
            onBackground = onBackground,
            onSurface = onSurface
        )
    }

    /**
     * Calculates a contrasting color (black or white) based on the perceived luminance.
     *
     * @param color The background Compose Color.
     * @return Either Color.Black or Color.White.
     */
    private fun getContrastingColor(color: Color): Color {
        val red = color.red
        val green = color.green
        val blue = color.blue

        fun adjustGamma(c: Float): Float {
            return if (c <= 0.04045f) c / 12.92f else ((c + 0.055f) / 1.055f).pow(2.4f)
        }
        val lumR = adjustGamma(red)
        val lumG = adjustGamma(green)
        val lumB = adjustGamma(blue)
        val luminance = 0.2126f * lumR + 0.7152f * lumG + 0.0722f * lumB

        return if (luminance > 0.5f) COLOR_BLACK else COLOR_WHITE
    }

    /**
     * Adjusts the saturation and/or lightness of a Compose Color using HSL space.
     *
     * @param color The input Compose Color.
     * @param saturationFactor Optional: Factor to multiply saturation by.
     * @param lightnessFactor Optional: Factor to multiply lightness by.
     * @return The adjusted Compose Color.
     */
    private fun adjustHsl(color: Color, saturationFactor: Float? = null, lightnessFactor: Float? = null): Color {
        val hsl = rgbToHsl(color)
        val h = hsl[0]
        var s = hsl[1]
        var l = hsl[2]

        if (saturationFactor != null) {
            s = (s * saturationFactor).coerceIn(0f, 1f)
        }
        if (lightnessFactor != null) {
            l = (l * lightnessFactor).coerceIn(0f, 1f)
        }

        return hslToRgb(floatArrayOf(h, s, l), color.alpha)
    }

    /**
     * Creates a version of the color with specific target saturation and lightness.
     *
     * @param color The input Compose Color.
     * @param targetSaturation The desired saturation level (0.0 to 1.0).
     * @param targetLightness The desired lightness level (0.0 to 1.0).
     * @return The adjusted Compose Color.
     */
    private fun desaturateAndLighten(color: Color, targetSaturation: Float, targetLightness: Float): Color {
        val hsl = rgbToHsl(color)
        val h = hsl[0]
        val s = targetSaturation.coerceIn(0f, 1f)
        val l = targetLightness.coerceIn(0f, 1f)
        return hslToRgb(floatArrayOf(h, s, l), color.alpha)
    }

    /**
     * Converts a Compose Color (RGB) to HSL color space.
     * Output HSL values: H (0-1), S (0-1), L (0-1).
     */
    private fun rgbToHsl(color: Color): FloatArray {
        val r = color.red
        val g = color.green
        val b = color.blue

        val max = max(r, max(g, b))
        val min = min(r, min(g, b))
        val delta = max - min

        var h = 0f
        var s = 0f
        val l = (max + min) / 2f

        if (delta > 1e-6f) {
            s = if (l < 0.5f) delta / (max + min) else delta / (2f - max - min)
            h = when (max) {
                r -> (g - b) / delta + (if (g < b) 6f else 0f)
                g -> (b - r) / delta + 2f
                else -> (r - g) / delta + 4f
            }
            h /= 6f
            if (h < 0f) h += 1f
            if (h >= 1f) h -= 1f
        }

        return floatArrayOf(h, s, l)
    }

    /**
     * Converts HSL color space values to a Compose Color (RGB).
     * Input HSL values: H (0-1), S (0-1), L (0-1).
     * Alpha is passed separately (0-1).
     */
    private fun hslToRgb(hsl: FloatArray, alpha: Float): Color {
        val h = hsl[0]
        val s = hsl[1]
        val l = hsl[2]

        val r: Float
        val g: Float
        val b: Float

        if (s < 1e-6f) {
            r = l; g = l; b = l
        } else {
            val q = if (l < 0.5f) l * (1 + s) else l + s - l * s
            val p = 2 * l - q
            r = hueToRgbComponent(p, q, h + 1f / 3f)
            g = hueToRgbComponent(p, q, h)
            b = hueToRgbComponent(p, q, h - 1f / 3f)
        }

        return Color(
            red = r.coerceIn(0f, 1f),
            green = g.coerceIn(0f, 1f),
            blue = b.coerceIn(0f, 1f),
            alpha = alpha.coerceIn(0f, 1f)
        )
    }

    private fun hueToRgbComponent(p: Float, q: Float, t: Float): Float {
        var tempT = t
        if (tempT < 0f) tempT += 1f
        if (tempT > 1f) tempT -= 1f
        return when {
            tempT < 1f / 6f -> p + (q - p) * 6f * tempT
            tempT < 1f / 2f -> q
            tempT < 2f / 3f -> p + (q - p) * (2f / 3f - tempT) * 6f
            else -> p
        }
    }

    private fun Float.pow(n: Float): Float = this.toDouble().pow(n.toDouble()).toFloat()
}