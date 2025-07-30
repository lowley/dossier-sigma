package lorry.folder.items.dossiersigma.ui.sigma

import android.graphics.Paint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.R

@Composable
context(RowScope)
fun SortingArea(
    modifier: Modifier,
    sortingFlow: StateFlow<SortingCriterion>,
    onDateSortClick: () -> Unit,
    onNameSortClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .padding(0.dp)
            .wrapContentWidth()
            .height(30.dp)
            .align(Alignment.CenterVertically),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val sorting by sortingFlow.collectAsState()

        if (sorting == SortingCriterion.ByDateDesc)
            FilterChip(
                label = { Text("Date") },
                modifier = Modifier,
                leadingIcon = {
                    Icon(
                        painterResource(id = R.drawable.trier_decroissant),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.Red
                    )
                },
                enabled = true,
                onClick = onNameSortClick,
                selected = false
            )

        if (sorting == SortingCriterion.ByNameAsc)
            FilterChip(
                label = { Text("Nom") },
                modifier = Modifier,
//                    .align(Alignment.CenterVertically),
                selected = false,
                leadingIcon = {
                    Icon(
                        painterResource(id = R.drawable.trier_croissant),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.Red
                    )

                },
                enabled = true,
                onClick = onDateSortClick
            )
    }
}