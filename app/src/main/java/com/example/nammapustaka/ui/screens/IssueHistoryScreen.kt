package com.example.nammapustaka.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AssignmentReturn
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nammapustaka.data.model.IssueHistory
import com.example.nammapustaka.ui.components.AppScreen
import com.example.nammapustaka.ui.components.EmptyState
import com.example.nammapustaka.ui.components.StatusPill
import com.example.nammapustaka.viewmodel.BookViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun IssueHistoryScreen(
    viewModel: BookViewModel = viewModel(),
    userId: String,
    isAdmin: Boolean,
    onBack: (() -> Unit)? = null
) {
    val history by viewModel.issueHistory.observeAsState(emptyList())

    LaunchedEffect(userId, isAdmin) {
        viewModel.fetchIssueHistory(userId, isAdmin)
    }

    AppScreen(
        title = if (isAdmin) "Issue History" else "My History",
        subtitle = if (isAdmin) "All circulation events" else "Your circulation events",
        onBack = onBack
    ) {
        if (history.isEmpty()) {
            EmptyState(
                title = "No history yet",
                message = "Issue and return activity will appear here.",
                icon = Icons.Outlined.History
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(history, key = { it.historyId }) { item ->
                    HistoryRow(item = item, isAdmin = isAdmin)
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(
    item: IssueHistory,
    isAdmin: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                item.bookTitle.ifBlank { item.bookId },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusPill(
                    text = item.action.replaceFirstChar { it.uppercase() },
                    icon = Icons.Outlined.AssignmentReturn
                )
                StatusPill(
                    text = formatTime(item.createdAt),
                    icon = Icons.Outlined.Schedule
                )
            }

            if (isAdmin && item.userEmail.isNotBlank()) {
                StatusPill(
                    text = item.userEmail,
                    icon = Icons.Outlined.Person
                )
            }
        }
    }
}

private fun formatTime(value: Long): String {
    if (value <= 0L) return "-"
    return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(value))
}
