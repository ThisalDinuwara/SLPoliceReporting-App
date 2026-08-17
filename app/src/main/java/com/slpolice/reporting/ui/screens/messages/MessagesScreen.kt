package com.slpolice.reporting.ui.screens.messages

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slpolice.reporting.data.MessageKind
import com.slpolice.reporting.data.local.MessageEntity
import com.slpolice.reporting.ui.AppViewModelProvider
import com.slpolice.reporting.ui.components.EmptyState
import com.slpolice.reporting.ui.components.Eyebrow
import com.slpolice.reporting.ui.components.ReferenceTag
import com.slpolice.reporting.ui.theme.BraidGold
import com.slpolice.reporting.ui.theme.Hairline
import com.slpolice.reporting.ui.theme.InkSoft
import com.slpolice.reporting.ui.theme.Navy
import com.slpolice.reporting.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    onBack: () -> Unit,
    onOpenReport: (String) -> Unit,
    viewModel: MessagesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Messages") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
                actions = {
                    if (state.unread > 0) {
                        IconButton(onClick = viewModel::markAllRead) {
                            Icon(
                                Icons.Filled.MarkEmailRead,
                                contentDescription = "Mark everything as read"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            if (state.messages.isEmpty() && !state.loading) {
                item {
                    EmptyState(
                        icon = Icons.Filled.MailOutline,
                        title = "No messages yet",
                        message = "When you file a report the department confirms it here, and an officer writes to you whenever the case moves."
                    )
                }
            }

            items(state.messages, key = { it.id }) { message ->
                MessageCard(
                    message = message,
                    onOpened = { viewModel.open(message.id) },
                    onOpenReport = onOpenReport,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun MessageCard(
    message: MessageEntity,
    onOpened: () -> Unit,
    onOpenReport: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val kind = MessageKind.from(message.kind)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, if (message.isRead) Hairline else BraidGold, RoundedCornerShape(14.dp))
            .clickable {
                expanded = !expanded
                if (!message.isRead) onOpened()
            }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Navy),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Shield,
                    contentDescription = null,
                    tint = BraidGold,
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Eyebrow(kind.label)
                Spacer(Modifier.height(3.dp))
                Text(message.senderName, style = MaterialTheme.typography.bodySmall, color = InkSoft)
            }
            if (!message.isRead) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(BraidGold)
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(message.title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(
            text = message.body,
            style = MaterialTheme.typography.bodyMedium,
            color = InkSoft,
            maxLines = if (expanded) Int.MAX_VALUE else 2
        )

        AnimatedVisibility(visible = expanded) {
            Column {
                Spacer(Modifier.height(14.dp))
                message.referenceNo?.let { reference ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ReferenceTag(reference)
                        message.reportId?.let { id ->
                            TextButton(onClick = { onOpenReport(id) }) {
                                Text("Open case", color = Navy)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            Formatters.relative(message.createdAt),
            style = MaterialTheme.typography.bodySmall,
            color = InkSoft
        )
    }
}
