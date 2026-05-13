package com.example.nammapustaka.data.repository

import com.example.nammapustaka.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun registerUser(
        name: String,
        email: String,
        password: String,
        role: String,
        onResult: (Boolean, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result.user?.uid.orEmpty()
                    val user = User(userId, name, email, role)

                    if (userId.isBlank()) {
                        onResult(false, "Unable to create user profile")
                        return@addOnCompleteListener
                    }

                    db.collection("users").document(userId)
                        .set(user)
                        .addOnSuccessListener {
                            onResult(true, "Success")
                        }
                        .addOnFailureListener { error ->
                            onResult(false, error.message ?: "Error")
                        }
                } else {
                    onResult(false, task.exception?.message ?: "Error")
                }
            }
    }

    fun loginUser(
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onResult(true, "Login Success")
                } else {
                    onResult(false, it.exception?.message ?: "Error")
                }
            }
    }

    fun currentUserId(): String? = auth.currentUser?.uid

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun observeAuthState(onChanged: (String?) -> Unit): FirebaseAuth.AuthStateListener {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            onChanged(firebaseAuth.currentUser?.uid)
        }
        auth.addAuthStateListener(listener)
        return listener
    }

    fun removeAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        auth.removeAuthStateListener(listener)
    }

    fun listenCurrentUser(
        userId: String,
        onResult: (User?) -> Unit
    ): ListenerRegistration {
        return db.collection("users").document(userId)
            .addSnapshotListener { snapshot, _ ->
                onResult(snapshot?.toObject(User::class.java))
            }
    }

    fun getCurrentUser(onResult: (User?) -> Unit) {
        val userId = currentUserId()

        if (userId.isNullOrBlank()) {
            onResult(null)
            return
        }

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.toObject(User::class.java))
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun logout() {
        auth.signOut()
    }
}
