package com.example.papb0

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.papb0.ui.theme.Papb0Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authViewModel: AuthViewModel by viewModels()
        val todoViewModel: TodoViewModel by viewModels()

        setContent {
            Papb0Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PageNavigation(
                        modifier = Modifier.padding(innerPadding),
                        authViewModel = authViewModel,
                        todoViewModel = todoViewModel
                    )
                }
            }
        }
    }
}