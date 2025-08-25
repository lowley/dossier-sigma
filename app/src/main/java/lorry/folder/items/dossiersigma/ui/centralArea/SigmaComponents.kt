package lorry.folder.items.dossiersigma.ui.centralArea

import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity
import lorry.folder.items.dossiersigma.ui.sigma.SigmaColors
import lorry.folder.items.dossiersigma.ui.sigma.SigmaViewModel

@Composable
context(RowScope)
fun HomeButtonIcon(
    icon : Int,
    onTapAction: (Offset) -> Unit
){
    Icon(
        modifier = Modifier.Companion
            .size(50.dp)
            .padding(
                start = 15.dp,
                end = 5.dp
            )
            .align(Alignment.Companion.CenterVertically)
            .size(50.dp)
            .pointerInput(true) {
                detectTapGestures(
                    onTap = {
                        onTapAction(it)
                    }
                )
            },
        painter = painterResource(icon),
        tint = SigmaColors.current.secondary,
        contentDescription = null
    )
}

context(SigmaActivity)
public fun initializeFileIntentLauncher(viewModel: SigmaViewModel) {
    val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val pathUri = result.data?.data
            viewModel.onFolderSelected(pathUri)
        }
    intentWrapper.setLauncher(launcher as Object)
}