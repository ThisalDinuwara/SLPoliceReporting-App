package com.slpolice.reporting.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slpolice.reporting.data.CaseGroup
import com.slpolice.reporting.data.Priority
import com.slpolice.reporting.data.ReportCategory
import com.slpolice.reporting.data.ReportStatus
import com.slpolice.reporting.ui.AppViewModelProvider
import com.slpolice.reporting.ui.components.Eyebrow
import com.slpolice.reporting.ui.components.PrimaryButton
import com.slpolice.reporting.ui.components.SectionCard
import com.slpolice.reporting.ui.components.statusColor
import com.slpolice.reporting.ui.theme.BraidGold
import com.slpolice.reporting.ui.theme.Hairline
import com.slpolice.reporting.ui.theme.InkSoft
import com.slpolice.reporting.ui.theme.Navy
import com.slpolice.reporting.ui.theme.StatusAction
import com.slpolice.reporting.ui.theme.TagStyle
import com.slpolice.reporting.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onBack: () -> Unit,
    onCreateAccount: () -> Unit,
    viewModel: AdminPanelViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Admin panel") },
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
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile("Reports", state.totalReports.toString(), Modifier.weight(1f))
                MetricTile("Reporters", state.citizens.size.toString(), Modifier.weight(1f))
                MetricTile("Evidence", state.totalEvidence.toString(), Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile("Cleared", "${state.clearanceRate}%", Modifier.weight(1f), StatusAction)
                MetricTile("Critical", state.countOf(Priority.CRITICAL).toString(), Modifier.weight(1f))
                MetricTile("Protected", state.protectedReports.toString(), Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))

            SectionCard {
                Eyebrow("Account provisioning")
                Spacer(Modifier.height(6.dp))
                Text(
                    "Officer accounts exist only when an administrator issues them. Citizens may also be registered here on behalf of someone at the counter.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSoft
                )
                Spacer(Modifier.height(14.dp))
                PrimaryButton(text = "Create an account", onClick = onCreateAccount)
            }

            Spacer(Modifier.height(16.dp))

            Eyebrow("Division dashboards")
            Spacer(Modifier.height(4.dp))
            Text(
                "Each division sees only the offence categories it is responsible for.",
                style = MaterialTheme.typography.bodySmall,
                color = InkSoft
            )
            Spacer(Modifier.height(12.dp))
            CaseGroup.entries.forEach { group ->
                DivisionCard(summary = state.divisionSummary(group))
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(6.dp))

            SectionCard {
                Eyebrow("Case load by status")
                Spacer(Modifier.height(14.dp))
                ReportStatus.entries.forEach { status ->
                    BreakdownRow(
                        label = status.label,
                        value = state.countOf(status),
                        total = state.totalReports,
                        tint = statusColor(status)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            SectionCard {
                Eyebrow("Offence categories")
                Spacer(Modifier.height(6.dp))
                Text(
                    "Where reporting activity is concentrated across the division.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSoft
                )
                Spacer(Modifier.height(14.dp))
                if (state.categories.isEmpty()) {
                    Text("No reports filed yet", color = InkSoft, style = MaterialTheme.typography.bodyMedium)
                } else {
                    state.categories.forEach { entry ->
                        BreakdownRow(
                            label = ReportCategory.from(entry.category).label,
                            value = entry.total,
                            total = state.totalReports,
                            tint = Navy
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            SectionCard {
                Eyebrow("Registered reporters \u00b7 ${state.citizens.size}")
                Spacer(Modifier.height(6.dp))
                Text(
                    "Each account is bound to one verified NIC number.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSoft
                )
                Spacer(Modifier.height(12.dp))
                if (state.citizens.isEmpty()) {
                    Text("No citizens registered", color = InkSoft, style = MaterialTheme.typography.bodyMedium)
                }
                state.citizens.take(20).forEach { citizen ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Navy.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                citizen.fullName.take(1).uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                color = Navy
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                citizen.fullName,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(citizen.nic, style = TagStyle, color = InkSoft)
                        }
                        Text(
                            Formatters.date(citizen.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSoft
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            SectionCard {
                Eyebrow("System audit log")
                Spacer(Modifier.height(6.dp))
                Text(
                    "Append-only record of every account and case action.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSoft
                )
                Spacer(Modifier.height(12.dp))
                state.activity.take(25).forEach { entry ->
                    Row(modifier = Modifier.padding(vertical = 7.dp)) {
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(BraidGold)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                entry.action.replace('_', ' ').lowercase()
                                    .replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                entry.details,
                                style = MaterialTheme.typography.bodySmall,
                                color = InkSoft
                            )
                            Text(
                                "${entry.actorName.ifBlank { "System" }} \u00b7 ${entry.targetRef} \u00b7 ${Formatters.dateTime(entry.timestamp)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = InkSoft
                            )
                        }
                    }
                }
                if (state.activity.isEmpty()) {
                    Text("Nothing logged yet", color = InkSoft, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}

/** One division's workload, shown as its own dashboard card. */
@Composable
private fun DivisionCard(summary: DivisionSummary) {
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(summary.group.label, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(3.dp))
                Text(
                    summary.group.blurb,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSoft
                )
            }
            Text(
                summary.total.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = Navy
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DivisionStat("Open", summary.open, Modifier.weight(1f))
            DivisionStat("Cleared", summary.cleared, Modifier.weight(1f))
            DivisionStat("Critical", summary.critical, Modifier.weight(1f))
        }
    }
}

@Composable
private fun DivisionStat(label: String, value: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(Navy.copy(alpha = 0.05f))
            .padding(horizontal = 10.dp, vertical = 9.dp)
    ) {
        Text(value.toString(), style = MaterialTheme.typography.titleLarge, color = Navy)
        Text(label, style = MaterialTheme.typography.bodySmall, color = InkSoft)
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    tint: Color = Navy
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(tint.copy(alpha = 0.07f))
            .padding(14.dp)
    ) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = tint)
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = InkSoft)
    }
}

/** A labelled count with a proportional bar, so volumes are comparable at a glance. */
@Composable
private fun BreakdownRow(label: String, value: Int, total: Int, tint: Color) {
    val fraction = if (total <= 0) 0f else value.toFloat() / total.toFloat()
    Column(modifier = Modifier.padding(vertical = 7.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(Modifier.width(10.dp))
            Text("$value", style = TagStyle, color = tint)
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Hairline)
        ) {
            if (fraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(tint)
                )
            }
        }
    }
}
