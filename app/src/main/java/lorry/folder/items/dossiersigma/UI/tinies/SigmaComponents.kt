package lorry.folder.items.dossiersigma.UI.tinies

import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import lorry.folder.items.dossiersigma.UI.sigma.SigmaActivity
import lorry.folder.items.dossiersigma.UI.sigma.SigmaViewModel

@Composable
context(RowScope)
fun HomeButtonIcon(
    stuff: Pair<Int,Color>,
    onTapAction: (Offset) -> Unit,
){
    MorphingIcon(
        modifier = Modifier
            .pointerInput(true) {
                detectTapGestures(
                    onTap = {
                        onTapAction(it)
                    }
                )
            }
            .padding(
                start = 15.dp,
                end = 5.dp
            )
            .align(Alignment.Companion.CenterVertically),
        current = stuff.first,
        size = 35.dp,
        durationMs = 620,
        tint = stuff.second,
    )

//    Icon(
//        modifier = Modifier.Companion
//            .size(50.dp)
//            .padding(
//                start = 15.dp,
//                end = 5.dp
//            )
//            .align(Alignment.Companion.CenterVertically)
//            .size(50.dp)
//            .pointerInput(true) {
//                detectTapGestures(
//                    onTap = {
//                        onTapAction(it)
//                    }
//                )
//            },
//        painter = painterResource(icon),
//        tint = SigmaColors.current.secondary,
//        contentDescription = null
//    )
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