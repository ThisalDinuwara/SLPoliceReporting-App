package com.slpolice.reporting.ui.screens.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slpolice.reporting.data.local.MessageEntity
import com.slpolice.reporting.data.prefs.SessionManager
import com.slpolice.reporting.data.repository.ReportRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class InboxState(
    val messages: List<MessageEntity> = emptyList(),
    val loading: Boolean = true
) {
    val unread: Int get() = messages.count { !it.isRead }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MessagesViewModel(
    private val reportRepository: ReportRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val state: StateFlow<InboxState> = sessionManager.session
        .flatMapLatest { session ->
            session.userId?.let { reportRepository.inbox(it) } ?: flowOf(emptyList())
        }
        .map { InboxState(messages = it, loading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InboxState())

    fun open(id: Long) {
        viewModelScope.launch { reportRepository.markMessageRead(id) }
    }

    fun markAllRead() {
        viewModelScope.launch {
            val userId = sessionManager.session.first().userId ?: return@launch
            reportRepository.markInboxRead(userId)
        }
    }
}
