package lorry.folder.items.dossiersigma.ui.sigma

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
context(BoxScope)
fun SortingArea(
    sortingWidth: Dp,
    sortingFlow: StateFlow<SortingCriterion>,
    onDateSortClick: () -> Unit,
    onNameSortClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .width(sortingWidth),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val sorting by sortingFlow.collectAsState()

        FilterChip(
            label = { Text("Date") },
            modifier = Modifier
                .padding(end = 5.dp)
                .align(Alignment.CenterVertically),
            selected = sorting == SortingCriterion.ByDateDesc,
            leadingIcon = {
                if (sorting == SortingCriterion.ByDateDesc)
                    Icon(
                        painterResource(id = R.drawable.trier_decroissant),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.Red
                    )
                else
                    Icon(
                        painterResource(id = R.drawable.trier_decroissant),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
            },
            enabled = sorting == SortingCriterion.ByNameAsc,
            onClick = onDateSortClick
        )

        FilterChip(
            label = { Text("Nom") },
            modifier = Modifier
                .align(Alignment.CenterVertically),
            selected = sorting == SortingCriterion.ByNameAsc,
            leadingIcon = {
                if (sorting == SortingCriterion.ByNameAsc)
                    Icon(
                        painterResource(id = R.drawable.trier_croissant),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.Red
                    )
                else
                    Icon(
                        painterResource(id = R.drawable.trier_croissant),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
            },
            enabled = sorting == SortingCriterion.ByDateDesc,
            onClick = onNameSortClick
        )
    }
}