package lorry.folder.items.dossiersigma.headless.folderContentBack.utils

import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID
import javax.inject.Singleton

@Singleton
class BackFeed @Inject constructor(

): IBackFeed{

    ////////////////////////
    // étiquette courante //
    ////////////////////////
    private val _currentFlagId = MutableStateFlow<UUID?>(null)

    override val currentFlagId: StateFlow<UUID?> = _currentFlagId

    override fun setCurrentFlagId(flagId: UUID?) {
        _currentFlagId.value = flagId
    }
}