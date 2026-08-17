package com.slpolice.reporting.ui.screens.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.slpolice.reporting.ui.AppViewModelProvider
import com.slpolice.reporting.ui.components.EmptyState
import com.slpolice.reporting.ui.components.Eyebrow
import com.slpolice.reporting.ui.components.ReportCard
import com.slpolice.reporting.ui.theme.BraidGold
import com.slpolice.reporting.ui.theme.InkSoft
import com.slpolice.reporting.ui.theme.Navy

@Composable
fun HomeScreen(
    onNewReport: () -> Unit,
    onOpenReport: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenMessages: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewReport,
                containerColor = Navy,
                contentColor = Color.White,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("File a report") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                CitizenHeader(
                    name = state.displayName,
                    nic = state.nic,
                    total = state.total,
                    open = state.open,
                    resolved = state.resolved,
                    unread = state.unreadMessages,
                    onOpenProfile = onOpenProfile,
                    onOpenMessages = onOpenMessages
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Eyebrow("Your reports")
                    if (state.total > 0) {
                        Text(
                            "${state.total} filed",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSoft
                        )
                    }
                }
            }

            if (state.reports.isEmpty() && !state.loading) {
                item {
                    EmptyState(
                        icon = Icons.Filled.Inbox,
                        title = "Nothing filed yet",
                        message = "When you witness a traffic offence or another incident, capture it and send the evidence straight to the department.",
                        actionLabel = "File your first report",
                        onAction = onNewReport
                    )
                }
            }

            items(state.reports, key = { it.report.id }) { entry ->
                ReportCard(
                    entry = entry,
                    onClick = { onOpenReport(entry.report.id) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun CitizenHeader(
    name: String,
    nic: String,
    total: Int,
    open: Int,
    resolved: Int,
    unread: Int,
    onOpenProfile: () -> Unit,
    onOpenMessages: () -> Unit
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
                Eyebrow("Verified reporter", color = BraidGold)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = name.ifBlank { "Reporter" },
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "NIC $nic",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f)
                )
            }
            Box {
                IconButton(onClick = onOpenMessages) {
                    Icon(
                        Icons.Filled.MailOutline,
                        contentDescription = "Open your messages",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                if (unread > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 4.dp, end = 4.dp)
                            .size(18.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(BraidGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (unread > 9) "9+" else unread.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Navy
                        )
                    }
                }
            }
            IconButton(onClick = onOpenProfile) {
                Icon(
                    Icons.Filled.AccountCircle,
                    contentDescription = "Open your profile",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("Filed", total, Modifier.weight(1f))
            StatTile("With police", open, Modifier.weight(1f))
            StatTile("Acted on", resolved, Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatTile(label: String, value: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Column {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.65f)
            )
        }
    }
}
