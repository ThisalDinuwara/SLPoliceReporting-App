package com.slpolice.reporting.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slpolice.reporting.data.Outcome
import com.slpolice.reporting.data.UserRole
import com.slpolice.reporting.data.local.UserEntity
import com.slpolice.reporting.data.prefs.SessionManager
import com.slpolice.reporting.data.repository.AuthRepository
import com.slpolice.reporting.data.repository.ReportRepository
import com.slpolice.reporting.util.Validators
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileState(
    val user: UserEntity? = null,
    val reportCount: Int = 0,
    val loading: Boolean = true
) {
    val role: UserRole get() = UserRole.from(user?.role.orEmpty())
    val nicProfile get() = user?.nic?.let { Validators.decodeNic(it) }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val authRepository: AuthRepository,
    reportRepository: ReportRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val userFlow = sessionManager.session.flatMapLatest { session ->
        session.userId?.let { authRepository.observeUser(it) } ?: flowOf(null)
    }

    private val countFlow = sessionManager.session.flatMapLatest { session ->
        session.userId?.let { reportRepository.myReportCount(it) } ?: flowOf(0)
    }

    val state: StateFlow<ProfileState> = combine(userFlow, countFlow) { user, count ->
        ProfileState(user = user, reportCount = count, loading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileState())

    private val _message = MutableStateFlow<Pair<String, Boolean>?>(null)

    /** Message text paired with a flag that is true when it reports a failure. */
    val message: StateFlow<Pair<String, Boolean>?> = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    fun changePassword(current: String, replacement: String, confirm: String) {
        if (replacement != confirm) {
            _message.value = "Both new passwords must match" to true
            return
        }
        viewModelScope.launch {
            val userId = sessionManager.session.first().userId ?: return@launch
            _message.value = when (val result = authRepository.changePassword(userId, current, replacement)) {
                is Outcome.Success -> "Password changed" to false
                is Outcome.Failure -> result.message to true
            }
        }
    }

    fun signOut(onDone: () -> Unit) {
        viewModelScope.launch {
            sessionManager.signOut()
            onDone()
        }
    }
}
