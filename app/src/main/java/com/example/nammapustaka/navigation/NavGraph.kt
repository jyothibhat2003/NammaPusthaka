package com.example.nammapustaka.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nammapustaka.ui.screens.AddBookScreen
import com.example.nammapustaka.ui.screens.BookListScreen
import com.example.nammapustaka.ui.screens.GenAiAssistantScreen
import com.example.nammapustaka.ui.screens.HomeScreen
import com.example.nammapustaka.ui.screens.IssueHistoryScreen
import com.example.nammapustaka.ui.screens.LoginScreen
import com.example.nammapustaka.ui.screens.QRScannerScreen
import com.example.nammapustaka.ui.screens.RegisterScreen
import com.example.nammapustaka.ui.screens.SplashScreen
import com.example.nammapustaka.viewmodel.AuthViewModel
import com.example.nammapustaka.viewmodel.BookViewModel

@Composable
fun NavGraph() {
    val navController: NavHostController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val bookViewModel: BookViewModel = viewModel()
    val currentUser by authViewModel.currentUser.observeAsState()
    val currentUserId by authViewModel.currentUserId.observeAsState("")
    val isLoggedIn by authViewModel.isLoggedIn.observeAsState(false)
    val role = currentUser?.role ?: "student"
    val isAdmin = role == "admin"
    val goBack: () -> Unit = {
        if (!navController.popBackStack()) {
            navController.navigate("home") {
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                navController = navController,
                isLoggedIn = isLoggedIn
            )
        }

        composable("login") {
            LoginScreen(navController, authViewModel)
        }

        composable("register") {
            RegisterScreen(navController, authViewModel)
        }

        composable("home") {
            HomeScreen(
                navController = navController,
                role = role,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("scanner") {
            QRScannerScreen(
                viewModel = bookViewModel,
                userId = currentUserId,
                onBack = goBack
            )
        }

        composable("bookList") {
            BookListScreen(
                viewModel = bookViewModel,
                canManageBooks = isLoggedIn,
                currentUserId = currentUserId,
                isAdmin = isAdmin,
                onAddBook = { navController.navigate("addBook") },
                onBack = goBack
            )
        }

        composable("addBook") {
            AddBookScreen(
                viewModel = bookViewModel,
                userId = currentUserId,
                onBack = goBack,
                onBookAdded = {
                    navController.navigate("bookList") {
                        popUpTo("addBook") { inclusive = true }
                    }
                }
            )
        }

        composable("history") {
            IssueHistoryScreen(
                viewModel = bookViewModel,
                userId = currentUserId,
                isAdmin = isAdmin,
                onBack = goBack
            )
        }

        composable("assistant") {
            GenAiAssistantScreen(
                bookViewModel = bookViewModel,
                onBack = goBack
            )
        }
    }
}
