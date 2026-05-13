package com.example.nammapustaka.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nammapustaka.data.model.Book
import com.example.nammapustaka.ui.components.AppScreen
import com.example.nammapustaka.ui.components.PrimaryActionButton
import com.example.nammapustaka.viewmodel.BookViewModel

@Composable
fun AddBookScreen(
    viewModel: BookViewModel = viewModel(),
    userId: String = "",
    onBack: (() -> Unit)? = null,
    onBookAdded: () -> Unit = {}
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var totalCopies by remember { mutableStateOf("") }
    var validationMessage by remember { mutableStateOf("") }

    AppScreen(
        title = "Add Book",
        subtitle = "Create catalog entry",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Outlined.Title, contentDescription = null) },
                label = { Text("Title") },
                singleLine = true
            )

            OutlinedTextField(
                value = author,
                onValueChange = { author = it },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                label = { Text("Author") },
                singleLine = true
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Outlined.Category, contentDescription = null) },
                label = { Text("Category") },
                singleLine = true
            )

            OutlinedTextField(
                value = totalCopies,
                onValueChange = { totalCopies = it.filter(Char::isDigit) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Outlined.Numbers, contentDescription = null) },
                label = { Text("Copies") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Outlined.Description, contentDescription = null) },
                label = { Text("Description") },
                minLines = 3
            )

            if (validationMessage.isNotBlank()) {
                Text(
                    text = validationMessage,
                    color = if (validationMessage == "Saving...") {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            PrimaryActionButton(
                text = "Save Book",
                icon = Icons.Outlined.Save,
                enabled = validationMessage != "Saving..."
            ) {
                val copies = totalCopies.toIntOrNull() ?: 0

                if (title.isBlank() || author.isBlank() || copies <= 0) {
                    validationMessage = "Enter title, author, and a valid copy count"
                } else if (userId.isBlank()) {
                    validationMessage = "Please login again before adding books"
                } else {
                    val book = Book(
                        title = title.trim(),
                        author = author.trim(),
                        description = description.trim(),
                        category = category.trim(),
                        totalCopies = copies,
                        availableCopies = copies
                    )

                    validationMessage = "Saving..."
                    viewModel.addBook(book, userId) { success, message ->
                        if (success) {
                            onBookAdded()
                        } else {
                            validationMessage = message
                        }
                    }
                }
            }
        }
    }
}
