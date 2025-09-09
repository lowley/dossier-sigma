@file:Suppress("KotlinConstantConditions")

package lorry.folder.items.dossiersigma.ui.tinies

import android.graphics.Shader
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import android.graphics.RenderEffect as AndroidRenderEffect

@Composable
fun MorphingIcon(
    @DrawableRes current: Int,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    durationMs: Int = 450,
    easing: Easing = FastOutSlowInEasing,
    enableBlur: Boolean = true,           // désactive le flou si besoin (utile < API 31)
    baseBlurDp: Dp = 20.dp,
    tint: Color = Color.Unspecified,               // intensité max du flou (en dp)
) {
    // 1) Conserver l’ancienne icône jusqu’à la fin de l’anim
    var previous by remember { mutableIntStateOf(current) }
    val progress = remember { Animatable(1f) } // 1f = état stable (pas d’anim en cours)

    LaunchedEffect(current) {
        if (current != previous) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = durationMs, easing = easing)
            )
            previous = current
        }
    }

    // 2) Painters — pas de remember autour, painterResource est déjà optimisé
    val painterPrev = painterResource(previous)
    val painterCurr = painterResource(current)

    // 3) Calcul du flou en pixels
    val density = LocalDensity.current
    val baseBlurPx = with(density) { baseBlurDp.toPx() }
    val blurPrev = baseBlurPx * progress.value
    val blurCurr = baseBlurPx * (1f - progress.value)

    val cf = if (tint != Color.Unspecified) ColorFilter.tint(tint) else null

    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        // Image A (ancienne)
        Image(
            painter = painterPrev,
            contentDescription = null,
            colorFilter = cf,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    // fondu sortant + micro-zoom out
                    alpha = 1f - progress.value
                    val s = 1f - 0.03f * progress.value
                    scaleX = s; scaleY = s

                    // Flou progressif si dispo
                    renderEffect =
                        if (enableBlur) {
                            AndroidRenderEffect
                                .createBlurEffect(blurPrev, blurPrev, Shader.TileMode.CLAMP)
                                .asComposeRenderEffect()
                        } else null
                }
        )

        // Image B (nouvelle)
        Image(
            painter = painterCurr,
            contentDescription = null,
            colorFilter = cf,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    // fondu entrant + micro-zoom in
                    alpha = progress.value
                    val s = 0.97f + 0.03f * progress.value
                    scaleX = s; scaleY = s

                    renderEffect =
                        if (enableBlur) {
                            AndroidRenderEffect
                                .createBlurEffect(blurCurr, blurCurr, Shader.TileMode.CLAMP)
                                .asComposeRenderEffect()
                        } else null
                }
        )
    }
}
