package lorry.folder.items.dossiersigma.domain.usecases.browser

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.domain.interfaces.IClipboardRepository
import javax.inject.Inject

class BrowserUseCase @Inject constructor(
    val context: Context,
) {
    private val _currentPage = MutableStateFlow<String?>(null)
    val currentPage: StateFlow<String?> = _currentPage

    fun setCurrentPage(page: String?) {
        _currentPage.value = page
    }

    fun openBrowserWith(html: String) {
        _currentPage.value = html
    }

    fun closeBrowser() {
        _currentPage.value = null
    }

    fun openBrowser(item: Item, target: BrowserTarget) {
        val coreName = item.name.substringBeforeLast(".")

        val prepared1 = target
            .prepareSearchText(coreName)
            .split(' ')
            .filter {
                it.isNotEmpty()
//                    && !it.matches(Regex("^\\(\\d{4}\\)$"))
            }

        val byPart = prepared1.indexOfFirst { it == "by" }

        val prepared2 = if (byPart != -1) {
            val tiretIndex = prepared1.indexOfFirst {
                it == "-" && prepared1.indexOf(it) > byPart
            }
            prepared1
                .take(tiretIndex)
                .filter { it != "by" }
        } else prepared1

        val prepared3 = prepared2
            .joinToString("+") {
                it.replace("(", "")
                    .replace(")", "")
            }

        setCurrentPage(target.url + prepared3)

        Toast.makeText(context, "Naviguez et appuyez longuement sur l'image choisie", Toast.LENGTH_LONG)
            .show()
    }
}

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

