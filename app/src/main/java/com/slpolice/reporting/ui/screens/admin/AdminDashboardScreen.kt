package com.slpolice.reporting.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slpolice.reporting.data.CaseGroup
import com.slpolice.reporting.data.ReportStatus
import com.slpolice.reporting.ui.AppViewModelProvider
import com.slpolice.reporting.ui.components.EmptyState
import com.slpolice.reporting.ui.components.Eyebrow
import com.slpolice.reporting.ui.components.ReportCard
import com.slpolice.reporting.ui.components.statusColor
import com.slpolice.reporting.ui.theme.BraidGold
import com.slpolice.reporting.ui.theme.Hairline
import com.slpolice.reporting.ui.theme.InkSoft
import com.slpolice.reporting.ui.theme.Navy
import com.slpolice.reporting.ui.theme.StatusRejected
import com.slpolice.reporting.ui.theme.TagStyle

@Composable
fun AdminDashboardScreen(
    onOpenReport: (String) -> Unit,
    onOpenPanel: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: AdminViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                OfficerHeader(
                    name = state.officerName,
                    badge = state.badge,
                    station = state.station,
                    queue = state.countOf(ReportStatus.SUBMITTED),
                    critical = state.criticalCount,
                    onOpenPanel = onOpenPanel,
                    onSignOut = { viewModel.signOut(onSignedOut) }
                )
            }

            item {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::setQuery,
                    placeholder = { Text("Search reference, place or vehicle") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = InkSoft) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }

            item {
                Column {
                    Eyebrow(
                        "Divisions",
                        modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp)
                    ) {
                        item {
                            FilterPill(
                                label = "Every division",
                                count = state.reports.size,
                                selected = state.division == null,
                                color = Navy,
                                onClick = { viewModel.setDivision(null) }
                            )
                        }
                        items(CaseGroup.entries) { group ->
                            FilterPill(
                                label = group.label,
                                count = state.countOf(group),
                                selected = state.division == group,
                                color = Navy,
                                onClick = { viewModel.setDivision(group) }
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Eyebrow(
                        "Status",
                        modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
                    )
                }
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp)
                ) {
                    item {
                        FilterPill(
                            label = "All",
                            count = state.reports.size,
                            selected = state.filter == null,
                            color = Navy,
                            onClick = { viewModel.setFilter(null) }
                        )
                    }
                    items(ReportStatus.entries) { status ->
                        FilterPill(
                            label = status.label,
                            count = state.countOf(status),
                            selected = state.filter == status,
                            color = statusColor(status),
                            onClick = { viewModel.setFilter(status) }
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            val visible = state.visible
            if (visible.isEmpty() && !state.loading) {
                item {
                    EmptyState(
                        icon = Icons.Filled.FolderOff,
                        title = "No matching cases",
                        message = "Change the filter or clear the search to see the rest of the queue."
                    )
                }
            }

            items(visible, key = { it.report.id }) { entry ->
                ReportCard(
                    entry = entry,
                    showPriority = true,
                    onClick = { onOpenReport(entry.report.id) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun OfficerHeader(
    name: String,
    badge: String,
    station: String,
    queue: Int,
    critical: Int,
    onOpenPanel: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Navy)
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Eyebrow("Restricted \u00b7 police channel", color = BraidGold)
                Spacer(Modifier.height(6.dp))
                Text(
                    name.ifBlank { "Officer" },
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    listOf(badge, station).filter { it.isNotBlank() }.joinToString("  \u00b7  "),
                    style = TagStyle,
                    color = Color.White.copy(alpha = 0.62f)
                )
            }
            IconButton(onClick = onOpenPanel) {
                Icon(
                    Icons.Filled.InsertChart,
                    contentDescription = "Open the admin panel",
                    tint = Color.White
                )
            }
            IconButton(onClick = onSignOut) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Sign out",
                    tint = Color.White
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(12.dp)
            ) {
                Column {
                    Text("$queue", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    Text(
                        "Waiting for review",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(StatusRejected.copy(alpha = 0.22f))
                    .padding(12.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.PriorityHigh,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text("$critical", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    }
                    Text(
                        "Critical and open",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    count: Int,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) color.copy(alpha = 0.14f) else Color.Transparent)
            .border(1.dp, if (selected) color else Hairline, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) color else InkSoft
        )
        Spacer(Modifier.width(6.dp))
        Text("$count", style = TagStyle, color = if (selected) color else InkSoft)
    }
}
