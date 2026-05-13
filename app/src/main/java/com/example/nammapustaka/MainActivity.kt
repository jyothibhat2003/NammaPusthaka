package com.example.nammapustaka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.nammapustaka.navigation.NavGraph
import com.example.nammapustaka.ui.theme.NammaPustakaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Optional modern UI (safe to keep)
        enableEdgeToEdge()

        setContent {
            NammaPustakaTheme {
                NavGraph()
            }
        }
    }
}