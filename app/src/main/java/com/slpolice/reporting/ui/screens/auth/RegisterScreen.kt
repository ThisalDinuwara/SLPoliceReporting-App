package com.slpolice.reporting.ui.screens.auth

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
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.slpolice.reporting.ui.components.MessageBanner
import com.slpolice.reporting.ui.components.PrimaryButton
import com.slpolice.reporting.ui.theme.InkSoft
import com.slpolice.reporting.ui.theme.Navy
import com.slpolice.reporting.ui.theme.StatusAction
import com.slpolice.reporting.util.Validators

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var fullName by rememberSaveable { mutableStateOf("") }
    var nic by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var accepted by rememberSaveable { mutableStateOf(false) }

    val nicProfile = remember(nic) { Validators.decodeNic(nic) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Create your account") },
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
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Text(
                "Registration is tied to your National Identity Card, which is what keeps anonymous submissions out of the evidence queue.",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft
            )
            Spacer(Modifier.height(20.dp))

            state.error?.let {
                MessageBanner(text = it, isError = true)
                Spacer(Modifier.height(14.dp))
            }

            AppTextField(
                value = fullName,
                onValueChange = { fullName = it; viewModel.clearMessages() },
                label = "Full name",
                placeholder = "As printed on your NIC"
            )
            Spacer(Modifier.height(14.dp))

            AppTextField(
                value = nic,
                onValueChange = { nic = it.uppercase(); viewModel.clearMessages() },
                label = "NIC number",
                placeholder = "200012345678 or 901234567V",
                helper = if (nicProfile == null) "9 digits plus V, or the 12-digit format" else null
            )
            if (nicProfile != null) {
                Spacer(Modifier.height(10.dp))
                NicVerifiedCard(
                    birthDate = nicProfile.formattedBirthDate,
                    gender = nicProfile.gender,
                    adult = Validators.isAdult(nic)
                )
            }
            Spacer(Modifier.height(14.dp))

            AppTextField(
                value = phone,
                onValueChange = { phone = it; viewModel.clearMessages() },
                label = "Telephone number",
                placeholder = "+94771234567",
                helper = "Sri Lankan numbers only",
                keyboardType = KeyboardType.Phone
            )
            Spacer(Modifier.height(14.dp))

            AppTextField(
                value = email,
                onValueChange = { email = it; viewModel.clearMessages() },
                label = "Email address",
                keyboardType = KeyboardType.Email
            )
            Spacer(Modifier.height(14.dp))

            AppTextField(
                value = address,
                onValueChange = { address = it; viewModel.clearMessages() },
                label = "Residential address",
                singleLine = false,
                minLines = 2
            )
            Spacer(Modifier.height(14.dp))

            AppTextField(
                value = password,
                onValueChange = { password = it; viewModel.clearMessages() },
                label = "Password",
                isPassword = true,
                keyboardType = KeyboardType.Password,
                helper = "8 characters with a capital, a number and a symbol"
            )
            Spacer(Modifier.height(14.dp))

            AppTextField(
                value = confirm,
                onValueChange = { confirm = it; viewModel.clearMessages() },
                label = "Repeat password",
                isPassword = true,
                keyboardType = KeyboardType.Password,
                error = if (confirm.isNotEmpty() && confirm != password) "Both passwords must match" else null
            )
            Spacer(Modifier.height(18.dp))

            TermsRow(accepted = accepted, onChange = { accepted = it })
            Spacer(Modifier.height(20.dp))

            PrimaryButton(
                text = "Verify and create account",
                busy = state.busy,
                onClick = {
                    viewModel.register(
                        fullName = fullName,
                        nic = nic,
                        phone = phone,
                        email = email,
                        address = address,
                        password = password,
                        confirmPassword = confirm,
                        acceptedTerms = accepted,
                        onRegistered = onRegistered
                    )
                }
            )
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun NicVerifiedCard(birthDate: String, gender: String, adult: Boolean) {
    val tint = if (adult) StatusAction else MaterialTheme.colorScheme.error
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(tint.copy(alpha = 0.09f))
            .padding(12.dp)
    ) {
        Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Eyebrow(if (adult) "NIC recognised" else "NIC holder is under 18", color = tint)
            Spacer(Modifier.height(3.dp))
            Text(
                "$gender \u00b7 born $birthDate",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun TermsRow(accepted: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(
            checked = accepted,
            onCheckedChange = onChange,
            colors = CheckboxDefaults.colors(checkedColor = Navy)
        )
        Column(modifier = Modifier.padding(top = 12.dp)) {
            Text("I accept the reporting terms", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Filing a knowingly false report, or altering evidence before uploading it, is an offence under the Penal Code and the Evidence (Special Provisions) Act. Personal details are handled under Sri Lanka's Personal Data Protection Act.",
                style = MaterialTheme.typography.bodySmall,
                color = InkSoft
            )
        }
    }
}
