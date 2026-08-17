package com.slpolice.reporting.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slpolice.reporting.ui.AppViewModelProvider
import com.slpolice.reporting.ui.components.AppTextField
import com.slpolice.reporting.ui.components.Eyebrow
import com.slpolice.reporting.ui.components.LabelledValue
import com.slpolice.reporting.ui.components.MessageBanner
import com.slpolice.reporting.ui.components.PrimaryButton
import com.slpolice.reporting.ui.components.SectionCard
import com.slpolice.reporting.ui.theme.BraidGold
import com.slpolice.reporting.ui.theme.InkSoft
import com.slpolice.reporting.ui.theme.Navy
import com.slpolice.reporting.ui.theme.StatusAction
import com.slpolice.reporting.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    var current by rememberSaveable { mutableStateOf("") }
    var replacement by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Your account") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            val user = state.user

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(StatusAction.copy(alpha = 0.09f))
                    .padding(14.dp)
            ) {
                Icon(
                    Icons.Filled.VerifiedUser,
                    contentDescription = null,
                    tint = StatusAction,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Eyebrow("Identity verified", color = StatusAction)
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "Checked against the National Identity Card register",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSoft
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            SectionCard {
                Eyebrow("Account")
                Spacer(Modifier.height(6.dp))
                LabelledValue("Full name", user?.fullName.orEmpty())
                LabelledValue("NIC", user?.nic.orEmpty())
                state.nicProfile?.let {
                    LabelledValue("On record", "${it.gender} \u00b7 born ${it.formattedBirthDate}")
                }
                LabelledValue("Mobile", user?.phone.orEmpty())
                LabelledValue("Email", user?.email.orEmpty())
                LabelledValue("Address", user?.address.orEmpty())
                user?.let { LabelledValue("Member since", Formatters.date(it.createdAt)) }
                LabelledValue("Reports filed", state.reportCount.toString())
            }

            Spacer(Modifier.height(16.dp))

            SectionCard {
                Eyebrow("Change password")
                Spacer(Modifier.height(12.dp))
                message?.let { (text, isError) ->
                    MessageBanner(text = text, isError = isError)
                    Spacer(Modifier.height(12.dp))
                }
                AppTextField(
                    value = current,
                    onValueChange = { current = it; viewModel.clearMessage() },
                    label = "Current password",
                    isPassword = true,
                    keyboardType = KeyboardType.Password
                )
                Spacer(Modifier.height(12.dp))
                AppTextField(
                    value = replacement,
                    onValueChange = { replacement = it; viewModel.clearMessage() },
                    label = "New password",
                    isPassword = true,
                    keyboardType = KeyboardType.Password,
                    helper = "8 characters with a capital, a number and a symbol"
                )
                Spacer(Modifier.height(12.dp))
                AppTextField(
                    value = confirm,
                    onValueChange = { confirm = it; viewModel.clearMessage() },
                    label = "Repeat new password",
                    isPassword = true,
                    keyboardType = KeyboardType.Password
                )
                Spacer(Modifier.height(16.dp))
                PrimaryButton(
                    text = "Change password",
                    enabled = current.isNotBlank() && replacement.isNotBlank(),
                    onClick = {
                        viewModel.changePassword(current, replacement, confirm)
                        current = ""
                        replacement = ""
                        confirm = ""
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            SectionCard {
                Eyebrow("How your data is held")
                Spacer(Modifier.height(8.dp))
                Text(
                    "Passwords are stored as PBKDF2 hashes and never in readable form. Evidence files stay in this app's private storage, sealed with a SHA-256 checksum, and are visible only to you and to verified department personnel.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSoft
                )
            }

            Spacer(Modifier.height(22.dp))
            OutlinedButton(
                onClick = { viewModel.signOut(onSignedOut) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 14.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sign out")
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Sri Lanka Police Digital Evidence Platform \u00b7 v1.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = BraidGold
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
