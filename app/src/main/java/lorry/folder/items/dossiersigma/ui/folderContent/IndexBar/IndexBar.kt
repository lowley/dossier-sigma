package lorry.folder.items.dossiersigma.ui.folderContent.IndexBar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.headless.folderContentBack.IFolderContentBackComponent
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.ui.folderContent.IndexBar.utilities.Content
import lorry.folder.items.dossiersigma.ui.folderContent.IndexBar.utilities.InfoType
import lorry.folder.items.dossiersigma.ui.folderContent.IndexBar.utilities.toIndexBarItemInfoList
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity
import lorry.folder.items.dossiersigma.ui.sigma.SigmaColors
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import kotlin.reflect.KClass

class IndexBar @Inject constructor(
    val folderContentComponent: IFolderContentBackComponent
) : IIndexBar {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val modelFlow = combine(
        folderContentComponent.currentFolderFlow,
        folderContentComponent.sorting
    ) { currentFolder, sorting ->
        currentFolder?.items?.toIndexBarItemInfoList(sorting)
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    context(BoxScope, SigmaActivity)
    override fun display(currentScrollState: LazyGridState) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            val model by modelFlow.collectAsState(emptyList())

            model?.forEach { info ->
                when (val c = info.content) {

                    is Content.Text -> {
                        var tooltipVisible = remember { mutableStateOf(false) }

                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                            tooltip = {
                                PlainTooltip {
                                    Text(info.contextualHelp)
                                }
                            },
                            state = rememberTooltipState(isPersistent = false)
                        ) {
                            val coroutineScope = rememberCoroutineScope()
                            val currentFolderItems = remember {
                                derivedStateOf {
                                    mainViewModel.folderContentComponent
                                        .currentFolderFlow?.value?.items ?: emptyList()
                                }
                            }

//                                mainViewModel.displayedItemsFlow.collectAsState()

                            Text(
                                modifier = Modifier
                                    .clickable {
                                        tooltipVisible.value = !tooltipVisible.value

                                        val items = currentFolderItems.value
                                            .sortedBy { it.name }

                                        items.forEachIndexed { index, item ->
                                            val itemFirstCharacter = item.name.first().uppercase()

                                            if (itemFirstCharacter.equals(info.content.text)) {
                                                coroutineScope.launch {
                                                    currentScrollState.animateScrollToItem(
                                                        index
                                                    )
                                                }

                                                return@clickable
                                            }
                                        }
                                    },
                                text = info.content.text,
                                color = SigmaColors.current.secondary
//                                color = if (info.infoType == InfoType.MAJOR) SigmaColors.current.tertiary else SigmaColors.current.secondary
                            )

                        }
                    }

                    is Content.Icon -> {
                        var tooltipVisible = remember { mutableStateOf(false) }

                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                            tooltip = {
                                PlainTooltip {
                                    Text(info.contextualHelp)
                                }
                            },
                            state = rememberTooltipState(isPersistent = false)
                        ) {
                            val coroutineScope = rememberCoroutineScope()
                            val currentFolderItems = remember {
                                derivedStateOf {
                                    mainViewModel.folderContentComponent
                                        .currentFolderFlow.value?.items ?: emptyList()
                                }
                            }

                            Box(

                            ) {
                                val numbersRegex = Regex("""(\d+)\h*-> (\d+)""")
                                val matchNumbers = numbersRegex.find(info.contextualHelp)
                                val firstNumber = (matchNumbers?.groupValues[1] ?: "").toInt()
                                val secondNumber = (matchNumbers?.groupValues[2] ?: "").toInt()

                                val monthRegex = Regex("""([a-z]+)\s""")
                                val matchMonth = monthRegex.find(info.contextualHelp)
                                val month = matchMonth?.groupValues[1]?.let {
                                    val result = when (it) {
                                        "janvier" -> 1
                                        "février" -> 2
                                        "mars" -> 3
                                        "avril" -> 4
                                        "mai" -> 5
                                        "juin" -> 6
                                        "juillet" -> 7
                                        "août" -> 8
                                        "septembre" -> 9
                                        "octobre" -> 10
                                        "novembre" -> 11
                                        "décembre" -> 12
                                        else -> 1
                                    }?.let {

                                        //fin de mois
                                        if (firstNumber > secondNumber)
                                            Mois.get(it).successor
                                        else Mois.get(it).current
                                    }

                                    result
                                } ?: "???"


                                val iconContent =
                                    if (info.infoType == InfoType.MAJOR) month
                                    else firstNumber.toString()


                                val color =
                                    if (info.infoType == InfoType.MAJOR) SigmaColors.current.secondary
                                    else SigmaColors.current.tertiary


                                Icon(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(if (info.infoType == InfoType.MAJOR) 20.dp else 15.dp)
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onTap = {
                                                    tooltipVisible.value = !tooltipVisible.value

                                                    require(info.endDate != null)
                                                    val items = currentFolderItems.value
                                                        .sortedBy { it.modificationDate }
                                                        .reversed()

                                                    val zone = ZoneId.systemDefault()

                                                    items.forEachIndexed { index, item ->
                                                        val itemDate =
                                                            Instant.ofEpochMilli(item.modificationDate)
                                                                .atZone(ZoneId.systemDefault())
                                                                .toLocalDate()
                                                        if (itemDate.equals(info.endDate) ||
                                                            itemDate.isBefore(info.endDate)
                                                        ) {
                                                            coroutineScope.launch {
                                                                currentScrollState.animateScrollToItem(
                                                                    index
                                                                )
                                                            }
                                                            return@detectTapGestures
                                                        }
                                                    }
                                                })
                                        },
                                    painter = painterResource(id = R.drawable.rond),
                                    tint = if (info.infoType == InfoType.MAJOR) SigmaColors.current.tertiary else SigmaColors.current.secondary,
                                    contentDescription = null
                                )

                                Text(
                                    modifier = Modifier
                                        .align(Alignment.Center),
                                    text = iconContent,
                                    fontSize = 8.sp,
                                    color = color
                                )
                            }
                        }
                    }

                    else -> Unit
                }
            }
        }
    }
}

