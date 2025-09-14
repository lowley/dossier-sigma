package lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import lorry.folder.items.dossiersigma.headless.domain.ColoredTag
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity
import lorry.folder.items.dossiersigma.ui.sigma.SigmaViewModel
import java.util.UUID

// Outil unique avec icône, texte, et un comportement.
data class Tool(
    val text: () -> String,
    @DrawableRes val icon: Int,
    val isColoredIcon: Boolean = false,
    val onClick: suspend Tool.(SigmaViewModel, SigmaActivity) -> Unit,
    val visible: suspend (SigmaViewModel, SigmaActivity) -> Boolean = { _, _ -> true },
    val tint: Color? = null,
    val id: UUID = UUID.randomUUID(),
    val activated: Boolean = false
) {
    fun isActivated() = activated

}

fun Tool.toColoredTag(viewModel: SigmaViewModel? = null): ColoredTag = ColoredTag(
    id = this.id,
    title = this.text(),
    color = this.tint ?: Color.Companion.Unspecified,
)
