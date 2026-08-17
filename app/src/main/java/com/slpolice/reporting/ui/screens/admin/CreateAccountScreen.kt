package com.slpolice.reporting.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.slpolice.reporting.data.UserRole
import com.slpolice.reporting.ui.AppViewModelProvider
import com.slpolice.reporting.ui.components.AppTextField
import com.slpolice.reporting.ui.components.Eyebrow
import com.slpolice.reporting.ui.components.MessageBanner
import com.slpolice.reporting.ui.components.PrimaryButton
import com.slpolice.reporting.ui.components.SectionCard
import com.slpolice.reporting.ui.theme.Hairline
import com.slpolice.reporting.ui.theme.InkSoft
import com.slpolice.reporting.ui.theme.Navy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(
    onBack: () -> Unit,
    viewModel: CreateAccountViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var role by rememberSaveable { mutableStateOf(UserRole.OFFICER.name) }
    var fullName by rememberSaveable { mutableStateOf("") }
    var nic by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var badge by rememberSaveable { mutableStateOf("") }
    var station by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }

    val isOfficer = role == UserRole.OFFICER.name

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Create an account") },
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
            state.error?.let {
                MessageBanner(text = it, isError = true)
                Spacer(Modifier.height(14.dp))
            }
            state.createdName?.let {
                MessageBanner(text = "Account created for $it", isError = false)
                Spacer(Modifier.height(14.dp))
            }

            SectionCard {
                Eyebrow("Account type")
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RoleOption(
                        label = "Police officer",
                        selected = isOfficer,
                        onClick = { role = UserRole.OFFICER.name },
                        modifier = Modifier.weight(1f)
                    )
                    RoleOption(
                        label = "Citizen",
                        selected = !isOfficer,
                        onClick = { role = UserRole.CITIZEN.name },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    if (isOfficer)
                        "Officers reach the case queue, the admin panel and reporter details."
                    else
                        "Citizens can only file reports and track their own cases.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSoft
                )
            }

            Spacer(Modifier.height(14.dp))

            SectionCard {
                Eyebrow("Holder details")
                Spacer(Modifier.height(12.dp))
                AppTextField(
                    value = fullName,
                    onValueChange = { fullName = it; viewModel.clearMessages() },
                    label = "Full name"
                )
                Spacer(Modifier.height(12.dp))
                AppTextField(
                    value = nic,
                    onValueChange = { nic = it.uppercase(); viewModel.clearMessages() },
                    label = "NIC number",
                    placeholder = "200012345678 or 901234567V"
                )
                Spacer(Modifier.height(12.dp))
                AppTextField(
                    value = phone,
                    onValueChange = { phone = it; viewModel.clearMessages() },
                    label = "Telephone number",
                    placeholder = "+94771234567",
                    helper = "Sri Lankan numbers only",
                    keyboardType = KeyboardType.Phone
                )
                Spacer(Modifier.height(12.dp))
                AppTextField(
                    value = email,
                    onValueChange = { email = it; viewModel.clearMessages() },
                    label = "Email address",
                    keyboardType = KeyboardType.Email
                )
                Spacer(Modifier.height(12.dp))
                AppTextField(
                    value = address,
                    onValueChange = { address = it; viewModel.clearMessages() },
                    label = if (isOfficer) "Station address" else "Residential address",
                    singleLine = false,
                    minLines = 2
                )
            }

            if (isOfficer) {
                Spacer(Modifier.height(14.dp))
                SectionCard {
                    Eyebrow("Service record")
                    Spacer(Modifier.height(12.dp))
                    AppTextField(
                        value = badge,
                        onValueChange = { badge = it.uppercase(); viewModel.clearMessages() },
                        label = "Badge number",
                        placeholder = "SLP-4471"
                    )
                    Spacer(Modifier.height(12.dp))
                    AppTextField(
                        value = station,
                        onValueChange = { station = it; viewModel.clearMessages() },
                        label = "Division or station",
                        placeholder = "Colombo Central Division"
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            SectionCard {
                Eyebrow("Temporary password")
                Spacer(Modifier.height(6.dp))
                Text(
                    "Hand this to the holder in person and ask them to change it from their profile after first sign-in.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSoft
                )
                Spacer(Modifier.height(12.dp))
                AppTextField(
                    value = password,
                    onValueChange = { password = it; viewModel.clearMessages() },
                    label = "Password",
                    isPassword = true,
                    keyboardType = KeyboardType.Password,
                    helper = "8 characters with a capital, a number and a symbol"
                )
                Spacer(Modifier.height(12.dp))
                AppTextField(
                    value = confirm,
                    onValueChange = { confirm = it; viewModel.clearMessages() },
                    label = "Repeat password",
                    isPassword = true,
                    keyboardType = KeyboardType.Password,
                    error = if (confirm.isNotEmpty() && confirm != password) "Both passwords must match" else null
                )
            }

            Spacer(Modifier.height(20.dp))
            PrimaryButton(
                text = "Create account",
                busy = state.busy,
                onClick = {
                    viewModel.create(
                        fullName = fullName,
                        nic = nic,
                        phone = phone,
                        email = email,
                        address = address,
                        password = password,
                        confirmPassword = confirm,
                        role = UserRole.from(role),
                        badgeNumber = badge,
                        station = station
                    )
                    if (password == confirm) {
                        fullName = ""; nic = ""; phone = ""; email = ""
                        address = ""; badge = ""; station = ""
                        password = ""; confirm = ""
                    }
                }
            )
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun RoleOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (selected) Navy.copy(alpha = 0.10f) else Color.Transparent)
            .border(1.dp, if (selected) Navy else Hairline, RoundedCornerShape(9.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Navy else InkSoft
        )
    }
}
