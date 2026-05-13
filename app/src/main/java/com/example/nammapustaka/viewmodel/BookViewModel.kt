package com.example.nammapustaka.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nammapustaka.data.model.Book
import com.example.nammapustaka.data.model.IssueHistory
import com.example.nammapustaka.data.repository.BookRepository
import com.google.firebase.firestore.ListenerRegistration

class BookViewModel : ViewModel() {

    private val repo = BookRepository()
    private var allBooks: List<Book> = emptyList()
    private var searchQuery: String = ""
    private var booksRegistration: ListenerRegistration? = null
    private var historyRegistration: ListenerRegistration? = null

    var books = MutableLiveData<List<Book>>(emptyList())
    var catalogBooks = MutableLiveData<List<Book>>(emptyList())
    var issueHistory = MutableLiveData<List<IssueHistory>>(emptyList())
    var resultMessage = MutableLiveData<String>()

    fun fetchBooks() {
        if (booksRegistration != null) return

        booksRegistration = repo.getBooks(
            onResult = { fetchedBooks ->
                allBooks = fetchedBooks
                catalogBooks.postValue(fetchedBooks)
                publishBooks()
            },
            onError = { message -> resultMessage.postValue(message) }
        )
    }

    fun clearSearch() {
        if (searchQuery.isBlank()) return
        searchQuery = ""
        publishBooks()
    }

    fun clearResultMessage() {
        resultMessage.postValue("")
    }

    fun addBook(
        book: Book,
        createdBy: String,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        repo.addBook(book, createdBy) { success, message ->
            resultMessage.postValue(message)
            onResult(success, message)
        }
    }

    fun fetchIssueHistory(userId: String?, isAdmin: Boolean) {
        historyRegistration?.remove()
        historyRegistration = repo.getIssueHistory(
            userId = userId,
            isAdmin = isAdmin,
            onResult = { history -> issueHistory.postValue(history) },
            onError = { message -> resultMessage.postValue(message) }
        )
    }

    fun deleteBook(bookId: String) {
        repo.deleteBook(bookId)
        resultMessage.postValue("Deleting book...")
    }

    fun updateBook(book: Book) {
        repo.updateBook(book)
    }

    fun searchBooks(query: String) {
        searchQuery = query
        publishBooks()
    }

    private fun publishBooks() {
        val filtered = if (searchQuery.isBlank()) {
            allBooks
        } else {
            allBooks.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.author.contains(searchQuery, ignoreCase = true) ||
                        it.category.contains(searchQuery, ignoreCase = true)
            }
        }

        books.postValue(filtered)
    }

    fun issueOrReturn(bookId: String, userId: String) {
        repo.issueOrReturnBook(bookId, userId) { result ->
            resultMessage.postValue(result)
        }
    }

    override fun onCleared() {
        booksRegistration?.remove()
        historyRegistration?.remove()
        super.onCleared()
    }
}
