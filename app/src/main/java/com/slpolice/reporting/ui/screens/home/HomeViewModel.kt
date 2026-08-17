package com.slpolice.reporting.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slpolice.reporting.data.ReportStatus
import com.slpolice.reporting.data.local.ReportWithEvidence
import com.slpolice.reporting.data.prefs.SessionManager
import com.slpolice.reporting.data.repository.AuthRepository
import com.slpolice.reporting.data.repository.ReportRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CitizenHomeState(
    val displayName: String = "",
    val nic: String = "",
    val reports: List<ReportWithEvidence> = emptyList(),
    val unreadMessages: Int = 0,
    val loading: Boolean = true
) {
    val total: Int get() = reports.size
    val open: Int get() = reports.count {
        ReportStatus.from(it.report.status) in setOf(ReportStatus.SUBMITTED, ReportStatus.UNDER_REVIEW)
    }
    val resolved: Int get() = reports.count {
        ReportStatus.from(it.report.status) == ReportStatus.ACTION_TAKEN
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    authRepository: AuthRepository,
    private val reportRepository: ReportRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val userFlow = sessionManager.session.flatMapLatest { session ->
        session.userId?.let { authRepository.observeUser(it) } ?: flowOf(null)
    }

    private val reportsFlow: Flow<List<ReportWithEvidence>> =
        sessionManager.session.flatMapLatest { session ->
            session.userId?.let { reportRepository.myReports(it) } ?: flowOf(emptyList())
        }

    private val unreadFlow: Flow<Int> = sessionManager.session.flatMapLatest { session ->
        session.userId?.let { reportRepository.unreadCount(it) } ?: flowOf(0)
    }

    val state: kotlinx.coroutines.flow.StateFlow<CitizenHomeState> =
        kotlinx.coroutines.flow.combine(userFlow, reportsFlow, unreadFlow) { user, reports, unread ->
            CitizenHomeState(
                displayName = user?.fullName.orEmpty(),
                nic = user?.nic.orEmpty(),
                reports = reports,
                unreadMessages = unread,
                loading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CitizenHomeState()
        )

    val greeting: kotlinx.coroutines.flow.StateFlow<String> = userFlow
        .map { it?.fullName?.split(" ")?.lastOrNull().orEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    fun signOut(onDone: () -> Unit) {
        viewModelScope.launch {
            sessionManager.signOut()
            onDone()
        }
    }
}
