package com.example.papb0

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.papb0.pages.HomePage
import com.example.papb0.pages.LoginPage
import com.example.papb0.pages.SignupPage

// Perbaiki PageNavigation.kt
@Composable
fun PageNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    todoViewModel: TodoViewModel
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login", builder = {
        composable("login"){
            LoginPage(modifier, navController, authViewModel)
        }
        composable("signup"){
            SignupPage(modifier, navController, authViewModel)
        }
        composable("home"){
            // Tambahkan todoViewModel di sini
            HomePage(modifier, navController, authViewModel, todoViewModel)
        }
    })
}


