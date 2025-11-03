# NOTES

```
👀️ adb install "C:\Users\olivier\progs\dossiersigma.apk"
```

## codes logcat


| code      | signification                                                                                                 |
| --------- | ------------------------------------------------------------------------------------------------------------- |
| PathCrspd | épie path des FolderCacheEntry émises par DaemonService et reçues par <br />FolderContentComponent (flows) |
| folderFlow| état d'entrée de la recombination de folderContentFlow dans FolderContentComponent + décision de récup de données|
|dsplitms   | procédure d'affichage de la liste des items dans itemsPage                                                       |

## goodies

### injecter un viewModel dans un composant (hors @Composable)

dans le constructeur du composant:
```
private val owner: ViewModelStoreOwner
```

puis dans la classe:
```
val memoViewModel: MemoViewModel by lazy {
        ViewModelProvider(owner)[MemoViewModel::class.java]
    }
```

// 1) VM
```
@HiltViewModel
class SigmaViewModel @Inject constructor(
    private val repo: Repo
) : ViewModel() {
    private val _ui = MutableStateFlow(BottomToolsUiState())
    val ui: StateFlow<BottomToolsUiState> = _ui

    fun onTabSelected(tab: Tab) { _ui.update { it.copy(selected = tab) } }
}

data class BottomToolsUiState(val selected: Tab? = null)
```
2) Contrôleur
```
class BottomToolsController(private val vm: SigmaViewModel) {
    val ui: StateFlow<BottomToolsUiState> = vm.ui
    fun select(tab: Tab) = vm.onTabSelected(tab)
}
```
3) UI
```
@Composable
fun BottomTools(controller: BottomToolsController) {
    val state by controller.ui.collectAsState()
    Row {
        Tab.values().forEach { tab ->
            Button(onClick = { controller.select(tab) }) {
                Text(tab.name + if (tab == state.selected) "*" else "")
            }
        }
    }
}
```
4) Assemblage dans l’Activity/Screen
```
@Composable
fun SigmaScreen() {
    val vm: SigmaViewModel = hiltViewModel()
    val controller = remember { BottomToolsController(vm) }
    BottomTools(controller)
}
```