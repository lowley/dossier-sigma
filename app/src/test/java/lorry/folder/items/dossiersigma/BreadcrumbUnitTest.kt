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
class BreadcrumbUnitTest : MainDispatcherRule() {

    var previousPath = ""
    var currentPath = ""
    val breadcrumbComponent = BreadcrumbComponent()
    val breadcrumbState: MutableStateFlow<BreadcrumbState> =
        MutableStateFlow(BreadcrumbState.LOADING as BreadcrumbState)

    @Test
    fun `regulator() existe`() {
        assert(BreadcrumbComponent::class.declaredMemberFunctions.find {
            it.name == "breadcrumbStateRegulate"
                    && it.isSuspend
        } != null)
    }

    @Test
    fun `regulator() ∅ → ∅`() = runTest {

        previousPath = ""
        currentPath = ""

        breadcrumbState.test {

            //saute la valeur initiale
            skipItems(1)

            val job = launch {
                breadcrumbComponent.breadcrumbStateRegulate(
                    currentPath = currentPath,
                    previousPath = previousPath,
                    state = breadcrumbState
                )
            }

            advanceTimeBy(300)
            expectNoEvents()
        }
    }

    @Test
    fun `regulator() x → x`() = runTest {

        previousPath = "x"
        currentPath = "x"

        breadcrumbState.test {

            //saute la valeur initiale
            skipItems(1)

            val job = launch {
                breadcrumbComponent.breadcrumbStateRegulate(
                    currentPath = currentPath,
                    previousPath = previousPath,
                    state = breadcrumbState
                )
            }

            advanceTimeBy(300)
            expectNoEvents()
        }
    }

