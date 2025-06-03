package lorry.folder.items.dossiersigma.domain.usecases.browser

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.domain.interfaces.IClipboardRepository
import lorry.folder.items.dossiersigma.ui.SigmaViewModel
import javax.inject.Inject

class BrowserUseCase @Inject constructor(
    val context: Context,
    val clipboardRepository: IClipboardRepository
) {
    private val _isBrowserVisible = MutableStateFlow(false)
    val isBrowserVisible: StateFlow<Boolean> = _isBrowserVisible
    
    private val _isGoogle = MutableStateFlow(false)
    val isGoogle: StateFlow<Boolean> = _isGoogle

    private val _browserSearch = MutableStateFlow("")
    val browserSearch: StateFlow<String> = _browserSearch
    
    private val _searchIsForPersonNotMovies = MutableStateFlow(true)
    val searchIsForPersonNotMovies: StateFlow<Boolean> = _searchIsForPersonNotMovies

    fun showBrowser() {
        _isBrowserVisible.value = true
    }

    fun hideBrowser() {
        _isBrowserVisible.value = false
    }
    
    fun setIsGoogle(isGoogle: Boolean) {
        _isGoogle.value = isGoogle
    }
    
    fun setBrowserPersonSearch(search: String) {
        _browserSearch.value = search
        _searchIsForPersonNotMovies.value = true
    }

    fun setBrowserMovieSearch(search: String) {
        _browserSearch.value = search
        _searchIsForPersonNotMovies.value = false
    }
    
    fun openBrowser(item: Item, viewModel: SigmaViewModel) {
        val preparedKey = item.name.split('.').last().split(' ').joinToString("+")
        viewModel.setSelectedItem(item)

        if (item.name.endsWith(".mp4"))
            setBrowserMovieSearch(item.name.replace(".mp4", "").substringBefore("by"))
        else setBrowserPersonSearch(preparedKey)

        showBrowser()
        Toast.makeText(context, "Naviguez et appuyez longuement sur l'image choisie", Toast.LENGTH_LONG).show()
    }
}