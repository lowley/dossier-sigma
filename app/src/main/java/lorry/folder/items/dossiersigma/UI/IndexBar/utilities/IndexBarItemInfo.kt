package lorry.folder.items.dossiersigma.UI.IndexBar.utilities

import java.time.LocalDate

data class IndexBarItemInfo (
    val contextualHelp: String,
    val infoType: InfoType = InfoType.MAJOR,
    val content: Content,
    val endDate: LocalDate? = null
)

enum class InfoType{
    MAJOR,
    MINOR
}

sealed class Content(){
    data class Text(val text: String): Content()
    data class Icon(val icon: Int) : Content()
}