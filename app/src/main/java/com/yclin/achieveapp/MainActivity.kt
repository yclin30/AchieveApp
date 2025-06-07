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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yclin.achieveapp.ui.components.AchieveBottomNavigation
import com.yclin.achieveapp.ui.feature_dashboard.DashboardScreen
import com.yclin.achieveapp.ui.feature_habits.add_edit.AddEditHabitScreen
import com.yclin.achieveapp.ui.feature_habits.add_edit.AddEditHabitViewModel
import com.yclin.achieveapp.ui.feature_habits.list.HabitListScreen
import com.yclin.achieveapp.ui.feature_habits.list.HabitListViewModel
import com.yclin.achieveapp.ui.feature_tasks.add_edit.AddEditTaskScreen
import com.yclin.achieveapp.ui.feature_tasks.add_edit.AddEditTaskViewModel
import com.yclin.achieveapp.ui.feature_tasks.list.TaskListScreen
import com.yclin.achieveapp.ui.feature_tasks.list.TaskListViewModel
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

                    val showBottomBar = currentRoute in listOf(
                        Screen.Dashboard.route,
                        Screen.Tasks.route,
                        Screen.Habits.route
                    )

                    Scaffold(
                        bottomBar = {
                            if (showBottomBar) {
                                AchieveBottomNavigation(navController = navController)
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Dashboard.route,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable(Screen.Dashboard.route) {
                                DashboardScreen(navController = navController)
                            }

                            composable(Screen.Tasks.route) {
                                val taskListViewModel: TaskListViewModel = viewModel(
                                    factory = TaskListViewModel.Factory(application as AchieveApp)
                                )
                                TaskListScreen(
                                    navController = navController,
                                    viewModel = taskListViewModel
                                )
                            }

                            composable(Screen.Habits.route) {
                                val habitListViewModel: HabitListViewModel = viewModel(
                                    factory = HabitListViewModel.Factory(application as AchieveApp)
                                )
                                HabitListScreen(
                                    navController = navController,
                                    viewModel = habitListViewModel
                                )
                            }

                            // 任务新增/编辑
                            composable(
                                route = Screen.AddEditTask.route,
                                arguments = listOf(
                                    navArgument("taskId") {
                                        type = NavType.LongType
                                        defaultValue = -1L
                                    }
                                )
                            ) { backStackEntry ->
                                val taskId = backStackEntry.arguments?.getLong("taskId") ?: -1L
                                val addEditTaskViewModel: AddEditTaskViewModel = viewModel(
                                    factory = AddEditTaskViewModel.provideFactory(taskId)
                                )
                                AddEditTaskScreen(
                                    navController = navController,
                                    viewModel = addEditTaskViewModel
                                )
                            }

                            // 习惯新增/编辑
                            composable(
                                route = Screen.AddEditHabit.route,
                                arguments = listOf(
                                    navArgument("habitId") {
                                        type = NavType.LongType
                                        defaultValue = -1L
                                    }
                                )
                            ) { backStackEntry ->
                                val habitId = backStackEntry.arguments?.getLong("habitId") ?: -1L
                                val addEditHabitViewModel: AddEditHabitViewModel = viewModel(
                                    factory = AddEditHabitViewModel.provideFactory(habitId)
                                )
                                AddEditHabitScreen(
                                    navController = navController,
                                    viewModel = addEditHabitViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}