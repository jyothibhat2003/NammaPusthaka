package com.example.nammapustaka.ui.screens

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nammapustaka.data.model.Book
import com.example.nammapustaka.ui.components.AppScreen
import com.example.nammapustaka.ui.components.EmptyState
import com.example.nammapustaka.ui.components.PrimaryActionButton
import com.example.nammapustaka.ui.components.StatusPill
import com.example.nammapustaka.viewmodel.BookViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun BookListScreen(
    viewModel: BookViewModel = viewModel(),
    canManageBooks: Boolean = false,
    currentUserId: String = "",
    isAdmin: Boolean = false,
    onAddBook: () -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    val books by viewModel.books.observeAsState(emptyList())
    var search by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.clearSearch()
        viewModel.fetchBooks()
    }

    AppScreen(
        title = "Books",
        subtitle = if (canManageBooks) "Catalog and QR inventory" else "Catalog availability",
        onBack = onBack
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (canManageBooks) {
                PrimaryActionButton(
                    text = "Add Book",
                    icon = Icons.Outlined.Add,
                    onClick = onAddBook
                )
            }

            OutlinedTextField(
                value = search,
                onValueChange = {
                    search = it
                    viewModel.searchBooks(it)
                },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                label = { Text("Search books") },
                singleLine = true
            )

            if (books.isEmpty()) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    EmptyState(
                        title = "No books found",
                        message = if (search.isBlank()) {
                            "Add your first book to start the catalog."
                        } else {
                            "Try a different search."
                        },
                        icon = Icons.Outlined.AutoStories
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(books, key = { it.bookId }) { book ->
                        BookRow(
                            book = book,
                            canManageBooks = canManageBooks,
                            canDelete = isAdmin || book.createdBy == currentUserId,
                            onDelete = { viewModel.deleteBook(book.bookId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookRow(
    book: Book,
    canManageBooks: Boolean,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Icon(
                        Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(26.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (canDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "Delete book",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusPill("${book.availableCopies}/${book.totalCopies} available")
                if (book.category.isNotBlank()) {
                    StatusPill(book.category)
                }
            }

            if (canManageBooks) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BookQrCode(book.bookId)
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.QrCodeScanner,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Book QR",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                        Text(
                            text = book.bookId,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookQrCode(bookId: String) {
    if (bookId.isBlank()) return

    val bitmap = remember(bookId) {
        val size = 220
        val bits = QRCodeWriter().encode(bookId, BarcodeFormat.QR_CODE, size, size)
        Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).apply {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Book QR code",
            modifier = Modifier
                .padding(8.dp)
                .size(104.dp)
        )
    }
}
