package com.mst.xcamera

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController



@Composable
fun NavManager(){

    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "Home")
    {
        composable("Home") { HomeView(navController)  }
        composable("Agregar") { AddView()  }
    }

}