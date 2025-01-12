package lorry.folder.items.dossiersigma

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.domain.Item
import javax.inject.Inject

class GlobalStateManager @Inject constructor(){
    private val _selectedItem = MutableStateFlow<Item?>(null)
    val selectedItem: StateFlow<Item?> = _selectedItem
    
    var doNotTriggerChange = false

    fun setSelectedItem(item: Item?) {
        _selectedItem.value = item
    }
}