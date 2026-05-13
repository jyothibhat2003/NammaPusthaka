package com.example.nammapustaka.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.nammapustaka.ui.components.ActionTile
import com.example.nammapustaka.ui.components.AppScreen
import com.example.nammapustaka.ui.components.SecondaryActionButton
import com.example.nammapustaka.ui.components.StatusPill
import com.example.nammapustaka.ui.components.TwoColumnStats

@Composable
fun HomeScreen(
    navController: NavController,
    role: String,
    onLogout: () -> Unit
) {
    val isAdmin = role == "admin"

    AppScreen(
        title = "Namma Pustaka",
        subtitle = "Library workspace",
        trailing = {
            StatusPill(
                text = if (isAdmin) "Admin" else "Student",
                icon = if (isAdmin) Icons.Outlined.AdminPanelSettings else Icons.Outlined.Person
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(10.dp))
                TwoColumnStats(
                    firstLabel = "Mode",
                    firstValue = if (isAdmin) "Admin" else "Student",
                    secondLabel = "Access",
                    secondValue = "Live"
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ActionTile(
                            title = "Books",
                            subtitle = "Browse catalog",
                            icon = Icons.Outlined.AutoStories,
                            onClick = { navController.navigate("bookList") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    ActionTile(
                        title = "Add Book",
                        subtitle = "Create catalog entry",
                        icon = Icons.Outlined.Add,
                        onClick = { navController.navigate("addBook") }
                    )

                    ActionTile(
                        title = "Scan",
                        subtitle = "Issue or return by QR",
                        icon = Icons.Outlined.QrCodeScanner,
                        onClick = { navController.navigate("scanner") }
                    )

                    ActionTile(
                        title = "History",
                        subtitle = if (isAdmin) "All circulation" else "Your circulation",
                        icon = Icons.Outlined.History,
                        onClick = { navController.navigate("history") }
                    )

                    ActionTile(
                        title = "AI Assistant",
                        subtitle = "Ask the catalog",
                        icon = Icons.Outlined.SmartToy,
                        onClick = { navController.navigate("assistant") }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                SecondaryActionButton(
                    text = "Logout",
                    icon = Icons.Outlined.Logout,
                    onClick = onLogout
                )
            }
        }
    }
}