sealed class Mois(
    val current: String,
    val successor: String,
    val index: Int
){
    data object Janvier: Mois("Jan", "Fév", 1)
    data object Février: Mois("Fév", "Mar", 2)
    data object Mars: Mois("Mar", "Avr", 3)
    data object Avril: Mois("Avr", "Mai", 4)
    data object Mai: Mois("Mai", "Jun", 5)
    data object Juin: Mois("Jun", "Jul", 6)
    data object Juillet: Mois("Jul", "Aoû", 7)
    data object Août: Mois("Aoû", "Sep", 8)
    data object Septembre: Mois("Sep", "Oct", 9)
    data object Octobre: Mois("Oct", "Nov", 10)
    data object Novembre: Mois("Nov", "Déc", 11)
    data object Décembre: Mois("Déc", "Jan", 12)

    companion object{
        operator fun get(index: Int): Mois {
            assert(index in 1..12)
            val tousLesMois = allObjectInstances<Mois>()
            return tousLesMois.first { it.index == index }
        }
    }
}



inline fun <reified T : Any> allObjectInstances(): List<T> = allObjectInstances(T::class)

fun <T : Any> allObjectInstances(base: KClass<T>): List<T> {
    fun collect(k: KClass<out T>): List<T> {
        // instance si c'est un object (data object inclus)
        val self = k.objectInstance?.let { listOf(it) } ?: emptyList()

        // descendre si c'est scellé (pour couvrir les sous-classes scellées)
        val children = if (k.isSealed) {
            k.sealedSubclasses.flatMap { collect(it) }
        } else emptyList()

        return self + children
    }
    return collect(base)
}