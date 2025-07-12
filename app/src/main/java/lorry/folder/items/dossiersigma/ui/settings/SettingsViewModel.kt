package lorry.folder.items.dossiersigma.ui.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.domain.usecases.homePage.SettingDatas
import javax.inject.Inject
import javax.inject.Singleton

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val settingsManager: SettingsManager
): ViewModel() {


}