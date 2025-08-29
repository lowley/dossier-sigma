package lorry.folder.items.dossiersigma.headless.folderContent

import lorry.folder.items.dossiersigma.headless.domain.SigmaFolder
import lorry.folder.items.dossiersigma.ui.sigma.SortingCriterion

data class FolderCacheEntry(
    val folder: SigmaFolder,
    val sort: SortingCriterion,
    val freshness: FolderFreshness,
    val cachedAt: Long = System.currentTimeMillis()
)
