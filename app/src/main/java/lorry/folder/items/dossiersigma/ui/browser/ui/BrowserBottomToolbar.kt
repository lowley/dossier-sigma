package lorry.folder.items.dossiersigma.ui.browser.ui

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity

@Composable
context(SigmaActivity)
fun BrowserBottomToolbar(
    webView: StateFlow<WebView?>,
    canGoBackFlow: StateFlow<Boolean>,
    canGoForwardFlow: StateFlow<Boolean>
) {
    val canGoForward by canGoForwardFlow.collectAsState()
    val canGoBack by canGoBackFlow.collectAsState()

    Column(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .height(65.dp)
            .background(Color.Companion.Transparent)
    ) {
        Spacer(
            modifier = Modifier.Companion
                .padding(start = 50.dp, end = 50.dp, top = 5.dp, bottom = 0.dp)
                .height(1.dp)
                .fillMaxWidth()
                .background(Color.Companion.LightGray)
        )
        Row(
            Modifier.Companion
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .background(Color.Companion.Transparent)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            Button(
                onClick = { webView?.value?.goBack() },
                enabled = canGoBack,
                modifier = Modifier.Companion.padding(horizontal = 5.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFe9c46a),
                    contentColor = Color.Companion.Black
                )
            )
            {
                Icon(
                    painter = painterResource(id = R.drawable.la_gauche),
                    contentDescription = "back",
                    modifier = Modifier.Companion.size(ButtonDefaults.IconSize),
                    tint = Color.Companion.Black
                )
            }

            Button(
                onClick = { mainViewModel.browserManager.setCurrentPage("https://www.google.fr") },
                modifier = Modifier.Companion.padding(horizontal = 5.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFe9c46a),
                    contentColor = Color.Companion.Black
                )
            )
            {
                Icon(
                    painter = painterResource(id = R.drawable.maison),
                    contentDescription = "home",
                    modifier = Modifier.Companion.size(ButtonDefaults.IconSize),
                    tint = Color.Companion.Black
                )
            }

            Button(
                onClick = mainViewModel.browserManager::closeBrowser,
                modifier = Modifier.Companion.padding(horizontal = 5.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFe9c46a),
                    contentColor = Color.Companion.Black
                )
            ) {
                Text(
                    "Retourner à l'application",
                    color = Color.Companion.Black
                )
            }

            Button(
                onClick = { webView.value?.goForward() },
                enabled = canGoForward,
                modifier = Modifier.Companion.padding(horizontal = 5.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFe9c46a),
                    contentColor = Color.Companion.Black
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.la_droite),
                    contentDescription = "forward",
                    modifier = Modifier.Companion.size(ButtonDefaults.IconSize),
                    tint = Color.Companion.Black
                )
            }
        }
    }
}