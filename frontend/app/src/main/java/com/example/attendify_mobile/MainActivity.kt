package com.example.attendify_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.*
import com.example.attendify_mobile.ui.screens.LoginScreen
import com.example.attendify_mobile.ui.screens.StudentDashboard
import com.example.attendify_mobile.ui.screens.MarkAttendance

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "login") {

                composable("login") {
                    LoginScreen(navController)
                }

                composable("dashboard") {
                    StudentDashboard(navController)
                }

                composable("mark_attendance") {
                    MarkAttendance()
                }

            }
        }
    }
}