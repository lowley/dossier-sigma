package lorry.folder.items.dossiersigma.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import lorry.folder.items.dossiersigma.ui.theme.DossierSigmaTheme
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import dagger.hilt.android.AndroidEntryPoint
import lorry.folder.items.dossiersigma.ui.components.ItemComponent

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: SigmaViewModel by viewModels()

        setContent {
            DossierSigmaTheme {
                //barre d'outils

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    val folderState = viewModel.folder.collectAsState()
                    Text(text = folderState.value.path)

                    LazyRow(
                        modifier = Modifier
                    ) {
                        if (folderState.value.items.isNotEmpty())
                            items(folderState.value.items) { item ->
                                ItemComponent(item)
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