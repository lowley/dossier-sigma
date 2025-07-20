package lorry.folder.items.dossiersigma.ui.sigma

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
context(BoxScope, SigmaActivity)
fun ToolRow(){
    Text(
       modifier = Modifier
           .align(Alignment.Center),
        text = mainViewModel.dialogMessage.value ?: ""
    )
}