package com.slpolice.reporting.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slpolice.reporting.data.CaseGroup
import com.slpolice.reporting.data.Priority
import com.slpolice.reporting.data.ReportCategory
import com.slpolice.reporting.data.ReportStatus
import com.slpolice.reporting.data.group
import com.slpolice.reporting.data.UserRole
import com.slpolice.reporting.data.local.AuditLogEntity
import com.slpolice.reporting.data.local.CategoryCount
import com.slpolice.reporting.data.local.ReportWithEvidence
import com.slpolice.reporting.data.local.UserEntity
import com.slpolice.reporting.data.prefs.SessionManager
import com.slpolice.reporting.data.repository.AuthRepository
import com.slpolice.reporting.data.repository.ReportRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class AdminPanelState(
    val reports: List<ReportWithEvidence> = emptyList(),
    val citizens: List<UserEntity> = emptyList(),
    val categories: List<CategoryCount> = emptyList(),
    val activity: List<AuditLogEntity> = emptyList(),
    val loading: Boolean = true
) {
    val totalReports: Int get() = reports.size

    val totalEvidence: Int get() = reports.sumOf { it.evidence.size }

    val protectedReports: Int get() = reports.count { it.report.anonymous }

    val clearanceRate: Int
        get() = if (reports.isEmpty()) 0 else
            reports.count { it.report.status == ReportStatus.ACTION_TAKEN.name } * 100 / reports.size

    fun countOf(status: ReportStatus): Int = reports.count { it.report.status == status.name }

    fun countOf(priority: Priority): Int = reports.count { it.report.priority == priority.name }

    val busiestCategory: CategoryCount? get() = categories.firstOrNull()

    /** Per-division rollup that drives the separate dashboards. */
    fun divisionSummary(group: CaseGroup): DivisionSummary {
        val owned = reports.filter { ReportCategory.from(it.report.category).group == group }
        return DivisionSummary(
            group = group,
            total = owned.size,
            open = owned.count {
                it.report.status == ReportStatus.SUBMITTED.name ||
                    it.report.status == ReportStatus.UNDER_REVIEW.name
            },
            cleared = owned.count { it.report.status == ReportStatus.ACTION_TAKEN.name },
            critical = owned.count { it.report.priority == Priority.CRITICAL.name }
        )
    }
}

data class DivisionSummary(
    val group: CaseGroup,
    val total: Int,
    val open: Int,
    val cleared: Int,
    val critical: Int
)

/** Read-only department overview: volumes, categories, registered reporters and the audit log. */
class AdminPanelViewModel(
    authRepository: AuthRepository,
    reportRepository: ReportRepository,
    sessionManager: SessionManager
) : ViewModel() {

    val state: StateFlow<AdminPanelState> = combine(
        reportRepository.allReports(),
        authRepository.observeCitizens(),
        reportRepository.categoryCounts(),
        reportRepository.recentActivity()
    ) { reports, citizens, categories, activity ->
        AdminPanelState(
            reports = reports,
            citizens = citizens,
            categories = categories,
            activity = activity,
            loading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdminPanelState())

    val role: UserRole = UserRole.OFFICER
}
