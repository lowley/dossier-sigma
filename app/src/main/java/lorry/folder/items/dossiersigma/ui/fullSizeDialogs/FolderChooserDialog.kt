package lorry.folder.items.dossiersigma.ui.fullSizeDialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.headless.domain.Item
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity
import lorry.folder.items.dossiersigma.ui.sigma.SigmaViewModel
import lorry.folder.items.dossiersigma.ui.sigma.SortingCriterion

@Composable
fun SigmaActivity.FolderChooserDialog(
    modifier: Modifier,
    viewModel: SigmaViewModel,
    onDatasCompleted: (path: String?) -> Unit,
) {


    /////////////////////////////////
    // Code de FolderChooserDialog //
    /////////////////////////////////

    var path = remember { mutableStateOf("/storage/emulated/0") }
    var items = remember { mutableStateOf(listOf<Item>()) }

    LaunchedEffect(path.value) {
        mainViewModel.viewModelScope.launch {
            items.value = mainViewModel.diskRepository.getFolderItems(
                path.value,
                SortingCriterion.ByNameAsc
            )
        }
    }

    Column(
        modifier = modifier
            .width(600.dp)
            .height(400.dp)
            .background(Color.Companion.White)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = Color.Companion.Black,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
    ) {
        FileChooserToolbox(
            path = path,
        )

        Spacer(modifier = Modifier.Companion.height(8.dp))

        SelectedPathDisplay(path = path)

        Spacer(modifier = Modifier.Companion.height(8.dp))

        FileList(
            path = path,
            items = items,
        )

        Spacer(modifier = Modifier.Companion.height(8.dp))

        BottomToolbar2(
            modifier = Modifier.Companion,
            path = path,
            items = items,
            onDatasCompleted = onDatasCompleted,
            viewModel = viewModel
        )
    }
}

@Composable
fun FileChooserToolbox(
    path: MutableState<String>,
) {

    Row(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(top = 10.dp)
    ) {
        Button(
            modifier = Modifier.Companion
                .padding(horizontal = 5.dp)
                .width(IntrinsicSize.Min),
            onClick = {
                path.value = path.value.substringBeforeLast("/")
            }
        ) {
            Text(text = "Remonter")
        }

        Button(
            modifier = Modifier.Companion
                .padding(horizontal = 5.dp)
                .width(IntrinsicSize.Min),
            onClick = {
                path.value = "/storage/emulated/0/Download"
            }
        ) {
            Text(text = "Téléchargements")
        }

        Button(
            modifier = Modifier.Companion
                .padding(horizontal = 5.dp)
                .width(IntrinsicSize.Min),
            onClick = {
                path.value = "/storage/emulated/0"
            }
        ) {
            Text(text = "Stockage principal")
        }

        Button(
            modifier = Modifier.Companion
                .padding(horizontal = 5.dp)
                .width(IntrinsicSize.Min),
            onClick = {
                path.value = "/storage/emulated/0/Movies"
            }
        ) {
            Text(text = "Movies")
        }
    }
}

@Composable
fun ColumnScope.SelectedPathDisplay(
    path: MutableState<String>
) {
    Text(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .align(Alignment.Companion.CenterHorizontally),
        text = path.value.substringAfterLast("/"),
        textAlign = TextAlign.Companion.Center

    )
}

@Composable
fun ColumnScope.BottomToolbar2(
    modifier: Modifier,
    path: MutableState<String>,
    items: MutableState<List<Item>>,
    onDatasCompleted: (path: String?) -> Unit,
    viewModel: SigmaViewModel
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Spacer(
            modifier = Modifier.Companion
                .weight(1f)
        )

        Button(
            onClick = {
                viewModel.setIsFilePickerVisible(false)
            }
        ) {
            Text(text = "Abandonner")
        }

        Button(
            modifier = Modifier.Companion
                .padding(horizontal = 5.dp),
            onClick = {
                onDatasCompleted(path.value)
                viewModel.setIsFilePickerVisible(false)
            }
        ) {
            Text(text = "Choisir ${path.value.substringAfterLast("/").takeLast(20)}")
        }
    }
}

@Composable
fun ColumnScope.FileList(
    path: MutableState<String>,
    items: MutableState<List<Item>>
) {

    LazyColumn(
        modifier = Modifier.Companion
            .weight(1f)
            .padding(horizontal = 20.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = Color.Companion.Gray,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
    ) {
        val isEmpty = items.value.isEmpty()

        if (isEmpty)
            item {
                Box(
                    modifier = Modifier.Companion.fillParentMaxSize(),
                    contentAlignment = Alignment.Companion.Center
                ) {
                    Text(text = "Le répertoire est vide")
                }
            }
        else
            items(items.value.size) { index ->
                val item = items.value[index]

                ItemRow(path = path, item = item)
            }
    }
}

@Composable
fun ColumnScope.ItemRow(
    path: MutableState<String>,
    item: Item
) {

    Row(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .clickable {
                if (item.isFolder())
                    path.value = item.fullPath
            }
    ) {
        AsyncImage(
            modifier = Modifier.Companion
                .size(50.dp)
                .padding(end = 10.dp),
            model = if (item.isFile()) R.drawable.file else R.drawable.folder_empty,
            contentDescription = "Miniature",
            contentScale = ContentScale.Companion.Fit,

            )

        Text(
            modifier = Modifier.Companion
                .align(Alignment.Companion.CenterVertically),
            text = item.name
        )
    }
}
