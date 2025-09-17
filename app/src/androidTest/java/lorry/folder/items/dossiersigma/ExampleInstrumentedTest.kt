package lorry.folder.items.dossiersigma

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidTest
import lorry.folder.items.dossiersigma.ui.folderContent.breadcrumb.manageUI

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest: MainDispatcherRule() {

    @get:Rule val compose = createComposeRule()
    @get:Rule val main = MainDispatcherRule()
    
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("lorry.folder.items.dossiersigma", appContext.packageName)
    }

    @Test
    fun `bloc dans LaunchedEffect réagit aux tests`(){

        compose.setContent {

            manageUI(
                suffix = TODO(),
                prev = TODO(),
                commonPart = TODO(),
                vis = TODO(),
                path = TODO()
            )
        }





    }



}