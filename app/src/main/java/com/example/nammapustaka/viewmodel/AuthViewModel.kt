package com.example.nammapustaka.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nammapustaka.data.model.User
import com.example.nammapustaka.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration

class AuthViewModel : ViewModel() {

    private val repo = AuthRepository()
    private var authListener: FirebaseAuth.AuthStateListener? = null
    private var userRegistration: ListenerRegistration? = null

    var authState = MutableLiveData<String>()
    var currentUser = MutableLiveData<User?>()
    var currentUserId = MutableLiveData(repo.currentUserId().orEmpty())
    var isLoggedIn = MutableLiveData(repo.isLoggedIn())

    init {
        authListener = repo.observeAuthState { userId ->
            currentUserId.postValue(userId.orEmpty())
            isLoggedIn.postValue(!userId.isNullOrBlank())

            userRegistration?.remove()
            userRegistration = null

            if (userId.isNullOrBlank()) {
                currentUser.postValue(null)
            } else {
                userRegistration = repo.listenCurrentUser(userId) { user ->
                    currentUser.postValue(user)
                }
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        repo.registerUser(name, email, password, "student") { _, message ->
            authState.postValue(message)
        }
    }

    fun login(email: String, password: String) {
        repo.loginUser(email, password) { _, message ->
            authState.postValue(message)
            if (message == "Login Success") {
                loadCurrentUser()
            }
        }
    }

    fun loadCurrentUser() {
        repo.getCurrentUser { user ->
            currentUser.postValue(user)
        }
    }

    fun logout() {
        repo.logout()
        authState.postValue("")
    }

    override fun onCleared() {
        userRegistration?.remove()
        authListener?.let { repo.removeAuthStateListener(it) }
        super.onCleared()
    }
}
