package lorry.folder.items.dossiersigma.ui.centralArea

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.ui.sigma.SigmaColors
import lorry.folder.items.dossiersigma.ui.sigma.SortingCriterion

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
            .align(Alignment.Companion.CenterVertically),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Companion.CenterVertically
    ) {
        val sorting by sortingFlow.collectAsState()

        if (sorting == SortingCriterion.ByDateDesc)
            FilterChip2(
                colors = FilterChipDefaults.filterChipColors().copy(
                    containerColor = SigmaColors.current.primary,
                    labelColor = SigmaColors.current.tertiary,
                ),
                label = { Text("Date") },
                modifier = Modifier.Companion,
                leadingIcon = {
                    Icon(
                        painterResource(id = R.drawable.trier_decroissant),
                        contentDescription = null,
                        modifier = Modifier.Companion.size(24.dp),
                        tint = SigmaColors.current.tertiary
                    )
                },
                enabled = true,
                onClick = onNameSortClick,
                selected = false
            )

        if (sorting == SortingCriterion.ByNameAsc)
            FilterChip2(
                colors = FilterChipDefaults.filterChipColors().copy(
                    containerColor = SigmaColors.current.primary,
                    labelColor = SigmaColors.current.tertiary,
                ),
                label = { Text("Nom") },
                modifier = Modifier.Companion,
//                    .align(Alignment.CenterVertically),
                selected = false,
                leadingIcon = {
                    Icon(
                        painterResource(id = R.drawable.trier_croissant),
                        contentDescription = null,
                        modifier = Modifier.Companion.size(24.dp),
                        tint = SigmaColors.current.tertiary
                    )

                },
                enabled = true,
                onClick = onDateSortClick
            )
    }
}

@Composable
fun FilterChip2(
    colors: SelectableChipColors,
    label: @Composable () -> Unit,
    modifier: Modifier.Companion,
    leadingIcon: @Composable () -> Unit,
    enabled: Boolean,
    onClick: () -> Unit,
    selected: Boolean
) {

    Box(
        modifier = Modifier
            .wrapContentSize()
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = SigmaColors.current.tertiary,
                shape = RoundedCornerShape(8.dp)
            )
    ){
        Row(
            modifier = Modifier
                .clickable {onClick()}
        ){

            leadingIcon()

            label()
        }
    }
}