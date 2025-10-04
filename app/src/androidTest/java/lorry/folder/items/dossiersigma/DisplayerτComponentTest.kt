package lorry.folder.items.dossiersigma

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import blahblah.kommunicator.CommunicatorContract
import blahblah.kommunicator.CommunicatorContract.EMITTER__PROCESSING_FILE
import blahblah.kommunicator.CommunicatorContract.EMITTER__RECEPTION_ACKNOWLEDGMENT
import blahblah.kommunicator.IncomingMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import lorry.folder.items.dossiersigma.ui.tinies.DisplayerτComponent

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Rule
import java.text.MessageFormat

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class DisplayerτComponentTest {

    @get:Rule
    val rule = createComposeRule()

    @get:Rule
    val main = MainDispatcherRule()

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Test
    fun `nothing displayed by default`() {

        val displayerτComponent = DisplayerτComponent(SigmaApplication.getContext())

        rule.setContent {
            displayerτComponent.MessageDisplayerτ()
        }

        rule.onNodeWithTag(DisplayerτComponent.MESSAGE_TAG).assertExists()
        rule.onNodeWithTag("message").assertTextEquals(DisplayerτComponent.WAITING_MESSAGE)
    }

    @Test
    fun `all received ⭢ processing starts`() = runTest {
        //arrange
        val dummyStatesFlow = MutableSharedFlow<IncomingMessage>(
            replay = 0,
            extraBufferCapacity = 64
        )
        val displayerτComponent = DisplayerτComponent(
            SigmaApplication.getContext(),
            testFlow = dummyStatesFlow
        )

        rule.mainClock.autoAdvance = false

        rule.setContent {
            displayerτComponent.MessageDisplayerτ()
        }

        dummyStatesFlow.emit(IncomingMessage(EMITTER__RECEPTION_ACKNOWLEDGMENT))

        rule.mainClock.advanceTimeBy(3_000)

        rule.onNodeWithTag(DisplayerτComponent.MESSAGE_TAG).assertExists()
        rule.onNodeWithTag(DisplayerτComponent.MESSAGE_TAG)
            .assertTextEquals(DisplayerτComponent.PROCESS_STARTED_MESSAGE)
    }

    @Test
    fun `NOT-all received ⭢ no communication message`() = runTest {
        //arrange
        val dummyStatesFlow = MutableSharedFlow<IncomingMessage>(
            replay = 0,
            extraBufferCapacity = 64
        )
        val displayerτComponent = DisplayerτComponent(
            SigmaApplication.getContext(),
            testFlow = dummyStatesFlow
        )

        rule.mainClock.autoAdvance = false

        rule.setContent {
            displayerτComponent.MessageDisplayerτ()
        }

        rule.mainClock.advanceTimeBy(3_000)

        rule.onNodeWithTag(DisplayerτComponent.MESSAGE_TAG).assertExists()
        rule.onNodeWithTag(DisplayerτComponent.MESSAGE_TAG)
            .assertTextEquals(DisplayerτComponent.NO_COMMUNICATION_MESSAGE)
    }

    @Test
    fun `processing starts ⭢ send n-N`() = runTest {
        //arrange
        val dummyStatesFlow = MutableSharedFlow<IncomingMessage>(
            replay = 0,
            extraBufferCapacity = 64
        )

        val displayerτComponent = DisplayerτComponent(
            SigmaApplication.getContext(),
            testFlow = dummyStatesFlow
        )

        rule.mainClock.autoAdvance = false

        rule.setContent {
            displayerτComponent.MessageDisplayerτ()
        }

        dummyStatesFlow.emit(IncomingMessage(EMITTER__RECEPTION_ACKNOWLEDGMENT))

        rule.mainClock.advanceTimeBy(2_000)

        rule.onNodeWithTag(DisplayerτComponent.MESSAGE_TAG).assertExists()
        rule.onNodeWithTag(DisplayerτComponent.MESSAGE_TAG)
            .assertTextEquals(DisplayerτComponent.PROCESS_STARTED_MESSAGE)

        dummyStatesFlow.emit(IncomingMessage(EMITTER__PROCESSING_FILE, 5, 7))
        rule.mainClock.advanceTimeBy(4_000)

        rule.onNodeWithTag(DisplayerτComponent.MESSAGE_TAG).assertExists()
        rule.onNodeWithTag(DisplayerτComponent.MESSAGE_TAG)
            .assertTextEquals(MessageFormat.format(DisplayerτComponent.PROCESSING_FILE_MESSAGE, 5, 7))
    }

    @Test
    fun `send n-N with parsing failure 0-0`() = runTest {
        //arrange
        val dummyStatesFlow = MutableSharedFlow<IncomingMessage>(
            replay = 0,
            extraBufferCapacity = 64
        )

        val displayerτComponent = DisplayerτComponent(
            SigmaApplication.getContext(),
            testFlow = dummyStatesFlow
        )

        rule.mainClock.autoAdvance = false

        rule.setContent {
            displayerτComponent.MessageDisplayerτ()
        }

        dummyStatesFlow.emit(IncomingMessage(EMITTER__RECEPTION_ACKNOWLEDGMENT))

        rule.mainClock.advanceTimeBy(2_000)

        rule.onNodeWithTag(DisplayerτComponent.MESSAGE_TAG).assertExists()
        rule.onNodeWithTag(DisplayerτComponent.MESSAGE_TAG)
            .assertTextEquals(DisplayerτComponent.PROCESS_STARTED_MESSAGE)

        dummyStatesFlow.emit(IncomingMessage(EMITTER__PROCESSING_FILE, 0, 0))
        rule.mainClock.advanceTimeBy(4_000)

        rule.onNodeWithTag(DisplayerτComponent.MESSAGE_TAG).assertExists()
        rule.onNodeWithTag(DisplayerτComponent.MESSAGE_TAG)
            .assertTextEquals(DisplayerτComponent.ERROR_PROCESSING_FILE_MESSAGE)
    }
}