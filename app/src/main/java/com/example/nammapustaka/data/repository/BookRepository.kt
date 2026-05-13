package com.example.nammapustaka.data.repository

import com.example.nammapustaka.data.model.Book
import com.example.nammapustaka.data.model.IssueHistory
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class BookRepository {

    private val db = FirebaseFirestore.getInstance()

    fun addBook(
        book: Book,
        createdBy: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val id = db.collection("books").document().id
        val newBook = book.copy(
            bookId = id,
            createdBy = createdBy,
            createdAt = System.currentTimeMillis()
        )

        db.collection("books").document(id)
            .set(newBook)
            .addOnSuccessListener { onResult(true, "Book added") }
            .addOnFailureListener { error ->
                onResult(false, error.message ?: "Unable to add book")
            }
    }

    fun getBooks(
        onResult: (List<Book>) -> Unit,
        onError: (String) -> Unit = {}
    ): ListenerRegistration {
        return db.collection("books")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.message ?: "Unable to load books")
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    onError("Unable to load books")
                    return@addSnapshotListener
                }

                val skipped = mutableListOf<String>()
                val list = snapshot.documents.mapNotNull { document ->
                    document.toBookOrNull(
                        onInvalid = { message -> skipped.add(message) }
                    )
                }

                if (skipped.isNotEmpty()) {
                    onError("Skipped ${skipped.size} invalid book record(s). Check Firestore data format.")
                }

                onResult(list.sortedBy { it.title.lowercase() })
            }
    }

    fun getIssueHistory(
        userId: String?,
        isAdmin: Boolean,
        onResult: (List<IssueHistory>) -> Unit,
        onError: (String) -> Unit = {}
    ): ListenerRegistration {
        val query = if (isAdmin) {
            db.collection("issueHistory")
                .orderBy("createdAt", Query.Direction.DESCENDING)
        } else {
            db.collection("issueHistory")
                .whereEqualTo("userId", userId.orEmpty())
        }

        return query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error.message ?: "Unable to load issue history")
                return@addSnapshotListener
            }

            if (snapshot == null) {
                onError("Unable to load issue history")
                return@addSnapshotListener
            }

            val list = snapshot.documents.mapNotNull {
                it.toObject(IssueHistory::class.java)
            }

            onResult(list.sortedByDescending { it.createdAt })
        }
    }

    fun updateBook(book: Book) {
        db.collection("books").document(book.bookId).set(book)
    }

    fun deleteBook(bookId: String) {
        db.collection("books").document(bookId).delete()
    }

    fun issueOrReturnBook(
        bookId: String,
        userId: String,
        onResult: (String) -> Unit
    ) {
        val bookRef = db.collection("books").document(bookId)
        val userRef = db.collection("users").document(userId)
        val historyRef = db.collection("issueHistory").document()

        db.runTransaction { transaction ->
            val bookSnapshot = transaction.get(bookRef)
            val userSnapshot = transaction.get(userRef)

            if (!bookSnapshot.exists()) {
                return@runTransaction "Book not found"
            }

            if (!userSnapshot.exists()) {
                return@runTransaction "User not found"
            }

            val book = bookSnapshot.toBookOrNull() ?: return@runTransaction "Book not found"
            val available = bookSnapshot.getLong("availableCopies")?.toInt() ?: 0
            val borrowed = (userSnapshot.get("borrowedBooks") as? List<*>)
                ?.filterIsInstance<String>()
                ?: emptyList()
            val userEmail = userSnapshot.getString("email").orEmpty()

            if (borrowed.contains(bookId)) {
                transaction.update(bookRef, "availableCopies", available + 1)
                transaction.update(userRef, "borrowedBooks", borrowed - bookId)
                transaction.set(
                    historyRef,
                    IssueHistory(
                        historyId = historyRef.id,
                        bookId = bookId,
                        bookTitle = book.title,
                        userId = userId,
                        userEmail = userEmail,
                        action = "returned",
                        createdAt = System.currentTimeMillis()
                    )
                )
                "Book returned"
            } else if (available > 0) {
                transaction.update(bookRef, "availableCopies", available - 1)
                transaction.update(userRef, "borrowedBooks", borrowed + bookId)
                transaction.set(
                    historyRef,
                    IssueHistory(
                        historyId = historyRef.id,
                        bookId = bookId,
                        bookTitle = book.title,
                        userId = userId,
                        userEmail = userEmail,
                        action = "issued",
                        createdAt = System.currentTimeMillis()
                    )
                )
                "Book issued"
            } else {
                "No copies available"
            }
        }.addOnSuccessListener { result ->
            onResult(result)
        }.addOnFailureListener {
            onResult("Error: ${it.message}")
        }
    }

    private fun DocumentSnapshot.toBookOrNull(
        onInvalid: (String) -> Unit = {}
    ): Book? {
        val normalizedId = id
        val title = getString("title").orEmpty().trim()
        val author = getString("author").orEmpty().trim()
        val totalCopies = numberAsInt("totalCopies")
        val availableCopies = numberAsInt("availableCopies")

        if (normalizedId.isBlank() || title.isBlank() || author.isBlank()) {
            onInvalid("Book $id is missing an id, title, or author")
            return null
        }

        if (totalCopies == null || availableCopies == null) {
            onInvalid("Book $id has invalid copy counts")
            return null
        }

        return Book(
            bookId = normalizedId,
            title = title,
            author = author,
            description = getString("description").orEmpty(),
            category = getString("category").orEmpty(),
            totalCopies = totalCopies.coerceAtLeast(0),
            availableCopies = availableCopies.coerceIn(0, totalCopies.coerceAtLeast(0)),
            createdBy = getString("createdBy").orEmpty(),
            createdAt = createdAtMillis()
        )
    }

    private fun DocumentSnapshot.numberAsInt(field: String): Int? {
        return when (val value = get(field)) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull()
            else -> null
        }
    }

    private fun DocumentSnapshot.createdAtMillis(): Long {
        return when (val value = get("createdAt")) {
            is Number -> value.toLong()
            is Timestamp -> value.toDate().time
            else -> 0L
        }
    }
}
