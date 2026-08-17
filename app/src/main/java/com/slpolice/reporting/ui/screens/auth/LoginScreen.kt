package com.slpolice.reporting.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slpolice.reporting.data.UserRole
import com.slpolice.reporting.data.repository.AuthRepository
import com.slpolice.reporting.ui.AppViewModelProvider
import com.slpolice.reporting.ui.components.AppTextField
import com.slpolice.reporting.ui.components.Eyebrow
import com.slpolice.reporting.ui.components.MessageBanner
import com.slpolice.reporting.ui.components.PrimaryButton
import com.slpolice.reporting.ui.theme.BraidGold
import com.slpolice.reporting.ui.theme.InkSoft
import com.slpolice.reporting.ui.theme.Navy
import com.slpolice.reporting.ui.theme.TagStyle

@Composable
fun LoginScreen(
    onSignedIn: (UserRole) -> Unit,
    onCreateAccount: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var nic by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        AuthHeader(
            eyebrow = "Sri Lanka Police",
            title = "Report an incident with evidence",
            caption = "Files go straight to the department. Nothing passes through a third party."
        )

        Column(modifier = Modifier.padding(24.dp)) {
            Text("Sign in", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Use the NIC number your account was verified with.",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft
            )
            Spacer(Modifier.height(20.dp))

            state.error?.let {
                MessageBanner(text = it, isError = true)
                Spacer(Modifier.height(14.dp))
            }

            AppTextField(
                value = nic,
                onValueChange = {
                    nic = it
                    viewModel.clearMessages()
                },
                label = "NIC number",
                placeholder = "200012345678 or 901234567V"
            )
            Spacer(Modifier.height(14.dp))
            AppTextField(
                value = password,
                onValueChange = {
                    password = it
                    viewModel.clearMessages()
                },
                label = "Password",
                isPassword = true,
                keyboardType = KeyboardType.Password
            )
            Spacer(Modifier.height(22.dp))

            PrimaryButton(
                text = "Sign in",
                busy = state.busy,
                onClick = { viewModel.signIn(nic, password, onSignedIn) }
            )

            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("No account yet?", style = MaterialTheme.typography.bodyMedium, color = InkSoft)
                TextButton(onClick = onCreateAccount) {
                    Text("Verify your NIC", color = Navy, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(18.dp))
            OfficerHint()
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AuthHeader(eyebrow: String, title: String, caption: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Navy)
            .padding(horizontal = 24.dp, vertical = 34.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(BraidGold.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        Icons.Filled.Shield,
                        contentDescription = null,
                        tint = BraidGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Eyebrow(eyebrow, color = BraidGold)
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = caption,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.72f)
            )
        }
    }
}

/** Demo credentials for the police channel, shown so the admin side can be reviewed. */
@Composable
private fun OfficerHint() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Navy.copy(alpha = 0.05f))
            .padding(14.dp)
    ) {
        Column {
            Eyebrow("Police channel \u00b7 demo account")
            Spacer(Modifier.height(6.dp))
            Text(
                text = AuthRepository.DEMO_OFFICER_NIC + "  \u00b7  " + AuthRepository.DEMO_OFFICER_PASSWORD,
                style = TagStyle,
                color = Navy
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Officer accounts are issued by the department and cannot be self-registered.",
                style = MaterialTheme.typography.bodySmall,
                color = InkSoft
            )
        }
    }
}
