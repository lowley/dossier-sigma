package lorry.folder.items.dossiersigma.ui.fullSizeDialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import lorry.folder.items.dossiersigma.ui.sigma.SigmaViewModel

@Composable
fun CustomYesNoDialog(
    text: String,
    viewModel: SigmaViewModel,
    onOk: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier.Companion
            .fillMaxSize()
            .background(
                color = contentColorFor(Color.Companion.White)
                    .copy(alpha = 0.6f)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    viewModel.setIsYesNoDialogVisible(false)
                }
            ),
        contentAlignment = Alignment.Companion.Center
    ) {
        Column(
            modifier = Modifier.Companion
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                .background(Color.Companion.White)
                .padding(8.dp),
        ) {

            Text(
                modifier = Modifier.Companion,
                text = text,
                color = Color.Companion.Black
            )

            Spacer(modifier = Modifier.Companion.height(8.dp))

            Row(
                modifier = Modifier.Companion.align(Alignment.Companion.End)
            ) {
                Button(
                    onClick = {
                        viewModel.setIsYesNoDialogVisible(false)
                        onOk(false)
                    }
                ) {
                    Text("Non")
                }

                Spacer(modifier = Modifier.Companion.width(8.dp))

                Button(
                    onClick = {
                        viewModel.setIsYesNoDialogVisible(false)
                        onOk(true)
                    }
                ) {
                    Text("Oui")
                }
            }
        }
    }
}
