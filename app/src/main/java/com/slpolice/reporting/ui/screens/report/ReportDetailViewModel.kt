package com.slpolice.reporting.ui.screens.report

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slpolice.reporting.data.Outcome
import com.slpolice.reporting.data.Priority
import com.slpolice.reporting.data.ReportStatus
import com.slpolice.reporting.data.UserRole
import com.slpolice.reporting.data.local.AuditLogEntity
import com.slpolice.reporting.data.local.ReportWithEvidence
import com.slpolice.reporting.data.prefs.SessionManager
import com.slpolice.reporting.data.repository.AuthRepository
import com.slpolice.reporting.data.repository.ReportRepository
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

/** Reporter identity as it may be shown, honouring the witness protection setting. */
data class ReporterCard(
    val name: String,
    val nic: String,
    val phone: String,
    val protected: Boolean
)

data class ReportDetailState(
    val report: ReportWithEvidence? = null,
    val trail: List<AuditLogEntity> = emptyList(),
    val reporter: ReporterCard? = null,
    val viewerRole: UserRole = UserRole.CITIZEN,
    val loading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class ReportDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val reportRepository: ReportRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val reportId: String = checkNotNull(savedStateHandle["reportId"])

    private val reportFlow = reportRepository.report(reportId)

    private val trailFlow = reportFlow.flatMapLatest { entry ->
        entry?.let { reportRepository.trailFor(it.report.referenceNo) } ?: flowOf(emptyList())
    }

    private val reporterFlow = reportFlow.flatMapLatest { entry ->
        if (entry == null) flowOf(null) else authRepository.observeUser(entry.report.reporterId)
    }

    val state: StateFlow<ReportDetailState> =
        combine(
            reportFlow,
            trailFlow,
            reporterFlow,
            sessionManager.session
        ) { report, trail, reporter, session ->
            val anonymous = report?.report?.anonymous == true
            ReportDetailState(
                report = report,
                trail = trail,
                reporter = reporter?.let {
                    if (anonymous) {
                        ReporterCard(
                            name = "Protected reporter",
                            nic = maskNic(it.nic),
                            phone = "Withheld",
                            protected = true
                        )
                    } else {
                        ReporterCard(it.fullName, it.nic, it.phone, protected = false)
                    }
                },
                viewerRole = session.role ?: UserRole.CITIZEN,
                loading = false
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportDetailState())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    fun updateStatus(status: ReportStatus, note: String) {
        viewModelScope.launch {
            val session = sessionManager.session.first()
            val officerId = session.userId ?: return@launch
            val officer = authRepository.findUser(officerId)
            val result = reportRepository.updateStatus(
                reportId = reportId,
                officerId = officerId,
                officerName = officer?.fullName.orEmpty(),
                status = status,
                note = note
            )
            _message.value = when (result) {
                is Outcome.Success -> "Status updated to ${status.label}"
                is Outcome.Failure -> result.message
            }
        }
    }

    fun setPriority(priority: Priority) {
        viewModelScope.launch {
            val session = sessionManager.session.first()
            val officerId = session.userId ?: return@launch
            val officer = authRepository.findUser(officerId)
            reportRepository.setPriority(reportId, officerId, officer?.fullName.orEmpty(), priority)
            _message.value = "Priority set to ${priority.label}"
        }
    }

    /** Officer writes directly to the reporter's inbox. */
    fun sendMessage(text: String) {
        viewModelScope.launch {
            val session = sessionManager.session.first()
            val officerId = session.userId ?: return@launch
            val officer = authRepository.findUser(officerId)
            val result = reportRepository.sendMessage(
                reportId = reportId,
                officerId = officerId,
                officerName = officer?.fullName.orEmpty(),
                text = text
            )
            _message.value = when (result) {
                is Outcome.Success -> "Message delivered to the reporter"
                is Outcome.Failure -> result.message
            }
        }
    }

    fun withdraw(onWithdrawn: () -> Unit) {
        viewModelScope.launch {
            val session = sessionManager.session.first()
            val userId = session.userId ?: return@launch
            val user = authRepository.findUser(userId)
            when (val result = reportRepository.withdraw(reportId, userId, user?.fullName.orEmpty())) {
                is Outcome.Success -> onWithdrawn()
                is Outcome.Failure -> _message.value = result.message
            }
        }
    }

    private fun maskNic(nic: String): String =
        if (nic.length <= 4) "****" else "*".repeat(nic.length - 4) + nic.takeLast(4)
}
