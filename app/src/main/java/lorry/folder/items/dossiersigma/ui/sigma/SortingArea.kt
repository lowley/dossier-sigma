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
    sortingFlow: StateFlow<ITEMS_ORDERING_STRATEGY>,
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
            selected = sorting == ITEMS_ORDERING_STRATEGY.DATE_DESC,
            leadingIcon = {
                if (sorting == ITEMS_ORDERING_STRATEGY.DATE_DESC)
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
            enabled = sorting == ITEMS_ORDERING_STRATEGY.NAME_ASC,
            onClick = onDateSortClick
        )

        FilterChip(
            label = { Text("Nom") },
            modifier = Modifier
                .align(Alignment.CenterVertically),
            selected = sorting == ITEMS_ORDERING_STRATEGY.NAME_ASC,
            leadingIcon = {
                if (sorting == ITEMS_ORDERING_STRATEGY.NAME_ASC)
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
            enabled = sorting == ITEMS_ORDERING_STRATEGY.DATE_DESC,
            onClick = onNameSortClick
        )
    }
}