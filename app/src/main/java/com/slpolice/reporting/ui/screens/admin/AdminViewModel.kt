package com.slpolice.reporting.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slpolice.reporting.data.CaseGroup
import com.slpolice.reporting.data.Priority
import com.slpolice.reporting.data.ReportCategory
import com.slpolice.reporting.data.group
import com.slpolice.reporting.data.ReportStatus
import com.slpolice.reporting.data.local.ReportWithEvidence
import com.slpolice.reporting.data.prefs.SessionManager
import com.slpolice.reporting.data.repository.AuthRepository
import com.slpolice.reporting.data.repository.ReportRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AdminState(
    val officerName: String = "",
    val station: String = "",
    val badge: String = "",
    val reports: List<ReportWithEvidence> = emptyList(),
    val filter: ReportStatus? = null,
    val division: CaseGroup? = null,
    val query: String = "",
    val loading: Boolean = true
) {
    val visible: List<ReportWithEvidence>
        get() = reports.filter { entry ->
            val matchesStatus = filter == null || entry.report.status == filter.name
            val matchesDivision = division == null ||
                ReportCategory.from(entry.report.category).group == division
            val text = query.trim().lowercase()
            val matchesQuery = text.isEmpty() ||
                entry.report.referenceNo.lowercase().contains(text) ||
                entry.report.title.lowercase().contains(text) ||
                entry.report.locationName.lowercase().contains(text) ||
                entry.report.vehicleNumber?.lowercase()?.contains(text) == true ||
                ReportCategory.from(entry.report.category).label.lowercase().contains(text)
            matchesStatus && matchesDivision && matchesQuery
        }

    fun countOf(status: ReportStatus): Int = reports.count { it.report.status == status.name }

    /** How many cases sit with a division, honouring the division filter's own scope. */
    fun countOf(group: CaseGroup): Int =
        reports.count { ReportCategory.from(it.report.category).group == group }

    val criticalCount: Int
        get() = reports.count {
            it.report.priority == Priority.CRITICAL.name &&
                it.report.status != ReportStatus.ACTION_TAKEN.name
        }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AdminViewModel(
    authRepository: AuthRepository,
    reportRepository: ReportRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val filter = MutableStateFlow<ReportStatus?>(null)
    private val division = MutableStateFlow<CaseGroup?>(null)
    private val query = MutableStateFlow("")

    private val officerFlow = sessionManager.session.flatMapLatest { session ->
        session.userId?.let { authRepository.observeUser(it) } ?: flowOf(null)
    }

    val state: StateFlow<AdminState> = combine(
        officerFlow,
        reportRepository.allReports(),
        filter,
        division,
        query
    ) { officer, reports, status, group, text ->
        AdminState(
            officerName = officer?.fullName.orEmpty(),
            station = officer?.station.orEmpty(),
            badge = officer?.badgeNumber.orEmpty(),
            reports = reports,
            filter = status,
            division = group,
            query = text,
            loading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdminState())

    fun setFilter(status: ReportStatus?) {
        filter.value = status
    }

    fun setQuery(text: String) {
        query.value = text
    }

    fun setDivision(group: CaseGroup?) {
        division.value = group
    }

    fun signOut(onDone: () -> Unit) {
        viewModelScope.launch {
            sessionManager.signOut()
            onDone()
        }
    }
}
