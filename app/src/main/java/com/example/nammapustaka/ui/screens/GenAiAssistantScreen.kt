package com.example.nammapustaka.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nammapustaka.ui.components.AppScreen
import com.example.nammapustaka.ui.components.PrimaryActionButton
import com.example.nammapustaka.ui.components.StatusPill
import com.example.nammapustaka.viewmodel.BookViewModel
import com.example.nammapustaka.viewmodel.GenAiViewModel

@Composable
fun GenAiAssistantScreen(
    bookViewModel: BookViewModel,
    genAiViewModel: GenAiViewModel = viewModel(),
    onBack: (() -> Unit)? = null
) {
    val books by bookViewModel.catalogBooks.observeAsState(emptyList())
    val answer by genAiViewModel.answer.observeAsState("")
    val loading by genAiViewModel.loading.observeAsState(false)
    var question by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        bookViewModel.fetchBooks()
    }

    AppScreen(
        title = "AI Assistant",
        subtitle = "Catalog-aware support",
        onBack = onBack,
        trailing = {
            StatusPill(
                text = "${books.size} books",
                icon = Icons.Outlined.AutoAwesome
            )
        }
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = question,
                onValueChange = { question = it },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Outlined.SmartToy, contentDescription = null) },
                label = { Text("Ask the library") },
                minLines = 3
            )

            PrimaryActionButton(
                text = if (loading) "Thinking..." else "Ask AI",
                icon = Icons.Outlined.Send,
                enabled = !loading && question.isNotBlank()
            ) {
                genAiViewModel.ask(question, books)
            }

            if (answer.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
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
                        StatusPill("Answer", Icons.Outlined.AutoAwesome)
                        Text(answer, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}
