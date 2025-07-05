package com.example.bookhunt.presentation.Navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.bookhunt.presentation.AllBooksByCategory.BooksByCategoryScreen
import com.example.bookhunt.presentation.HomeScreen.HomeScreen
import com.example.bookhunt.presentation.UIComponent.PdfViewerScreen

@Composable
fun NavGraph(navHostController: NavHostController) {
    NavHost(
        navController = navHostController ,
        startDestination = Routes.HomeScreen
    ) {
        composable<Routes.HomeScreen> {
            HomeScreen(navHostController = navHostController)
        }
        
        composable<Routes.ShowPdfScreen> {backStackEntry ->
            val data : Routes.ShowPdfScreen = backStackEntry.toRoute()
            PdfViewerScreen(url = data.url)
        }

        composable<Routes.BooksByCategory> {BackStackEntry ->
            val data2 : Routes.BooksByCategory = BackStackEntry.toRoute()
            BooksByCategoryScreen(category = data2.category, navHostController = navHostController)
        }
    }
}