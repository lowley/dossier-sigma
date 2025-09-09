package lorry.folder.items.dossiersigma.UI.browser.utilities

sealed class BrowserTarget(
    val url: String,
    val prepareSearchText: (itemName: String) -> String = { it }
) {
    object GOOGLE : BrowserTarget(
        url = "https://www.google.com/search?q="
    )
}