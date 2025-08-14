package lorry.folder.items.dossiersigma.ui.browser.utilities

sealed class BrowserTarget(
    val url: String,
    val prepareSearchText: (itemName: String) -> String = { it }
) {
    object GOOGLE : BrowserTarget(
        url = "https://www.google.com/search?q="
    )

    object IAFD_PERSON : BrowserTarget(
        url = "https://www.iafd.com/results.asp?searchtype=comprehensive&searchstring="
    )

    object IAFD_MOVIE : BrowserTarget(
        url = "https://www.iafd.com/results.asp?searchtype=comprehensive&searchstring=",
        prepareSearchText = { it.replace(".mp4", "").substringBefore("by") })
}