package lorry.folder.items.dossiersigma.ui.folderContent.toolbar.utils

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp

@Composable
fun StickerIcon(
    modifier: Modifier = Modifier.Companion,
    iconRes: Int,
    ringColor: Color,
    ringWidth: Dp,
    iconTint: Color, // Permet de garder la couleur originale de l'icône
    ringSize: Dp,
    iconSize: Dp,
    isRingEnabled: Boolean,
) {

// Le Box sert de conteneur pour dessiner la bordure autour.
    Box(
        modifier = if (isRingEnabled) modifier
// Étape 1 : Appliquer une bordure.
            .border(
                width = ringWidth,
                color = ringColor,
                shape = CircleShape // Essentiel pour que la bordure soit un anneau.
            )
// Étape 2 : Ajouter un padding INTERNE égal à l'épaisseur de l'anneau.
// Cela "pousse" le contenu (l'icône) vers l'intérieur pour ne pas qu'il soit sous la bordure.
            .padding(ringWidth)
// Étape 3 (Optionnel mais recommandé) : Donner une taille fixe au conteneur.
            .size(ringSize)
        else modifier
            .border(
                width = ringWidth,
                color = Color.Companion.Transparent,
                shape = CircleShape // Essentiel pour que la bordure soit un anneau.
            )
            .padding(ringWidth)
            .size(ringSize),
        contentAlignment = Alignment.Companion.Center // S'assure que l'icône est bien centrée.
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = "Icône avec un anneau",
            // L'icône prend toute la place disponible à l'intérieur du padding.
            modifier = Modifier.Companion.size(iconSize),
            tint = iconTint
        )
    }
}
