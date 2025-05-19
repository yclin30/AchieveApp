package com.yclin.achieveapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yclin.achieveapp.ui.components.AchieveBottomNavigation
import com.yclin.achieveapp.ui.feature_dashboard.DashboardScreen
import com.yclin.achieveapp.ui.feature_habits.HabitListScreen
import com.yclin.achieveapp.ui.feature_tasks.TaskListScreen
import com.yclin.achieveapp.ui.navigation.Screen
import com.yclin.achieveapp.ui.theme.AchieveTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AchieveTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    Scaffold(
                        bottomBar = {
                            // 只在主屏幕显示底部导航栏
                            if (currentRoute in listOf(
                                    Screen.Dashboard.route,
                                    Screen.Tasks.route,
                                    Screen.Habits.route
                                )) {
                                AchieveBottomNavigation(navController = navController)
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Dashboard.route,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            // 底部导航主页面
                            composable(Screen.Dashboard.route) {
                                DashboardScreen(navController = navController)
                            }

                            composable(Screen.Tasks.route) {
                                TaskListScreen(navController = navController)
                            }

                            composable(Screen.Habits.route) {
                                HabitListScreen(navController = navController)
                            }
                        }
                    }
                }
            }
        }
    }
}