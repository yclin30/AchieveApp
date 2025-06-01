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
import com.yclin.achieveapp.ui.feature_habits.HabitListScreen
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

                    // 判断当前路由是否为主导航路由（应显示底部导航栏的路由）
                    val showBottomBar = currentRoute in listOf(
                        Screen.Dashboard.route,
                        Screen.Tasks.route,
                        Screen.Habits.route
                    )

                    Scaffold(
                        bottomBar = {
                            // 只在主屏幕显示底部导航栏
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
                            // 底部导航主页面
                            composable(Screen.Dashboard.route) {
                                DashboardScreen(navController = navController)
                            }

                            composable(Screen.Tasks.route) {
                                // 使用Factory创建TaskListViewModel实例
                                val taskListViewModel: TaskListViewModel = viewModel(
                                    factory = TaskListViewModel.Factory(application as AchieveApp)
                                )
                                TaskListScreen(
                                    navController = navController,
                                    viewModel = taskListViewModel
                                )
                            }

                            composable(Screen.Habits.route) {
                                HabitListScreen(navController = navController)
                            }

                            // 添加任务页面
                            composable(Screen.AddEditTask.route) {
                                // 创建AddEditTaskViewModel实例，传入-1L表示新建任务
                                val addEditTaskViewModel: AddEditTaskViewModel = viewModel(
                                    factory = AddEditTaskViewModel.provideFactory(-1L)
                                )
                                AddEditTaskScreen(
                                    navController = navController,
                                    viewModel = addEditTaskViewModel
                                )
                            }

                            // 编辑任务页面
                            composable(
                                route = "${Screen.AddEditTask.route}/{taskId}",
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
                        }
                    }
                }
            }
        }
    }
}