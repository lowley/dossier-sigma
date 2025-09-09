# NOTES

```
👀️ adb install "C:\Users\olivier\progs\dossiersigma.apk"
```

## codes logcat


| code      | signification                                                                                                 |
| --------- | ------------------------------------------------------------------------------------------------------------- |
| PathCrspd | épie path des FolderCacheEntry émises par DaemonService et reçues par <br />FolderContentComponent (flows) |
| fldDec    | état d'entrée de la recombination de folderContentFlow dans FolderContentComponent + décision de récup de données

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