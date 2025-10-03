package lorry.folder.items.dossiersigma

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import lorry.folder.items.dossiersigma.ui.folderContent.breadcrumb.Animation
import lorry.folder.items.dossiersigma.ui.folderContent.breadcrumb.BreadcrumbComponent
import lorry.folder.items.dossiersigma.ui.folderContent.breadcrumb.BreadcrumbState
import org.junit.Test

import kotlin.reflect.full.declaredMemberFunctions

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class MessageDisplayerτUnitTest : MainDispatcherRule() {

    @Test
    fun `regulator() existe`() {
        assert(BreadcrumbComponent::class.declaredMemberFunctions.find {
            it.name == "breadcrumbStateRegulate"
                    && it.isSuspend
        } != null)
    }


}