package com.slpolice.reporting.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slpolice.reporting.data.Outcome
import com.slpolice.reporting.data.UserRole
import com.slpolice.reporting.data.prefs.SessionManager
import com.slpolice.reporting.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val busy: Boolean = false,
    val error: String? = null,
    val notice: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun clearMessages() {
        _state.value = _state.value.copy(error = null, notice = null)
    }

    fun signIn(nic: String, password: String, onSignedIn: (UserRole) -> Unit) {
        if (_state.value.busy) return
        _state.value = AuthUiState(busy = true)
        viewModelScope.launch {
            when (val result = authRepository.signIn(nic, password)) {
                is Outcome.Success -> {
                    val role = UserRole.from(result.data.role)
                    sessionManager.signIn(result.data.id, role)
                    _state.value = AuthUiState()
                    onSignedIn(role)
                }
                is Outcome.Failure -> _state.value = AuthUiState(error = result.message)
            }
        }
    }

    fun register(
        fullName: String,
        nic: String,
        phone: String,
        email: String,
        address: String,
        password: String,
        confirmPassword: String,
        acceptedTerms: Boolean,
        onRegistered: () -> Unit
    ) {
        if (_state.value.busy) return
        if (password != confirmPassword) {
            _state.value = AuthUiState(error = "Both passwords must match")
            return
        }
        if (!acceptedTerms) {
            _state.value = AuthUiState(error = "Accept the reporting terms before you continue")
            return
        }
        _state.value = AuthUiState(busy = true)
        viewModelScope.launch {
            when (val result = authRepository.register(fullName, nic, phone, email, address, password)) {
                is Outcome.Success -> {
                    sessionManager.signIn(result.data, UserRole.CITIZEN)
                    _state.value = AuthUiState()
                    onRegistered()
                }
                is Outcome.Failure -> _state.value = AuthUiState(error = result.message)
            }
        }
    }
}
