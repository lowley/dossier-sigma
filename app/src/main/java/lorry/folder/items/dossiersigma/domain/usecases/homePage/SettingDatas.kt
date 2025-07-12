package lorry.folder.items.dossiersigma.domain.usecases.homePage

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingDatas @Inject constructor(

) {

    val _nasAddress = MutableStateFlow<String?>("")
    val nasAddress: StateFlow<String?> = _nasAddress

    fun setNasAddress(address: String) {
        _nasAddress.value = address
    }

    val _nasLogin = MutableStateFlow<String?>("")
    val nasLogin: StateFlow<String?> = _nasLogin

    fun setNasLogin(login: String?) {
        _nasLogin.value = login
    }

    val _nasPassword = MutableStateFlow<String?>("")
    val nasPassword: StateFlow<String?> = _nasPassword

    fun setNasPassword(password: String?) {
        _nasPassword.value = password
    }

    val _nasDirectory = MutableStateFlow<String?>("")
    val nasDirectory: StateFlow<String?> = _nasDirectory

    fun setNasDirectory(directory: String?) {
        _nasDirectory.value = directory
    }
}