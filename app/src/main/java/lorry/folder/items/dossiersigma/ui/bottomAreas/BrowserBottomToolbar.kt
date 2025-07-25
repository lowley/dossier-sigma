package lorry.folder.items.dossiersigma.ui.bottomAreas

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
import androidx.constraintlayout.solver.GoalRow
import kotlinx.coroutines.flow.MutableStateFlow
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
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    ) {
        Spacer(
            modifier = Modifier
                .padding(start = 50.dp, end = 50.dp, top = 5.dp, bottom = 0.dp)
                .height(1.dp)
                .fillMaxWidth()
                .background(Color.LightGray)
        )
        Row(
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .background(Color.Transparent)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { webView?.value?.goBack() },
                enabled = canGoBack,
                modifier = Modifier.padding(horizontal = 5.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFe9c46a),
                    contentColor = Color.Black
                )
            )
            {
                Icon(
                    painter = painterResource(id = R.drawable.la_gauche),
                    contentDescription = "back",
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    tint = Color.Black
                )
            }

            Button(
                onClick = { mainViewModel.browserManager.setCurrentPage("https://www.google.fr") },
                modifier = Modifier.padding(horizontal = 5.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFe9c46a),
                    contentColor = Color.Black
                )
            )
            {
                Icon(
                    painter = painterResource(id = R.drawable.maison),
                    contentDescription = "home",
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    tint = Color.Black
                )
            }

            Button(
                onClick = mainViewModel.browserManager::closeBrowser,
                modifier = Modifier.padding(horizontal = 5.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFe9c46a),
                    contentColor = Color.Black
                )
            ) {
                Text(
                    "Retourner à l'application",
                    color = Color.Black
                )
            }

            Button(
                onClick = { webView.value?.goForward() },
                enabled = canGoForward,
                modifier = Modifier.padding(horizontal = 5.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFe9c46a),
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.la_droite),
                    contentDescription = "forward",
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    tint = Color.Black
                )
            }
        }
    }
}