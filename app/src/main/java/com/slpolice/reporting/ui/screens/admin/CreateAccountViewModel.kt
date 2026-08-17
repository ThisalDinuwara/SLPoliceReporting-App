package com.slpolice.reporting.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slpolice.reporting.data.Outcome
import com.slpolice.reporting.data.UserRole
import com.slpolice.reporting.data.prefs.SessionManager
import com.slpolice.reporting.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class CreateAccountState(
    val busy: Boolean = false,
    val error: String? = null,
    val createdName: String? = null
)

/** Backs the admin panel's account provisioning form. */
class CreateAccountViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(CreateAccountState())
    val state: StateFlow<CreateAccountState> = _state.asStateFlow()

    fun clearMessages() {
        _state.value = _state.value.copy(error = null, createdName = null)
    }

    fun create(
        fullName: String,
        nic: String,
        phone: String,
        email: String,
        address: String,
        password: String,
        confirmPassword: String,
        role: UserRole,
        badgeNumber: String,
        station: String
    ) {
        if (_state.value.busy) return
        if (password != confirmPassword) {
            _state.value = CreateAccountState(error = "Both passwords must match")
            return
        }
        _state.value = CreateAccountState(busy = true)

        viewModelScope.launch {
            val actorId = sessionManager.session.first().userId
            val actor = actorId?.let { authRepository.findUser(it) }
            val result = authRepository.createAccount(
                actorName = actor?.fullName.orEmpty(),
                fullName = fullName,
                nic = nic,
                phone = phone,
                email = email,
                address = address,
                password = password,
                role = role,
                badgeNumber = badgeNumber.ifBlank { null },
                station = station.ifBlank { null }
            )
            _state.value = when (result) {
                is Outcome.Success -> CreateAccountState(createdName = fullName.trim())
                is Outcome.Failure -> CreateAccountState(error = result.message)
            }
        }
    }
}
