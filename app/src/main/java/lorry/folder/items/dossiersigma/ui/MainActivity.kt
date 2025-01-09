package lorry.folder.items.dossiersigma.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import lorry.folder.items.dossiersigma.ui.theme.DossierSigmaTheme
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import lorry.folder.items.dossiersigma.PermissionsManager
import lorry.folder.items.dossiersigma.ui.components.ItemComponent

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionsManager = PermissionsManager()
        if (!permissionsManager.hasExternalStoragePermission())
            permissionsManager.requestExternalStoragePermission(this)
        
        val viewModel: SigmaViewModel by viewModels()

        setContent {
            DossierSigmaTheme {
                //barre d'outils
                
                val state = rememberScrollState()
                
                Column(
                    modifier = Modifier
                        .wrapContentWidth()
                        .verticalScroll(state)
                ) {
                    val folderState = viewModel.folder.collectAsState()
                    Text(text = folderState.value.fullPath)

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        
                    ) {
                        if (folderState.value.items.isNotEmpty())
                            folderState.value.items.forEach { item ->
                                ItemComponent(this@MainActivity, viewModel, item)
                            }
                    }
                }
                
            }
        }
    }
}


//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    DossierSigmaTheme {
//        
//    }
//}