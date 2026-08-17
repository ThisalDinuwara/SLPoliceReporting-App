package com.slpolice.reporting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slpolice.reporting.data.UserRole
import com.slpolice.reporting.data.prefs.Session
import com.slpolice.reporting.ui.navigation.AppNavHost
import com.slpolice.reporting.ui.navigation.Routes
import com.slpolice.reporting.ui.screens.SplashScreen
import com.slpolice.reporting.ui.theme.SLPoliceTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as SLPoliceApp).container

        setContent {
            SLPoliceTheme {
                // The stored session decides where the app opens: the queue for officers,
                // the citizen dashboard for reporters, sign-in for everyone else.
                val session: Session? by container.sessionManager.session
                    .collectAsStateWithLifecycle(initialValue = null)

                var startDestination by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(session) {
                    val current = session
                    if (startDestination == null && current != null) {
                        startDestination = when {
                            !current.isSignedIn -> Routes.LOGIN
                            current.role == UserRole.OFFICER -> Routes.ADMIN
                            else -> Routes.HOME
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val destination = startDestination
                    if (destination == null) SplashScreen() else AppNavHost(destination)
                }
            }
        }
    }
}
