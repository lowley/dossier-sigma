package lorry.folder.items.dossiersigma.ui.components

import android.provider.CalendarContract
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.R
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Color

@Composable
public fun ItemComponent(item: Item) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .height(150.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(1f),
                painter = if (item.isFile) painterResource(R.drawable.file)
                else painterResource(R.drawable.folder),
                contentDescription = null
            )
        }
        Text(
            modifier = Modifier
                .align(alignment = CenterHorizontally),
            color = Color.Black,
            text = item.name
        )
    }
}