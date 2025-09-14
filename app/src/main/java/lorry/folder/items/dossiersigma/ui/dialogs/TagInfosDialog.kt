package lorry.folder.items.dossiersigma.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewModelScope
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity
import lorry.folder.items.dossiersigma.ui.sigma.SigmaViewModel


@Composable
fun TagInfosDialog(
    text: String,
    onDatasCompleted:
    suspend (tagInfos: TagInfos?, viewModel: SigmaViewModel, activity: SigmaActivity) -> Unit,
    viewModel: SigmaViewModel,
    mainActivity: SigmaActivity
) {
    val editMessage = remember { mutableStateOf("") }
    var hexColor by remember { mutableStateOf<String?>(null) }

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
                    viewModel.setIsTagInfosDialogVisible(false)
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

            //couleur + titre
            Box(
                modifier = Modifier.Companion
                    .size(200.dp)
                    .align(Alignment.Companion.CenterHorizontally)
            ) {
                val controller = rememberColorPickerController()

                HsvColorPicker(
                    modifier = Modifier.Companion
                        .fillMaxSize(),
                    controller = controller,
                    onColorChanged = { colorEnvelope: ColorEnvelope ->
                        val hexCode: String = colorEnvelope.hexCode
                        hexColor = hexCode
                    }
                )
            }

            Spacer(modifier = Modifier.Companion.height(8.dp))

            TextField(
                modifier = Modifier.Companion
                    .fillMaxWidth(),
                value = editMessage.value,
                onValueChange = { value: String -> editMessage.value = value },
                singleLine = true,
                label = { Text("Titre du drapeau") }
            )

            Spacer(modifier = Modifier.Companion.height(8.dp))

            Row(
                modifier = Modifier.Companion
            ) {
                Spacer(
                    modifier = Modifier.Companion
                        .weight(1f)
                )

                Button(
                    modifier = Modifier.Companion,
                    onClick = {
                        viewModel.setIsTagInfosDialogVisible(false)
                        viewModel.viewModelScope.launch {
                            onDatasCompleted(null, viewModel, mainActivity)
                        }
                    }
                ) {
                    Text("Annuler")
                }

                Button(
                    modifier = Modifier.Companion,
                    onClick = {
                        if (hexColor != null && editMessage.value != "")
                            viewModel.viewModelScope.launch {
                                onDatasCompleted(
                                    TagInfos(
                                        title = editMessage.value,
                                        Color("#$hexColor".toColorInt()),
                                    ), viewModel, mainActivity
                                )
                            }

                        viewModel.setIsTagInfosDialogVisible(false)
                    }
                ) {
                    Text("Valider")
                }
            }
        }
    }
}

data class TagInfos(
    val title: String,
    val color: Color
)