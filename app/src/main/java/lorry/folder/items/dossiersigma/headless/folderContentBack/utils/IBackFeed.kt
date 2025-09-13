package lorry.folder.items.dossiersigma.headless.folderContentBack.utils

import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

interface IBackFeed {

    ////////////////////////
    // étiquette courante //
    ////////////////////////
    val currentFlagId: StateFlow<UUID?>
    fun setCurrentFlagId(flagId: UUID?)
}