    @Test
    fun `regulator() ∅ → a`() = runTest {

        previousPath = ""
        currentPath = "a"

        breadcrumbState.test {

            //saute la valeur initiale
            skipItems(1)

            val job = launch {
                breadcrumbComponent.breadcrumbStateRegulate(
                    currentPath = currentPath,
                    previousPath = previousPath,
                    state = breadcrumbState
                )
            }

            val a = awaitItem()
            assert(
                a == BreadcrumbState.DATA(
                    currentPath = "a",
                    animation = Animation.APPEAR
                )
            )
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `regulator() x → x·a`() = runTest {

        previousPath = "x"
        currentPath = "x/a"

        breadcrumbState.test {

            //saute la valeur initiale
            skipItems(1)

            val job = launch {
                breadcrumbComponent.breadcrumbStateRegulate(
                    currentPath = currentPath,
                    previousPath = previousPath,
                    state = breadcrumbState
                )
            }

            assert(
                awaitItem() == BreadcrumbState.DATA(
                    currentPath = "x/a",
                    animation = Animation.APPEAR
                )
            )
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `regulator() x → ∅`() = runTest {

        previousPath = "x"
        currentPath = ""

        breadcrumbState.test {

            //saute la valeur initiale
            skipItems(1)

            val job = launch {
                breadcrumbComponent.breadcrumbStateRegulate(
                    currentPath = currentPath,
                    previousPath = previousPath,
                    state = breadcrumbState
                )
            }

            val atomicDuration = 500L

            val firstItem = awaitItem()
            assert(
                firstItem == BreadcrumbState.DATA(
                    currentPath = "x",
                    animation = Animation.DISAPPEAR
                )
            )
            advanceTimeBy(atomicDuration - 5)
            expectNoEvents()


        }
    }

    @Test
    fun `regulator() x·y·z → x`() = runTest {

        previousPath = "x/y/z"
        currentPath = "x"

        breadcrumbState.test {

            //saute la valeur initiale
            skipItems(1)

            val job = launch {
                breadcrumbComponent.breadcrumbStateRegulate(
                    currentPath = currentPath,
                    previousPath = previousPath,
                    state = breadcrumbState
                )
            }

            val atomicDuration = 500L / 2

            val firstItem = awaitItem()
            assert(
                firstItem == BreadcrumbState.DATA(
                    currentPath = "x/y/z",
                    animation = Animation.DISAPPEAR
                )
            )
            advanceTimeBy(atomicDuration - 5)
            expectNoEvents()

            val secondItem = awaitItem()
            assert(
                secondItem == BreadcrumbState.DATA(
                    currentPath = "x/y",
                    animation = Animation.DISAPPEAR
                )
            )
            advanceTimeBy(atomicDuration - 5)
            expectNoEvents()

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `regulator() x → x·a·b`() = runTest {

        previousPath = "x"
        currentPath = "x/a/b"

        breadcrumbState.test {

            //saute la valeur initiale
            skipItems(1)

            val job = launch {
                breadcrumbComponent.breadcrumbStateRegulate(
                    currentPath = currentPath,
                    previousPath = previousPath,
                    state = breadcrumbState,
                    duration = 500L
                )
            }

            val atomicDuration = 500L / 2

            val firstItem = awaitItem()
            assert(
                firstItem == BreadcrumbState.DATA(
                    currentPath = "x/a",
                    animation = Animation.APPEAR
                )
            )
            advanceTimeBy(atomicDuration - 5)
            expectNoEvents()

            val secondItem = awaitItem()
            assert(
                secondItem == BreadcrumbState.DATA(
                    currentPath = "x/a/b",
                    animation = Animation.APPEAR
                )
            )
            advanceTimeBy(atomicDuration - 5)
            expectNoEvents()

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `regulator() ∅ → a·b`() = runTest {

        previousPath = ""
        currentPath = "a/b"

        breadcrumbState.test {

            //saute la valeur initiale
            skipItems(1)

            val job = launch {
                breadcrumbComponent.breadcrumbStateRegulate(
                    currentPath = currentPath,
                    previousPath = previousPath,
                    state = breadcrumbState,
                    duration = 500L
                )
            }

            val atomicDuration = 500L / 2

            val firstItem = awaitItem()
            assert(
                firstItem == BreadcrumbState.DATA(
                    currentPath = "a",
                    animation = Animation.APPEAR
                )
            )
            advanceTimeBy(atomicDuration - 5)
            expectNoEvents()

            val secondItem = awaitItem()
            assert(
                secondItem == BreadcrumbState.DATA(
                    currentPath = "a/b",
                    animation = Animation.APPEAR
                )
            )
            advanceTimeBy(atomicDuration - 5)
            expectNoEvents()

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `regulator() x·y → x·a`() = runTest {

        previousPath = "x/y"
        currentPath = "x/a"

        breadcrumbState.test {

            //saute la valeur initiale
            skipItems(1)

            val job = launch {
                breadcrumbComponent.breadcrumbStateRegulate(
                    currentPath = currentPath,
                    previousPath = previousPath,
                    state = breadcrumbState,
                    duration = 500L
                )
            }

            val atomicDuration = 500L / 2

            val firstItem = awaitItem()
            println(firstItem)
            assert(
                firstItem == BreadcrumbState.DATA(
                    currentPath = "x/y",
                    animation = Animation.DISAPPEAR
                )
            )
            advanceTimeBy(atomicDuration - 5)
            expectNoEvents()

            val secondItem = awaitItem()
            println(secondItem)
            assert(
                secondItem == BreadcrumbState.DATA(
                    currentPath = "x/a",
                    animation = Animation.APPEAR
                )
            )
            advanceTimeBy(atomicDuration - 5)
            expectNoEvents()

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `regulator() x·y → a·b`() = runTest {

        previousPath = "x/y"
        currentPath = "a/b"

        breadcrumbState.test {

            //saute la valeur initiale
            skipItems(1)

            val job = launch {
                breadcrumbComponent.breadcrumbStateRegulate(
                    currentPath = currentPath,
                    previousPath = previousPath,
                    state = breadcrumbState,
                    duration = 500L
                )
            }

            val atomicDuration = 500L / 4

            var item = awaitItem()
            println("item:$item")
            assert(
                item == BreadcrumbState.DATA(
                    currentPath = "x/y",
                    animation = Animation.DISAPPEAR
                )
            )
            advanceTimeBy(atomicDuration - 5)
            expectNoEvents()

            item = awaitItem()
            println("item:$item")
            assert(
                item == BreadcrumbState.DATA(
                    currentPath = "x",
                    animation = Animation.DISAPPEAR
                )
            )
            advanceTimeBy(atomicDuration - 5)
            expectNoEvents()

            item = awaitItem()
            println("item:$item")
            assert(
                item == BreadcrumbState.DATA(
                    currentPath = "a",
                    animation = Animation.APPEAR
                )
            )
            advanceTimeBy(atomicDuration - 5)
            expectNoEvents()

            item = awaitItem()
            println("item:$item")
            assert(
                item == BreadcrumbState.DATA(
                    currentPath = "a/b",
                    animation = Animation.APPEAR
                )
            )
            advanceTimeBy(atomicDuration - 5)
            expectNoEvents()

            ensureAllEventsConsumed()
        }
    }
}