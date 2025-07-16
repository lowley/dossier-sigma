package lorry.folder.items.dossiersigma.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import lorry.folder.items.dossiersigma.ui.SigmaActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@Composable
fun SigmaActivity.settingsPage(
    vm: SettingsViewModel

) {
    val nasAddressFromDataStore by vm.settingsManager.nasAddressFlow.collectAsState("")
    var nasAddress by remember(nasAddressFromDataStore) {
        mutableStateOf(nasAddressFromDataStore)
    }

    val nasLoginFromDataStore by vm.settingsManager.nasLoginFlow.collectAsState("")
    var nasLogin by remember(nasLoginFromDataStore) {
        mutableStateOf(nasLoginFromDataStore)
    }

    val nasPasswordFromDataStore by vm.settingsManager.nasPasswordFlow.collectAsState("")
    var nasPassword by remember(nasPasswordFromDataStore) {
        mutableStateOf(nasPasswordFromDataStore)
    }

    val nasFolderFromDataStore by vm.settingsManager.nasFolderFlow.collectAsState("")
    var nasFolder by remember(nasFolderFromDataStore) {
        mutableStateOf(nasFolderFromDataStore)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        //données
        Row(){
            val rowHeight = 50.dp

            //titres
            Column(
                modifier = Modifier,

            ) {
                Text(
                    modifier = Modifier.height(rowHeight),
                    text = "Addresse")
                Text(
                    modifier = Modifier.height(rowHeight),
                    text = "Login")
                Text(
                    modifier = Modifier.height(rowHeight),
                    text = "Mot de passe")
                Text(
                    modifier = Modifier.height(rowHeight),
                    text = "Répertoire")
            }

            //champs de saisie
            Column(
                modifier = Modifier

            ) {
                TextField(
                    modifier = Modifier.height(rowHeight),
                    value = nasAddress,
                    onValueChange = {
                        nasAddress = it
                    }

                )

                TextField(
                    modifier = Modifier.height(rowHeight),
                    value = nasLogin,
                    onValueChange = {
                        nasLogin = it
                    }

                )

                TextField(
                    modifier = Modifier.height(rowHeight),
                    value = nasPassword,
                    onValueChange = {
                        nasPassword = it
                    }

                )

                TextField(
                    modifier = Modifier.height(rowHeight),
                    value = nasFolder,
                    onValueChange = {
                        nasFolder = it
                    }
                )
            }
        }

        Spacer(
            modifier = Modifier
                .weight(1f)
        )

        //boutons, en bas de la page
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {

            Spacer(
                modifier = Modifier
                    .weight(1f)
            )

            Button(
                modifier = Modifier
                    .padding(end = 20.dp, bottom = 10.dp),
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = Color.Red,
                    contentColor = Color.Black
                ),
                onClick = {
                    mainViewModel.setIsSettingsPageVisible(false)
                }
            ) {
                Text(text = "Annuler")
            }

            Button(
                modifier = Modifier
                    .padding(end = 20.dp, bottom = 10.dp),
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = Color.Red,
                    contentColor = Color.Black
                ),
                onClick = {
                    vm.viewModelScope.launch{
                        settingsViewModel.settingsManager.saveNasAddress(nasAddress)
                        settingsViewModel.settingsManager.saveNasLogin(nasLogin)
                        settingsViewModel.settingsManager.saveNasPassword(nasPassword)
                        settingsViewModel.settingsManager.saveNasFolder(nasFolder)
                    }

                    //sauver dans les opréférences

                    mainViewModel.setIsSettingsPageVisible(false)
                }
            ) {
                Text(text = "Enregistrer")
            }
        }
    }
}