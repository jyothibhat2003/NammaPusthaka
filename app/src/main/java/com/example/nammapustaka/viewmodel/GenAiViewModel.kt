package com.example.nammapustaka.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammapustaka.data.model.Book
import com.example.nammapustaka.data.repository.GenAiRepository
import kotlinx.coroutines.launch

class GenAiViewModel : ViewModel() {

    private val repo = GenAiRepository()

    val answer = MutableLiveData("")
    val loading = MutableLiveData(false)

    fun ask(question: String, books: List<Book>) {
        if (question.isBlank()) return

        loading.postValue(true)
        answer.postValue("")

        viewModelScope.launch {
            val result = repo.askAssistant(question.trim(), books)
            answer.value = result
            loading.value = false
        }
    }
}
