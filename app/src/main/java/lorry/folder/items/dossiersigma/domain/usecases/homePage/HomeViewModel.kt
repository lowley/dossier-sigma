package lorry.folder.items.dossiersigma.domain.usecases.homePage

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.domain.Item
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val homeUseCase: HomeUseCase
): ViewModel(){

    private val _homePageVisible = MutableStateFlow<Boolean>(false)
    val homePageVisible: StateFlow<Boolean> = _homePageVisible
    
    fun setHomePageVisible(visible: Boolean) {
        _homePageVisible.value = visible
    }
    
    
}